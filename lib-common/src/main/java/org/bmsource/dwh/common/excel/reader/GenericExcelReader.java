package org.bmsource.dwh.common.excel.reader;

import org.springframework.scheduling.annotation.Async;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GenericExcelReader<T> {
    private ExcelReaderHandler handler;
    private Class<T> classType;

    public GenericExcelReader(ExcelReaderHandler handler, Class<T> classType) {
        this.handler = handler;
        this.classType = classType;
    }

    @Async("asyncExecutor")
    public void parse(InputStream stream) throws Exception {
        BatchReader reader = new ExcelBatchReader();
        final List<ExcelRowMapper<T>> rowMapper = new ArrayList<>();

        reader.readContent(stream, new BatchHandler() {
            @Override
            public void onStart() {
                handler.onStart();
            }

            @Override
            public void onRead(List<List<Object>> rows, List<String> header, int rowsCount,
                               int totalRowsCount) {
                if (rowMapper.size() == 0) {
                    Map<String, String> simpleMapping =
                        header.stream().collect(Collectors.toMap(String::toString, item -> item));
                    rowMapper.add(new ExcelRowMapper<T>(classType, header, simpleMapping));
                }
                List<T> items = rowMapper.get(0).mapList(rows);
                handler.onRead(items);
            }

            @Override
            public void onFinish(int totalRowsCount) {
                handler.onFinish(totalRowsCount);
            }
        });
    }
}
