package org.bmsource.dwh.common.excel.reader;

import java.util.List;

public interface BatchHandler {

    default void onStart() { }

    void onRead(List<List<Object>> rows, List<String> header, int rowsCount, int totalRowsCount) throws InterruptedException;

    default void onFinish(int totalRowsCount) { }
}
