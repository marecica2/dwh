package org.bmsource.dwh.common.job.step.importer;

import org.bmsource.dwh.common.BaseFact;
import org.bmsource.dwh.common.appstate.client.AppStateService;
import org.bmsource.dwh.common.io.DataRow;
import org.bmsource.dwh.common.job.ImportJobConfiguration;
import org.bmsource.dwh.common.job.JobConstants;
import org.bmsource.dwh.common.multitenancy.impl.concurrent.ContextAwarePoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class ImportStepConfiguration<RawFact extends BaseFact> {

    private Logger logger = LoggerFactory.getLogger(ImportStepConfiguration.class);

    private static final int BATCH_SIZE = 5000;

    private static final int MAX_CONCURRENT_FILES = 10;

    private RawFact rawFact;

    @Autowired
    public void setImportJobConfiguration(ImportJobConfiguration<RawFact, ?> importJobConfiguration) {
        this.rawFact = importJobConfiguration.getBaseEntity();
    }

    private DataSource dataSource;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    public void setStepBuilderFactory(StepBuilderFactory stepBuilderFactory) {
        this.stepBuilderFactory = stepBuilderFactory;
    }

    private ImportPartitioner excelImportPartitioner;

    @Autowired
    public void setExcelImportPartitioner(ImportPartitioner excelImportPartitioner) {
        this.excelImportPartitioner = excelImportPartitioner;
    }

    private ItemWriteListener<RawFact> writeListener;

    @Autowired(required = false)
    public void setWriteListener(ItemWriteListener<RawFact> writeListener) {
        this.writeListener = writeListener;
    }

    private ChunkListener chunkListener;

    @Autowired(required = false)
    public void setChunkListener(ChunkListener chunkListener) {
        this.chunkListener = chunkListener;
    }

    private AppStateService appStateService;

    @Autowired
    public void setAppStateService(AppStateService appStateService) {
        this.appStateService = appStateService;
    }

    private ExcelItemReader<RawFact> reader;

    @Autowired
    public void setReader(ExcelItemReader<RawFact> reader) {
        this.reader = reader;
    }

    private CompositeImportItemWriter<RawFact> compositeItemWriter;

    @Autowired
    public void setCompositeItemWriter(CompositeImportItemWriter<RawFact> compositeItemWriter) {
        this.compositeItemWriter = compositeItemWriter;
    }

    private ThreadPoolTaskExecutor executor;

    @Autowired
    public void setExecutor(ThreadPoolTaskExecutor executor) {
        this.executor = executor;
    }

    @Bean
    public JdbcBatchItemWriter<RawFact> jdbcWriter() {
        JdbcBatchItemWriter<RawFact> jdbcWriter = new JdbcBatchItemWriterBuilder<RawFact>()
            .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
            .sql(writeSql())
            .dataSource(dataSource)
            .build();
        jdbcWriter.afterPropertiesSet();
        return jdbcWriter;
    }

    @Bean(name = "excelStep")
    public Step excelReadPartitionStep() {
        return stepBuilderFactory.get("excelReadPartitionStep")
            .partitioner("excelReadPartitioner", excelImportPartitioner)
            .partitionHandler(excelReadPartitioner())
            .build();
    }

    @Bean
    public PartitionHandler excelReadPartitioner() {
        TaskExecutorPartitionHandler partitionHandler = new TaskExecutorPartitionHandler();
        partitionHandler.setTaskExecutor(taskExecutor());
        partitionHandler.setStep(excelReadStep());
        partitionHandler.setGridSize(MAX_CONCURRENT_FILES);
        return partitionHandler;
    }

    @Bean
    public Step excelReadStep() {
        SimpleStepBuilder<DataRow<RawFact>, DataRow<RawFact>> step = stepBuilderFactory.get("excelReadStep")
            .<DataRow<RawFact>, DataRow<RawFact>>chunk(BATCH_SIZE)
            .reader(reader)
            .writer(compositeItemWriter);
        step.listener(promotionListener());
        if (writeListener != null) {
            step.listener(writeListener);
        }
        if (chunkListener != null) {
            step.listener(chunkListener);
        }
        return step
            .build();
    }

    @Bean
    public ExecutionContextPromotionListener promotionListener() {
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(new String[]{JobConstants.skippedRowsKey, JobConstants.totalRowsKey});
        return listener;
    }

    @Bean
    public ChunkListener chunkListener() {
        return new ChunkListener() {

            @Override
            public void beforeChunk(ChunkContext context) {
            }

            @Override
            public void afterChunk(ChunkContext context) {
                Map<String, Object> ec = context.getStepContext().getStepExecutionContext();
                int rows = context.getStepContext().getStepExecution().getWriteCount();
                int totalRows = (Integer) ec.get(JobConstants.totalRowsKey);
                int skippedRows = (Integer) ec.get(JobConstants.skippedRowsKey);

                String tenant = (String) context.getStepContext().getJobParameters().get(JobConstants.tenantKey);
                String project = (String) context.getStepContext().getJobParameters().get(JobConstants.projectKey);
                String file = (String) ec.get(JobConstants.fileNameKey);

                logger.info("Import {} {} of {} rows, skipped {}", file, rows, totalRows, skippedRows);

                Map<String, Object> state = new HashMap<>();
                state.put("type", "importStatusFile");
                state.put("running", true);
                state.put("fileName", file);
                state.put("rowsCount", rows);
                state.put("skippedRows", skippedRows);
                state.put("totalRowsCount", totalRows);
                appStateService.updateState(tenant, project, "importStatus", state);
            }

            @Override
            public void afterChunkError(ChunkContext context) {
            }
        };
    }

    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ContextAwarePoolExecutor();
        executor.setThreadNamePrefix("Partition_");
        executor.setMaxPoolSize(MAX_CONCURRENT_FILES);
        executor.setCorePoolSize(MAX_CONCURRENT_FILES / 2);
        executor.setQueueCapacity(MAX_CONCURRENT_FILES / 2);
        executor.afterPropertiesSet();
        return executor;
    }

    private String writeSql() {
        String sql = null;
        try {
            sql = rawFact.getClass().newInstance().insertSQL();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return sql;
    }
}
