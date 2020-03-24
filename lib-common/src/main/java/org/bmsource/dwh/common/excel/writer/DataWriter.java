package org.bmsource.dwh.common.excel.writer;

import org.bmsource.dwh.common.excel.DataRow;

import java.io.IOException;
import java.util.List;

public interface DataWriter<Fact> extends AutoCloseable {
    void open();

    void writeHeader(List<String> header);

    void writeRows(List<String> header, List<DataRow<Fact>> rows);

    void close() throws IOException;
}
