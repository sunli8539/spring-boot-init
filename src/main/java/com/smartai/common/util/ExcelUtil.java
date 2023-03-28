package com.smartai.common.util;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
public class ExcelUtil {

    private static final DecimalFormat decimalFormat = new DecimalFormat("0");

    /**
     * 解析excel
     *
     * @param inputStream 文件流
     * @param sheetNum 页数
     * @param showTitle 是否需要title内容数据  true 需要  false 不需要
     * @return List
     * @throws Exception 未知异常
     */
    public static List<List<Map<String, String>>> parseFile2List(InputStream inputStream, int sheetNum,
        boolean showTitle) throws Exception {
        // 1. 解析数据
        Workbook workbook = parseFile(inputStream);
        List<List<Map<String, String>>> mapList = new LinkedList<>();
        // 可指定传入读取前几个sheet
        int num = Math.min(workbook.getNumberOfSheets(), sheetNum);
        for (int i = 0; i < num; i++) {
            Sheet sheet = workbook.getSheetAt(i);
            // 2. 封装为List
            List<Map<String, String>> maps = parseExcel2List(sheet, showTitle);
            mapList.add(maps);
        }
        return mapList;
    }

    private static Workbook parseFile(InputStream inputStream) throws IOException {
        Workbook workbook = WorkbookFactory.create(inputStream);
        workbook.close();
        int sheetCount = workbook.getNumberOfSheets();
        if (sheetCount < 0) {
            log.info("file sheetCount is : {}", sheetCount);
            throw new IOException("file sheetCount is empty");
        }
        return workbook;
    }

    private static List<Map<String, String>> parseExcel2List(Sheet sheet, boolean showTitle) {
        List<Map<String, String>> mapList = new ArrayList<>();
        // 默认只有一个sheet页  多个sheet用 workBook.getNumberOfSheets() 遍历
        Row row;
        Cell cell;
        row = sheet.getRow(0);
        List<String> titles = new ArrayList<>();
        Map<String, String> titleMap = new HashMap();
        if (row != null) {
            titleMap.put("rowNum", String.valueOf(1));
            titleMap.put("sheetName", sheet.getSheetName());
            for (int x = row.getFirstCellNum(); x < row.getLastCellNum(); x++) {
                cell = row.getCell(x);
                if (cell == null) {
                    titles.add("");
                    titleMap.put(String.valueOf(x), "");
                } else {
                    titles.add(cell.getStringCellValue());
                    titleMap.put(String.valueOf(x), cell.getStringCellValue());
                }
            }
            // 是否需要展示title内容
            if (showTitle) {
                mapList.add(titleMap);
            }
        }
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            row = sheet.getRow(i);
            if (isEmptyRow(row)) {
                // 对应空行是跳过, 而不是报错
                continue;
            }
            Map<String, String> rowMap = new HashMap<>();
            // 不展示title内容 i不增加
            rowMap.put("rowNum", showTitle ? String.valueOf(i + 1) : String.valueOf(i));
            for (int j = row.getFirstCellNum(); j < titles.size(); j++) {
                cell = row.getCell(j);
                String key = titles.get(j);
                String value = getCellStringValue(cell);
                rowMap.put(key, value);
            }
            mapList.add(rowMap);
        }
        return mapList;
    }

    private static Object getCellValue(Cell cell) {
        Object obj;
        switch (cell.getCellType()) {
            case NUMERIC: {
                obj = decimalFormat.format(cell.getNumericCellValue());
                break;
            }
            case BOOLEAN: {
                obj = cell.getBooleanCellValue();
                break;
            }
            case STRING: {
                obj = cell.getStringCellValue();
                break;
            }
            default:
                obj = cell.getRichStringCellValue().length() == 0 ? null : cell.getRichStringCellValue();
        }
        return obj;
    }

    private static String getCellStringValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        Object value = getCellValue(cell);
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        // 如果是double类型，则进行转换
        if (value instanceof Double) {
            Double mainWastage = Double.parseDouble(String.valueOf(value));
            // 取整
            if (mainWastage.intValue() - mainWastage == 0) { // 判断是否符合取整条件
                value = String.valueOf(mainWastage.intValue());
            } else {
                value = String.valueOf(mainWastage);
            }
        }
        return String.valueOf(value).trim();
    }

    private static boolean isEmptyRow(Row row) {
        if (row == null || row.toString().isEmpty()) {
            return true;
        } else {
            Iterator<Cell> it = row.iterator();
            boolean isEmpty = true;
            while (it.hasNext()) {
                Cell cell = it.next();
                if (cell.getCellType() != CellType.BLANK) {
                    isEmpty = false;
                    break;
                }
            }
            return isEmpty;
        }
    }
}
