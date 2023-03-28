package com.smartai.common.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommonUtil {

    /**
     * 切分list，用于分批次插入sub表，一次batchSize
     *
     * @param list 初始list
     * @param batchSize 切分的数量
     * @return 分页后的list
     */
    public static Map<Integer, List> splitList(List list, int batchSize) {
        Map<Integer, List> itemMap = new HashMap<>();
        itemMap.put(1, new ArrayList<>());
        for (Object el : list) {
            List batchList = itemMap.get(itemMap.size());
            // 当list满足批次数量，新建一个list存放后面的数据
            if (batchList.size() == batchSize) {
                batchList = new ArrayList<>();
                itemMap.put(itemMap.size() + 1, batchList);
            }
            batchList.add(el);
        }
        return itemMap;
    }
}
