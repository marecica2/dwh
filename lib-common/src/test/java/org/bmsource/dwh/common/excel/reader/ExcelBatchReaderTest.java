package org.bmsource.dwh.common.excel.reader;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

public class ExcelBatchReaderTest {

  @Test
  public void testHeaderRowParsing() throws Exception {
    File xlsx = ResourceUtils.getFile(this.getClass().getResource("/spends.xlsx"));
    List<List<Object>> rows = new ExcelBatchReader().readContent(FileUtils.openInputStream(xlsx), 2);
    List<Object> preview = rows.get(0);
    assertThat(preview.size()).isEqualTo(38);
    assertThat(preview.get(4)).isEqualTo("Illinois");
  }

  @Test
  public void testPreviewParsing() throws Exception {
    File xlsx = ResourceUtils.getFile(this.getClass().getResource("/spends.xlsx"));
    List<List<Object>> rows = new ExcelBatchReader().readContent(FileUtils.openInputStream(xlsx), 20);
    assertThat(rows.size()).isEqualTo(20);
  }

  @Test
  public void testBatchParsing() throws Exception {
    File xlsx = ResourceUtils.getFile(this.getClass().getResource("/spends.xlsx"));
    AtomicInteger totalRows = new AtomicInteger();
    FileInputStream inputStream = FileUtils.openInputStream(xlsx);
    new ExcelBatchReader().readContent(inputStream, (rows, header, rowsCount, totalRowsCount) -> {
      totalRows.set(rowsCount);
    });
    assertThat(totalRows.get()).isEqualTo(424);
  }
}
