package org.bmsource.dwh.importer.importer.web;

import java.util.List;

public class MappingResult {

  private final List<String> headerRow;
  private final List<Object> previewRow;

  public MappingResult(List<String> headerRow, List<Object> previewRow) {
    this.headerRow = headerRow;
    this.previewRow = previewRow;
  }

  public List<String> getHeaderRow() {
    return headerRow;
  }

  public List<Object> getPreviewRow() {
    return previewRow;
  }
}
