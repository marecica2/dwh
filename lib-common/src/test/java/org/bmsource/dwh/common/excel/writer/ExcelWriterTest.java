package org.bmsource.dwh.common.excel.writer;

import org.apache.commons.io.FileUtils;
import org.bmsource.dwh.common.excel.DataRow;
import org.bmsource.dwh.common.filemanager.FileManager;
import org.bmsource.dwh.common.filemanager.ResourceFileManager;
import org.bmsource.dwh.common.excel.reader.ExcelReader;
import org.bmsource.dwh.common.excel.reader.ExcelRowValidator;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.util.*;

public class ExcelWriterTest {

    private static String SLASH = FileSystems.getDefault().getSeparator();

    private FileManager fileManager = new ResourceFileManager();

    @Test
    public void testExcelWriter() throws Exception {

        ArrayList<String> header = new ArrayList<String>() {{
            add("column1");
            add("column2");
            add("column3");
        }};

        Map<String, List<String>> errors = new HashMap<String, List<String>>() {{
            put("column2", Arrays.asList(new String[]{"should not be null"}));
        }};

        List<DataRow<Fact>> rows = new ArrayList<>();
        rows.add(createRow(new String[]{"column 1 value 1", "column 2 value 1", "value 31"}, null));
        rows.add(createRow(new String[]{"column 1 value 2", "column 2 value 2", "value 32"}, errors));
        rows.add(createRow(new String[]{"column 1 value 3", "column 2 value 3", "value 33"}, null));

        OutputStream outputStream = fileManager.writeStream("1234", "errors.xlsx");
        ExcelWriter<Fact> writer = new ExcelWriter<>(outputStream);
        writer.open();
        writer.writeHeader(header);
        writer.writeRows(header, rows);
        writer.close();

        String tempDir = System.getProperty("java.io.tmpdir");
        File errorFile = new File(tempDir + SLASH + "dwh-test" + SLASH + "1234" + SLASH + "errors.xlsx");
        InputStream actual = FileUtils.openInputStream(errorFile);
        ExcelReader<Fact> reader = new ExcelReader<>(actual);
        Assert.assertArrayEquals(new String[]{"column1", "column2", "column3"}, reader.getHeader().toArray());
        Assert.assertArrayEquals(new String[]{"column 1 value 1", "column 2 value 1", "value 31"}, reader.nextRow().toArray());
        reader.close();
    }

    private DataRow<Fact> createRow(String[] values, Map<String, List<String>> errors) {
        DataRow<Fact> data = new DataRow<>();
        data.setRow(Arrays.asList(values));
        data.setErrors(new ExcelRowValidator.ValidationErrors(errors, errors));
        return data;
    }

    private static class Fact {
    }
}
