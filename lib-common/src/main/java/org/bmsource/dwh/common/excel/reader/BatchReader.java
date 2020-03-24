package org.bmsource.dwh.common.excel.reader;

import java.io.InputStream;
import java.util.List;

public interface BatchReader {

    void readContent(InputStream inputStream, BatchHandler rowsHandler) throws Exception;

    void readContent(InputStream inputStream, BatchHandler rowsHandler, Integer batchSize) throws Exception;

    List<List<Object>> readContent(InputStream inputStream, int rowsToRead) throws Exception;
}
