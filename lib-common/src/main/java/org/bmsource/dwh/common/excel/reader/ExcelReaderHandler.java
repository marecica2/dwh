package org.bmsource.dwh.common.excel.reader;

import java.util.List;

public interface ExcelReaderHandler<T> {
    default void onStart() {};
    default void onRead(List<T> items) {};
    default void onFinish(int rowCount) {};
    default T transform(T item) { return item; }
}
