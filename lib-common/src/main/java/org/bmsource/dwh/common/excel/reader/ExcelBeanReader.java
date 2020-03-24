package org.bmsource.dwh.common.excel.reader;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.bmsource.dwh.common.excel.DataRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ExcelBeanReader<Bean> implements DataReader<Bean> {

    private static Logger logger = LoggerFactory.getLogger(ExcelBeanReader.class);

    private static final int DEFAULT_CACHE_SIZE = 5000;

    private static final int DEFAULT_BUFFER_SIZE = 1024;

    private final Map<String, Function<Cell, Comparable>> parsers;

    private final ExcelRowMapper<Bean> rowMapper;

    private final ExcelReader<List<Object>> reader;

    private final ExcelRowValidator<Object> validator;

    private final Map<String, String> mapping;

    public ExcelBeanReader(InputStream inputStream, Class<Bean> beanClass, Map<String, String> mapping) {
        this(inputStream, beanClass, mapping, DEFAULT_CACHE_SIZE, DEFAULT_BUFFER_SIZE);
    }

    public ExcelBeanReader(InputStream inputStream, Class<Bean> beanClass, Map<String, String> mapping, int cacheSize,
                           int bufferSize) {
        this.mapping = mapping;
        reader = new ExcelReader<>(inputStream, cacheSize, bufferSize);
        parsers = initCellParsers(beanClass, mapping);
        rowMapper = new ExcelRowMapper<>(beanClass, reader.getHeader(), mapping);
        validator = new ExcelRowValidator<>(mapping);
    }

    @Override
    public List<String> getHeader() {
        return reader.getHeader();
    }

    @Override
    public boolean hasNextRow() {
        return reader.hasNextRow();
    }

    @Override
    public Bean nextRow() {
        return rowMapper.map(next());
    }

    public DataRow<Bean> nextValidatedRow() {
        List<Object> row = next();
        if (row != null) {
            Bean bean = rowMapper.map(row);
            ExcelRowValidator.ValidationErrors errors = validator.getValidationErrors(bean);
            DataRow data = new DataRow();
            data.setErrors(errors);
            data.setColumnMapping(mapping);
            data.setFact(bean);
            data.setRow(row);
            data.validate();
            return data;
        }
        return null;
    }

    private List<Object> next() {
        List<Object> row = new LinkedList<>();
        if (reader.rowIterator.hasNext()) {
            int prevCellIndex = 0;
            Row sheetRow = reader.rowIterator.next();
            for (Cell cell : sheetRow) {
                int currentIndex = cell.getColumnIndex();
                reader.fillGaps(row, prevCellIndex, currentIndex);
                row.add(readCellValue(cell));
                prevCellIndex = currentIndex;
            }
            reader.rowsRead++;
            if (row.size() > 0) {
                return row;
            }
        }
        return null;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    @Override
    public int getTotalRowsCount() {
        return reader.getTotalRowsCount();
    }

    @Override
    public int getReadRowsCount() {
        return reader.getReadRowsCount();
    }

    private Comparable readCellValue(Cell cell) {
        if (cell.getColumnIndex() >= reader.getHeader().size())
            return null;

        String column = reader.getHeader().get(cell.getColumnIndex());
        if (parsers.get(column) == null)
            return null;
        try {
            return parsers.get(column).apply(cell);
        } catch (Exception e) {
            logger.trace("{}", e.toString());
            return null;
        }
    }

    private Map<String, Function<Cell, Comparable>> initCellParsers(Class<Bean> beanClass,
                                                                    Map<String, String> mapping) {
        return mapping
            .entrySet()
            .stream().collect(Collectors.toMap(Map.Entry::getKey,
                entry -> {
                    try {
                        Function<Cell, Comparable> fn;
                        Field field = beanClass.getDeclaredField(entry.getValue());

                        if (Number.class.isAssignableFrom(field.getType()))
                            return Cell::getNumericCellValue;

                        switch (field.getType().getSimpleName()) {
                            case "Boolean":
                                fn = Cell::getBooleanCellValue;
                                break;
                            case "Date":
                                fn = Cell::getDateCellValue;
                                break;
                            default:
                                fn = Cell::getStringCellValue;
                                break;
                        }
                        return fn;
                    } catch (NoSuchFieldException e) {
                        return Cell::getStringCellValue;
                    }
                }));
    }
}
