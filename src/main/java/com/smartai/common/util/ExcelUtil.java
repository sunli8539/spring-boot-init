package com.smartai.common.util;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class ExcelUtil {
    private static final String CHILDREN = "hasChildren";

    private static final String NAME_KEY = "数据名称";

    /**
     * parseExcel2List
     *
     * @param inputStream
     * @return
     */
    public static List<Map<String, Object>> parseExcel(InputStream inputStream) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        try {
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
                Map<String, Object> dataMap = new HashMap<>();
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
            log.error(e.getMessage());
        }
        return dataList;
    }

    public static Map<String, Object> parseData2Map(List<Map<String, Object>> dataList) {
        Map<String, Object> result = new HashMap<>();
        Map<Integer, Object> spaceNumParentMap = new HashMap<>();
        for (int i = 1; i < dataList.size(); i++) {
            if (Objects.isNull(dataList.get(i).get(NAME_KEY))) {
                continue;
            }
            Map<String, Object> before = dataList.get(i - 1);
            Map<String, Object> self = dataList.get(i);

            String beforeName = before.get(NAME_KEY).toString();
            String selfName = self.get(NAME_KEY).toString();

            int beforeSpaceNum = getSpaceNum(beforeName);
            int selfSpaceNum = getSpaceNum(selfName);

            // 和上一条数据空格一样多, 平级   || 上一条空格多, 则需重新定位当前行的父集
            if (beforeSpaceNum == selfSpaceNum || beforeSpaceNum > selfSpaceNum) {
                // 上一条数据有父集
                if (spaceNumParentMap.containsKey(selfSpaceNum)) {
                    Map<String, Object> parent = (Map<String, Object>) spaceNumParentMap.get(selfSpaceNum);
                    Map<String, Object> children = (Map<String, Object>) parent.get(CHILDREN);
                    children.put(selfName.trim(), self);
                    parent.put(CHILDREN, children);
                    spaceNumParentMap.put(selfSpaceNum, parent);
                } else {
                    result.put(selfName.trim(), self);
                }
            }
            // 比上一条数据空格多, 则为上一条的子集
            if (beforeSpaceNum < selfSpaceNum) {
                Map<String, Object> children;
                if (Objects.isNull(before.get(CHILDREN))) {
                    children = new HashMap<>();
                } else {
                    children = (Map<String, Object>) before.get(CHILDREN);
                }
                children.put(selfName.trim(), self);
                before.put(CHILDREN, children);
                spaceNumParentMap.put(selfSpaceNum, before);
            }
            if (i == 1) {
                result.put(beforeName.trim(), before);
            }
        }
        return result;

    }

    private static int getSpaceNum(String str) {
        if (StringUtils.isBlank(str)) {
            return 0;
        }
        int num = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ' ') {
                num++;
            }
        }
        return num;
    }
}
