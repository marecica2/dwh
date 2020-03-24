package org.bmsource.dwh.common.excel.reader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ExcelBatchReader implements BatchReader {

    private void readExcelStream(InputStream inputStream, BatchHandler batchHandler,
                                 Integer batchSize, Integer rowsLimit) throws Exception {
        try (
            ExcelReader<List<Object>> reader = new ExcelReader<>(inputStream)) {
            batchHandler.onStart();

            int totalRows = 0;
            List<List<Object>> rows = new LinkedList<>();
            List<String> header = reader.getHeader();

            while ((reader.hasNextRow()) && (totalRows < rowsLimit || rowsLimit == -1)) {
                List<Object> row = reader.nextRow();
                rows.add(row);
                totalRows++;
                if (totalRows % batchSize == 0) {
                    batchHandler.onRead(rows, header, totalRows, reader.getTotalRowsCount());
                    rows = new LinkedList<>();
                }
            }
            batchHandler.onRead(rows, header, totalRows, reader.getTotalRowsCount());
            batchHandler.onFinish(reader.getTotalRowsCount());
        }
    }

    @Override
    public void readContent(InputStream inputStream, BatchHandler batchHandler) throws Exception {
        readExcelStream(inputStream, batchHandler, 5000, -1);
    }

    @Override
    public void readContent(InputStream inputStream, BatchHandler batchHandler, Integer batchSize) throws Exception {
        readExcelStream(inputStream, batchHandler, batchSize, -1);
    }

    @Override
    public List<List<Object>> readContent(InputStream inputStream, int rowsToRead) throws Exception {
        if (rowsToRead > 1000) {
            throw new IllegalArgumentException("Rows to read must not be greater than 1000");
        }
        List<List<Object>> result = new ArrayList<>();
        readExcelStream(inputStream, (rows, header, rowsCount, totalRowsCount) -> {
                result.addAll(rows);
            },
            rowsToRead, rowsToRead);
        return result;
    }
}
