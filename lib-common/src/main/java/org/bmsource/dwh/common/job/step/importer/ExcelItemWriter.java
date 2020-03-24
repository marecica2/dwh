package org.bmsource.dwh.common.job.step.importer;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.bmsource.dwh.common.job.JobConstants;
import org.bmsource.dwh.common.excel.DataRow;
import org.bmsource.dwh.common.excel.writer.ExcelWriter;
import org.bmsource.dwh.common.filemanager.FileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


@Component
@StepScope
public class ExcelItemWriter implements ItemStreamWriter<DataRow> {

    private Logger logger = LoggerFactory.getLogger(ExcelItemReader.class.getName());

    @Value("#{jobParameters['transaction']}")
    private String transaction;

    @Value("#{stepExecutionContext['fileName']}")
    private String fileName;

    @Autowired
    private FileManager fileManager;

    private ExcelWriter writer;

    private int rowsCount = -1;

    private Iterator<Row> rowIterator;

    private Workbook workbook;

    private OutputStream outputStream;

    private List<String> header;

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        try {
            String headerString = executionContext.getString(JobConstants.headerKey);
            header = Arrays.asList(headerString.split(","));

            outputStream = fileManager.writeErrorStream(transaction, fileName);
            writer = new ExcelWriter(outputStream);
            writer.open();
            writer.writeHeader(header);
        } catch (Exception e) {
            throw new ItemStreamException(e);
        }
    }

    @Override
    public void write(List<? extends DataRow> items) {
        writer.writeRows(header, (List<DataRow>) items);
    }

    @Override
    public void close() throws ItemStreamException {
        try {
            writer.close();
            outputStream.close();
        } catch (IOException e) {
            throw new ItemStreamException(e);
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {

    }
}
