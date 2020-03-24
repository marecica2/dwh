package org.bmsource.dwh.common.excel.reader;

import com.monitorjbl.xlsx.StreamingReader;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ExcelReader<T> implements DataReader<List<Object>> {

    private static final int DEFAULT_CACHE_SIZE = 5000;

    private static final int DEFAULT_BUFFER_SIZE = 1024;

    private Workbook workbook;

    private InputStream inputStream;

    private Sheet sheet;

    private int rowsCount;

    private List<String> header;

    int rowsRead = 0;

    Iterator<Row> rowIterator;

    public ExcelReader(InputStream inputStream) {
        this(inputStream, DEFAULT_CACHE_SIZE, DEFAULT_BUFFER_SIZE);
    }

    ExcelReader(InputStream inputStream, int cacheSize, int bufferSize) {
        this.inputStream = inputStream;
        this.workbook = StreamingReader.builder()
            .rowCacheSize(cacheSize)
            .bufferSize(bufferSize)
            .open(inputStream);
        sheet = workbook.getSheetAt(0);
        rowIterator = sheet.rowIterator();
        rowsCount = sheet.getLastRowNum() - 1;
        header = nextRow().stream()
            .map(Object::toString)
            .collect(Collectors.toList());
    }

    @Override
    public int getTotalRowsCount() {
        return rowsCount;
    }

    @Override
    public int getReadRowsCount() {
        return rowsRead;
    }

    @Override
    public void close() throws IOException {
        workbook.close();
        inputStream.close();
    }

    @Override
    public List<Object> nextRow() {
        List<Object> row = new LinkedList<>();
        if (rowIterator.hasNext()) {
            int prevCellIndex = 0;
            Row sheetRow = rowIterator.next();
            for (Cell sheetCell : sheetRow) {
                int currentIndex = sheetCell.getColumnIndex();
                fillGaps(row, prevCellIndex, currentIndex);
                row.add(readCellValue(sheetCell));
                prevCellIndex = currentIndex;
            }
            rowsRead++;
            if (row.size() > 0 && row.get(0) != null) {
                return row;
            }
        }
        return null;
    }

    @Override
    public List<String> getHeader() {
        return header;
    }

    @Override
    public boolean hasNextRow() {
        return rowIterator.hasNext() && rowsRead < rowsCount;
    }

    Comparable readCellValue(Cell cell) {
        return cell.getStringCellValue();
    }

    void fillGaps(List<Object> row, int prevIndex, int currentIndex) {
        for (int i = prevIndex + 1; i < currentIndex; i++) {
            row.add(null);
        }
    }
}
