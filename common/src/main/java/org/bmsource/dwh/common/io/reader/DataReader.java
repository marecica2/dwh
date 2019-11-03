package org.bmsource.dwh.common.io.reader;

import org.apache.poi.ss.usermodel.Cell;

import java.io.IOException;
import java.util.List;

public interface DataReader<Row> extends AutoCloseable {

    List<String> getHeader();

    boolean hasNextRow();

    Row nextRow();

    void close() throws IOException;

    int getTotalRowsCount();

    int getReadRowsCount();
}
