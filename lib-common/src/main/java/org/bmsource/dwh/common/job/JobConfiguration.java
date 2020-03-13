package org.bmsource.dwh.common.job;

import org.bmsource.dwh.common.appstate.AppStateConfiguration;
import org.bmsource.dwh.common.appstate.AppStateService;
import org.bmsource.dwh.common.appstate.EnableImportEvents;
import org.bmsource.dwh.common.filemanager.TmpFileManager;
import org.bmsource.dwh.common.job.step.CleanUpTasklet;
import org.bmsource.dwh.common.job.step.ZipErrorsTasklet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.dao.Jackson2ExecutionContextStringSerializer;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableImportEvents
@EnableBatchProcessing
@EnableAutoConfiguration
@ComponentScan
@Import({AppStateConfiguration.class, TmpFileManager.class })
public class JobConfiguration<RawFact, Fact> {

    private Logger logger = LoggerFactory.getLogger(JobConfiguration.class);

    private StepBuilderFactory stepBuilderFactory;

    PlatformTransactionManager transactionManager;

    @Autowired
    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Autowired
    public void setStepBuilderFactory(StepBuilderFactory stepBuilderFactory) {
        this.stepBuilderFactory = stepBuilderFactory;
    }

    private ImportJobConfiguration<RawFact, Fact> importJobConfiguration;

    @Autowired
    public void setImportJobConfiguration(ImportJobConfiguration<RawFact, Fact> importJobConfiguration) {
        this.importJobConfiguration = importJobConfiguration;
    }

    @Bean("rawFact")
    public RawFact rawFact() {
        return importJobConfiguration.getBaseEntity();
    }

    @Bean("fact")
    public Fact fact() {
        return importJobConfiguration.getMappedEntity();
    }

    private DataSource dataSource;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private JobExecutionListener jobListener;

    @Autowired(required = false)
    public void setJobListener(JobExecutionListener jobListener) {
        this.jobListener = jobListener;
    }

    private ZipErrorsTasklet zipErrorsTasklet;

    @Autowired
    public void setZipErrorsTasklet(ZipErrorsTasklet zipErrorsTasklet) {
        this.zipErrorsTasklet = zipErrorsTasklet;
    }

    private CleanUpTasklet cleanUpTasklet;

    @Autowired
    public void setCleanUpTasklet(CleanUpTasklet cleanUpTasklet) {
        this.cleanUpTasklet = cleanUpTasklet;
    }

    private Step excelStep;

    @Autowired
    @Qualifier("excelStep")
    public void setExcelStep(Step excelStep) {
        this.excelStep = excelStep;
    }

    private ThreadPoolTaskExecutor executor;

    @Autowired
    public void setExecutor(ThreadPoolTaskExecutor executor) {
        this.executor = executor;
    }

    @Bean
    public Step postImportStep() {
        return this.stepBuilderFactory.get("cleanupStep")
            .tasklet(zipErrorsTasklet)
            .build();
    }

    @Bean
    public Step beforeImportStep() {
        return this.stepBuilderFactory.get("beforeStep")
            .tasklet(cleanUpTasklet)
            .build();
    }

    private Step normalizerStep;

    @Autowired(required = false)
    @Qualifier("normalizerStep")
    public void setNormalizerStep(Step step) {
        this.normalizerStep = step;
    }

    @Bean
    public Job importJob(@Autowired JobBuilderFactory jobBuilderFactory) {
        SimpleJobBuilder jobBuilder = jobBuilderFactory
            .get(JobConstants.jobName)
            .start(beforeImportStep())
            .next(excelStep)
            .next(postImportStep());

        if (normalizerStep != null) {
            jobBuilder = jobBuilder.next(normalizerStep);
        }

        if (jobListener != null) {
            jobBuilder = jobBuilder
                .listener(jobListener);
        }
        return jobBuilder.build();
    }

    @Bean
    public JobExecutionListener jobListener() {
        return new JobExecutionListenerSupport() {

            @Autowired
            AppStateService appStateService;

            @Override
            public void beforeJob(JobExecution jobExecution) {
                if (jobExecution.getJobParameters().getParameters().size() == 0)
                    return;
                String tenant = jobExecution.getJobParameters().getString("tenant");
                String transaction = jobExecution.getJobParameters().getString("transaction");
                String project = jobExecution.getJobParameters().getString("project");
                List<String> files =
                    Arrays.asList((jobExecution.getJobParameters().getString("files")).split(","));
                Map<String, Object> state = new HashMap<>();

                logger.info("Import started for tenant {} project {} transaction {}", tenant, project, transaction);
                state.put("type", "importStatus");
                state.put("running", true);
                state.put("files", files);
                appStateService.updateState(tenant, project, "importStatus", state);
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                if (jobExecution.getJobParameters().getParameters().size() == 0)
                    return;
                String tenant = jobExecution.getJobParameters().getString("tenant");
                String project = jobExecution.getJobParameters().getString("project");
                Map<String, Object> state = new HashMap<>();
                state.put("type", "importStatus");
                state.put("running", false);
                appStateService.updateState(tenant, project, "importStatus", state);
            }
        };
    }

    @Bean
    public JobLauncher simpleJobLauncher() throws Exception {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(myJobRepository());
        jobLauncher.setTaskExecutor(executor);
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }

    @Primary
    @Bean
    public JobRepository myJobRepository() {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(dataSource);
        factory.setDatabaseType("POSTGRES");
        factory.setTransactionManager(transactionManager);
        factory.setSerializer(new Jackson2ExecutionContextStringSerializer());
        factory.setTablePrefix("batch_");
        JobRepository jobRepository = null;
        try {
            jobRepository = factory.getObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jobRepository;
    }
}
