package com.smartai.common.util;

import com.huawei.xbenchserver.common.constants.XbenchConstant;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ExcelUtil {
    /**
     * parseExcel2List
     *
     * @param inputStream inputStream
     * @return List<Map < String, Object>>
     */
    public static List<Map<String, String>> parseExcel(InputStream inputStream) {
        List<Map<String, String>> dataList = new ArrayList<>();
        try {
            Workbook workbook = new XSSFWorkbook(inputStream);
            // 获取第一个Sheet
            Sheet sheet = workbook.getSheetAt(0);
            // 获取表头
            Row headerRow = sheet.getRow(0);
            List<String> headerList = new ArrayList<>();
            for (Cell cell : headerRow) {
                headerList.add(cell.getStringCellValue());
            }
            // 解析数据
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                String val1 = getValue(row.getCell(0));
                String val2 = getValue(row.getCell(1));
                String val3 = getValue(row.getCell(2));
                if (StringUtils.isAllEmpty(val1, val2, val3)) {
                    continue;
                }
                Map<String, String> dataMap = new HashMap<>();
                for (int j = 0; j < headerList.size(); j++) {
                    String key = headerList.get(j);
                    Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    String val = getValue(cell);
                    if (StringUtils.isBlank(key) || StringUtils.isBlank(val)) {
                        continue;
                    }
                    dataMap.put(key, val);
                }
                if (!dataMap.isEmpty() && dataMap.size() > 6) {
                    dataList.add(dataMap);
                }
            }
            workbook.close();
            inputStream.close();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return dataList;
    }

    private static String getValue(Cell cell) {
        String value;
        if (cell.getCellType() == CellType.STRING) {
            value = cell.getStringCellValue();
        } else if (cell.getCellType() == CellType.NUMERIC || cell.getCellType() == CellType.FORMULA) {
            double numericCellValue = cell.getNumericCellValue();
            value = new DecimalFormat(XbenchConstant.DOUBLE_FORMAT).format(numericCellValue);
        } else if (cell.getCellType() == CellType.BOOLEAN) {
            value = String.valueOf(cell.getBooleanCellValue());
        } else {
            value = null;
        }
        return value;
    }
}
