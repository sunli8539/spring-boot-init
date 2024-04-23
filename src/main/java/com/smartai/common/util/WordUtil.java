package com.smartai.common.util;

import com.spire.doc.Document;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * @author sWX1160681
 * @version [版本号，2024/3/5]
 * @description
 * @see [相关类/方法]
 * @since 2024/3/5 - 14:28
 **/
@Slf4j
public class WordUtil {

    /**
     * 解析word表格
     *
     * @param is
     * @return
     * @throws IOException
     */
    public static List<Map<String, String>> readTables(InputStream is) {
        List<Map<String, String>> result = new ArrayList<>();
        try (XWPFDocument document = new XWPFDocument(is)) {
            for (XWPFTable table : document.getTables()) {
                if (table.getText().contains("详细测试步骤")) {
                    List<List<String>> rs = readTable(table);
                    result.add(parseTableVal(rs));
                }
            }
            // 附件excel
            for (POIXMLDocumentPart part : document.getRelations()) {
                String partName = part.getPackagePart().getPartName().getName();
                if (partName.endsWith("xlsx") || partName.endsWith("xls")) {
                    List<Map<String, String>> dataList = ExcelUtil.parseExcel(part.getPackagePart().getInputStream());
                    result.addAll(dataList);
                }
            }
        } catch (Exception e) {
            log.error("WordUtil.readTables error {}", e);
        } finally {
            IOUtils.closeQuietly(is);
        }
        return result;
    }

    /**
     * 读取解析表格
     *
     * @param table
     * @return
     */
    public static List<List<String>> readTable(XWPFTable table) {
        List<List<String>> tableData = new ArrayList<>();
        List<XWPFTableRow> rows = table.getRows();
        for (XWPFTableRow row : rows) {
            List<XWPFTableCell> cells = row.getTableCells();
            List<String> rowData = new ArrayList<>();
            for (int j = 0; j < cells.size(); j++) {
                XWPFTableCell cell = cells.get(j);
                String cellText = cell.getText();
                // Handle merged cells
                if (cellText.isEmpty() && cell.getCTTc().getTcPr().isSetGridSpan()) {
                    int span = cell.getCTTc().getTcPr().getGridSpan().getVal().intValue();
                    StringBuilder mergedCellText = new StringBuilder();
                    for (int k = 0; k < span; k++) {
                        if (j + k < cells.size()) {
                            mergedCellText.append(cells.get(j + k).getText());
                            if (k < span - 1) {
                                mergedCellText.append(" ");
                            }
                        }
                    }
                    cellText = mergedCellText.toString();
                    j += span - 1;
                }
                rowData.add(cellText);
            }
            if (!CollectionUtils.isEmpty(rowData)) {
                tableData.add(rowData);
            }
        }
        return tableData;
    }

    /**
     * 处理合并行
     *
     * @param table
     * @return
     */
    private static Map<String, String> parseTableVal(List<List<String>> table) {
        Map<String, String> contents = new HashMap<>();
        for (int i = 0; i < table.size(); i++) {
            List<String> row = table.get(i);
            String rowKey = null, rowVal = null;
            for (int j = 0; j < row.size(); j++) {
                if (row.size() == 3) {
                    List<String> pre = table.get(i - 1);
                    if (pre.size() == 2) {
                        rowKey = row.get(0).concat("-").concat(row.get(1));
                        rowVal = row.get(2);
                    } else {
                        if (StringUtils.isBlank(row.get(0))) {
                            row.set(0, pre.get(0));
                        }
                        if (StringUtils.isBlank(row.get(2))) {
                            row.set(2, pre.get(2));
                        }
                        rowKey = row.get(0).concat("-").concat(row.get(1));
                        rowVal = row.get(2);
                    }

                } else {
                    rowKey = row.get(0);
                    rowVal = row.get(1);
                }
            }
            contents.put(rowKey, rowVal);
        }
        return dealPreconditions(contents);
    }

    private static Map<String, String> dealPreconditions(Map<String, String> map) {
        String preconditions = "";
        Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            if (entry.getKey().contains("测试前置条件")) {
                String tmpKey = entry.getKey().split("-")[1];
                String val = tmpKey.concat(entry.getValue());
                preconditions = preconditions.concat(val).concat("\n");
                iterator.remove();
            }
        }
        if (StringUtils.isNoneBlank(preconditions)) {
            map.put("测试前置条件", preconditions);
        }
        return map;
    }

    /**
     * 比对word文档内容差异
     */
    public static void compareWordContent(String sourcefilePath, String targetFilePath, String outputPath) {
        Document doc1 = new Document();
        Document doc2 = new Document();
        doc1.loadFromFile(sourcefilePath);
        doc2.loadFromFile(targetFilePath);
        doc1.compare(doc2, "admin");
        doc1.saveToFile(outputPath);
        doc1.dispose();
        doc2.dispose();
    }

}
