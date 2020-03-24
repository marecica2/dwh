package org.bmsource.dwh.common.excel.reader;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ExcelReaderTest {

    @Test
    public void testRowsReading() throws IOException {
        InputStream stream = new ClassPathResource("/spends.xlsx").getInputStream();
        ExcelReader reader = new ExcelReader(stream);
        List<Object> header = reader.getHeader();
        int rowsCount = 1;
        while(reader.hasNextRow()) {
            rowsCount++;
            List<Object> row = reader.nextRow();
            assertTrue(row.get(0) instanceof String);
        }
        reader.close();
        assertEquals(38, header.size());
        assertEquals("S. No.", header.get(0));
        assertEquals(rowsCount, reader.getTotalRowsCount());
        assertEquals(rowsCount, reader.getReadRowsCount());
    }
}
