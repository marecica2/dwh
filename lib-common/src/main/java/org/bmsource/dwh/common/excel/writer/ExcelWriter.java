package org.bmsource.dwh.common.excel.writer;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.bmsource.dwh.common.excel.DataRow;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class ExcelWriter<Fact> implements DataWriter<Fact> {

    private int rowNum = 0;

    private int columns = 0;

    private SXSSFWorkbook workBook;

    private SXSSFSheet sheet;

    private OutputStream outputStream;

    private XSSFCellStyle bodyStyle;

    private XSSFCellStyle headerStyle;

    private XSSFCellStyle errorStyle;

    public ExcelWriter(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public void open() {
        workBook = new SXSSFWorkbook();
        workBook.setCompressTempFiles(true);
        sheet = workBook.createSheet("Sheet 1");
        sheet.setRandomAccessWindowSize(1000);
        initStyles();
    }

    @Override
    public void writeHeader(List<String> header) {
        columns = header.size();
        Row row = sheet.createRow(rowNum);
        row.setRowStyle(headerStyle);

        int cellNum = 0;
        for (String value : header) {
            Cell cell = row.createCell(cellNum);
            cell.setCellValue(value);
            cellNum++;
        }
        rowNum++;
    }

    @Override
    public void writeRows(List<String> header, List<DataRow<Fact>> rows) {
        for (DataRow<Fact> excelRow : rows) {
            Row row = sheet.createRow(rowNum);
            row.setRowStyle(bodyStyle);
            int cellNum = 0;
            for (Object value : excelRow.getRow()) {
                Cell cell = row.createCell(cellNum);
                if (excelRow.getErrors() != null && excelRow.getErrors().getColumnErrors() != null && cellNum < header.size()) {
                    List<String> errors = excelRow.getErrors().getColumnErrors().get(header.get(cellNum));
                    if (errors != null) {
                        String errorComment = String.join("\r\n", errors);
                        cell.setCellStyle(errorStyle);
                        setCellComment(cell, errorComment);
                    }
                }
                if (value != null) {
                    cell.setCellValue(value.toString());
                }
                cellNum++;
            }
            rowNum++;
        }
    }

    @Override
    public void close() throws IOException {
        sheet.trackAllColumnsForAutoSizing();
        for (int i = 0; i < columns; i++) {
            sheet.autoSizeColumn(i);
        }
        workBook.write(outputStream);
        workBook.close();
        outputStream.close();
    }

    private static void setCellComment(Cell cell, String message) {
        Drawing drawing = cell.getSheet().createDrawingPatriarch();
        CreationHelper factory = cell.getSheet().getWorkbook()
            .getCreationHelper();
        ClientAnchor anchor = factory.createClientAnchor();
        Comment comment = drawing.createCellComment(anchor);
        RichTextString str = factory.createRichTextString(message);
        comment.setString(str);
        comment.setAuthor("DWH");
        comment.setRow(cell.getRowIndex());
        comment.setColumn(cell.getColumnIndex());
        cell.setCellComment(comment);
    }

    private void initStyles() {
        Font defaultFont = workBook.createFont();
        defaultFont.setFontHeightInPoints((short) 16);
        //defaultFont.setFontName("Arial");
        defaultFont.setColor(IndexedColors.BLACK.getIndex());
        defaultFont.setBold(false);
        defaultFont.setItalic(false);

        Font boldFont = workBook.createFont();
        boldFont.setFontHeightInPoints((short) 14);
        //boldFont.setFontName("Arial");
        boldFont.setColor(IndexedColors.BLACK.getIndex());
        boldFont.setBold(true);
        boldFont.setItalic(false);

        bodyStyle = (XSSFCellStyle) workBook.createCellStyle();
        bodyStyle.setFont(defaultFont);

        headerStyle = (XSSFCellStyle) workBook.createCellStyle();
        headerStyle.setFont(boldFont);

        errorStyle = (XSSFCellStyle) workBook.createCellStyle();
        errorStyle.setFont(boldFont);
        errorStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
        errorStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    }
}
