package com.smartai.common.util;

import lombok.extern.slf4j.Slf4j;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ExcelUtil {

    /**
     * parseExcel2List
     *
     * @param filePath
     * @return
     */
    public static List<Map<String, String>> parseExcel(String filePath) {
        List<Map<String, String>> dataList = new ArrayList<>();
        try {
            InputStream inputStream = new FileInputStream(filePath);
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0); // 获取第一个Sheet

            // 获取表头
            Row headerRow = sheet.getRow(0);
            List<String> headerList = new ArrayList<>();
            for (Cell cell : headerRow) {
                headerList.add(cell.getStringCellValue());
            }

            // 解析数据
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                Map<String, String> dataMap = new HashMap<>();
                for (int j = 0; j < headerList.size(); j++) {
                    String key = headerList.get(j);
                    Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    String value;
                    if (cell.getCellType() == CellType.STRING) {
                        value = cell.getStringCellValue();
                    } else if (cell.getCellType() == CellType.NUMERIC) {
                        value = String.valueOf(cell.getNumericCellValue());
                    } else if (cell.getCellType() == CellType.BOOLEAN) {
                        value = String.valueOf(cell.getBooleanCellValue());
                    } else {
                        value = null;
                    }
                    dataMap.put(key, value);
                }
                dataList.add(dataMap);
            }

            workbook.close();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataList;
    }

}
