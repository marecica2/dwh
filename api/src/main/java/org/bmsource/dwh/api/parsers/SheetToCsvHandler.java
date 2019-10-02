package org.bmsource.dwh.api.parsers;

import java.io.PrintStream;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler;
import org.apache.poi.xssf.usermodel.XSSFComment;

public class SheetToCsvHandler implements SheetContentsHandler {
  private final int minColumns;
  private final PrintStream output;
  private boolean firstCellOfRow;
  private int currentRow = -1;
  private int currentCol = -1;

  SheetToCsvHandler(PrintStream output, int minColumns) {
    this.output = output;
    this.minColumns = minColumns;
  }

  private void outputMissingRows(int number) {
    for (int i=0; i<number; i++) {
      for (int j=0; j<minColumns; j++) {
        output.append(',');
      }
      output.append('\n');
    }
  }

  @Override
  public void startRow(int rowNum) {
    // If there were gaps, output the missing rows
    outputMissingRows(rowNum-currentRow-1);
    // Prepare for this row
    firstCellOfRow = true;
    currentRow = rowNum;
    currentCol = -1;
  }

  @Override
  public void endRow(int rowNum) {
    // Ensure the minimum number of columns
    for (int i=currentCol; i<minColumns; i++) {
      output.append(',');
    }
    output.append('\n');
  }

  @Override
  public void cell(String cellReference, String formattedValue,
      XSSFComment comment) {
    if (firstCellOfRow) {
      firstCellOfRow = false;
    } else {
      output.append(',');
    }

    // gracefully handle missing CellRef here in a similar way as XSSFCell does
    if(cellReference == null) {
      cellReference = new CellAddress(currentRow, currentCol).formatAsString();
    }

    // Did we miss any cells?
    int thisCol = (new CellReference(cellReference)).getCol();
    int missedCols = thisCol - currentCol - 1;
    for (int i=0; i<missedCols; i++) {
      output.append(',');
    }
    currentCol = thisCol;

    // Number or string?
    try {
      //noinspection ResultOfMethodCallIgnored
      Double.parseDouble(formattedValue);
      output.append(formattedValue);
    } catch (NumberFormatException e) {
      output.append('"');
      output.append(formattedValue);
      output.append('"');
    }
  }
}
