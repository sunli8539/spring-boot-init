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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class ExcelUtil2 {
    private static final String CHILDREN = "hasChildren";

    private static final String NAME_KEY = "数据名称";

    private static final String SPLIT = "#";

    private static final String TRIM_REGEX = "[　 ]+$";

    /**
     * parseExcel2List
     *
     * @param inputStream inputStream
     * @return List<Map < String, Object>>
     */
    public static List<LinkedHashMap<String, Object>> parseExcel(InputStream inputStream) {
        List<LinkedHashMap<String, Object>> dataList = new ArrayList<>();
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
                LinkedHashMap<String, Object> dataMap = new LinkedHashMap<>();
                for (int j = 0; j < headerList.size(); j++) {
                    String key = headerList.get(j);
                    Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    dataMap.put(key, getValue(cell));
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

    /**
     * 处理值
     */
    private static String getValue(Cell cell) {
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
        return value;
    }

    /**
     * parseData2Map
     *
     * @param dataList
     * @return
     */
    public static LinkedHashMap<String, Object> parseData2Map(List<LinkedHashMap<String, Object>> dataList) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        LinkedHashMap<Integer, Object> spaceNumParentMap = new LinkedHashMap<>();
        for (int i = 1; i < dataList.size(); i++) {
            if (Objects.isNull(dataList.get(i).get(NAME_KEY))) {
                continue;
            }
            LinkedHashMap<String, Object> before = dataList.get(i - 1);
            LinkedHashMap<String, Object> self = dataList.get(i);
            dealData(before, self, result, spaceNumParentMap);
            if (i == 1) {
                result.put(before.get(NAME_KEY).toString().trim(), before);
            }
        }
        return result;
    }

    private static void dealData(LinkedHashMap<String, Object> before, LinkedHashMap<String, Object> self,
        LinkedHashMap<String, Object> result, LinkedHashMap<Integer, Object> spaceNumParentMap) {
        String beforeName = before.get(NAME_KEY).toString();
        String selfName = self.get(NAME_KEY).toString();
        int beforeSpaceNum = getSpaceNum(beforeName);
        int selfSpaceNum = getSpaceNum(selfName);
        // 上一条数据空格少, 则为上一条的子集
        if (beforeSpaceNum < selfSpaceNum) {
            LinkedHashMap<String, Object> children;
            if (Objects.isNull(before.get(CHILDREN))) {
                children = new LinkedHashMap<>();
            } else {
                children = (LinkedHashMap<String, Object>) before.get(CHILDREN);
            }
            children.put(selfName.trim(), self);
            before.put(CHILDREN, children);
            spaceNumParentMap.put(selfSpaceNum, before);
        }
        // 和上一条数据空格一样多, 平级
        if (beforeSpaceNum == selfSpaceNum) {
            // 上一条数据有父集
            if (spaceNumParentMap.containsKey(beforeSpaceNum)) {
                LinkedHashMap<String, Object> parent = (LinkedHashMap<String, Object>) spaceNumParentMap.get(
                    selfSpaceNum);
                LinkedHashMap<String, Object> children = (LinkedHashMap<String, Object>) parent.get(CHILDREN);
                // 解决同级名称重复问题
                children.put(getChildrenKey(beforeName.trim(), selfName.trim()), self);
                parent.put(CHILDREN, children);
                spaceNumParentMap.put(selfSpaceNum, parent);
            } else {
                result.put(selfName.trim(), self);
            }
        }
        // 上一条空格多, 则需重新定位当前行的父集
        if (beforeSpaceNum > selfSpaceNum) {
            if (spaceNumParentMap.containsKey(selfSpaceNum)) {
                LinkedHashMap<String, Object> parent = (LinkedHashMap<String, Object>) spaceNumParentMap.get(
                    selfSpaceNum);
                LinkedHashMap<String, Object> children = (LinkedHashMap<String, Object>) parent.get(CHILDREN);
                // 解决同级名称重复问题
                children.put(getChildrenKey2(children, selfName.trim()), self);
                parent.put(CHILDREN, children);
                spaceNumParentMap.put(selfSpaceNum, parent);
            } else {
                result.put(selfName.trim(), self);
            }
        }
    }

    private static int getSpaceNum(String str) {
        if (StringUtils.isBlank(str)) {
            return 0;
        }
        str = trimRight(str);
        int num = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ' ') {
                num++;
            }
        }
        return num;
    }

    private static String getChildrenKey(String beforeName, String selfName) {
        StringBuilder buffer = new StringBuilder();
        if (!beforeName.contains(SPLIT) && StringUtils.equals(beforeName, selfName)) {
            return buffer.append(selfName).append(SPLIT).append(0).toString();
        }
        if (beforeName.contains(SPLIT) && StringUtils.equals(beforeName.split(SPLIT)[0], selfName)) {
            return buffer.append(selfName).append(SPLIT).append(getNextIndex(beforeName.split(SPLIT)[1])).toString();
        }
        return selfName;
    }

    private static String getChildrenKey2(LinkedHashMap<String, Object> children, String selfName) {
        StringBuilder buffer = new StringBuilder();
        List<String> keys = new ArrayList<>();
        for (Map.Entry<String, Object> entry : children.entrySet()) {
            String key = entry.getKey();
            boolean con1 = StringUtils.equals(key, selfName);
            boolean con2 = key.contains(SPLIT) && StringUtils.equals(selfName, key.split(SPLIT)[0]);
            // 拿到所有重名的 key
            if (con1 || con2) {
                keys.add(key);
            }
        }
        if (keys.size() == 0) {
            return selfName;
        }
        if (keys.size() == 1) {
            return buffer.append(selfName).append(SPLIT).append(0).toString();
        }
        String index = keys.get(keys.size() - 1).split(SPLIT)[1];
        return buffer.append(selfName).append(SPLIT).append(getNextIndex(index)).toString();
    }

    private static int getNextIndex(String input) {
        if (StringUtils.isBlank(input)) {
            return 0;
        }
        int tmp = Integer.parseInt(input);
        return tmp + 1;
    }

    public static String trimRight(String str) {
        if (str == null || str.equals("")) {
            return str;
        } else {
            return str.replaceAll(TRIM_REGEX, "");
        }
    }
}
