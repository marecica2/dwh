package org.bmsource.dwh.common.job.step.importer;

import org.bmsource.dwh.common.BaseFact;
import org.bmsource.dwh.common.job.JobConstants;
import org.bmsource.dwh.common.excel.DataRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
@StepScope
public class CompositeImportItemWriter<Fact extends BaseFact> implements ItemStreamWriter<DataRow<Fact>> {

    private static Logger logger = LoggerFactory.getLogger(CompositeImportItemWriter.class);

    @Value("#{stepExecutionContext['fileName']}")
    private String fileName;

    @Autowired
    private JdbcBatchItemWriter<Fact> jdbcBatchItemWriter;

    @Autowired
    private ExcelItemWriter excelItemWriter;

    private Integer skippedRows = 0;

    @Override
    public void write(List<? extends DataRow<Fact>> items) throws Exception {

        List<Fact> validItems = new LinkedList<>();
        List<DataRow> errorRows = new LinkedList<>();
        for (DataRow<Fact> item : items) {
            if (item.isValid()) {
                validItems.add(item.getFact());
            } else {
                errorRows.add(item);
            }
        }
        logger.debug("Thread {} File: {}, Items count: {}, Valid items: {}, Invalid rows: {}",
            Thread.currentThread().getName(),
            fileName,
            items.size(),
            validItems.size(),
            errorRows.size()
        );

        jdbcBatchItemWriter.write(validItems);
        excelItemWriter.write(errorRows);
        skippedRows +=  errorRows.size();
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        excelItemWriter.open(executionContext);
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        executionContext.put(JobConstants.skippedRowsKey, skippedRows);
        excelItemWriter.update(executionContext);
    }

    @Override
    public void close() throws ItemStreamException {
        excelItemWriter.close();
    }
}
