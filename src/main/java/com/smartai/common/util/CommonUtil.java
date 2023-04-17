package com.smartai.common.util;

import lombok.extern.slf4j.Slf4j;

import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

@Slf4j
public class CommonUtil {
    private static final String UNKNOWN = "unknown";

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

    /**
     * localDateTimeToTimeStamp
     *
     * @param dateTime dateTime
     * @return timeStamp
     */
    public static long localDateTimeToTimeStamp(LocalDateTime dateTime) {
        Assert.notNull(dateTime, "dateTime can't be null");
        return dateTime.toInstant(ZoneOffset.of("+8")).toEpochMilli();
    }

    /**
     * 获取 IP地址
     * 使用 Nginx等反向代理软件， 则不能通过 request.getRemoteAddr()获取 IP地址
     * 如果使用了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP地址，
     * X-Forwarded-For中第一个非 unknown的有效IP字符串，则为真实IP地址
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }

    /**
     * 根据IP地址查询登录来源
     *
     * @param ip
     * @return
     */
    public static String getCityInfo(String ip) {
        try {
            Searcher searcher = Searcher.newWithFileOnly("src/main/resources/ip2region/ip2region.xdb");
            return searcher.searchByStr(ip);
        } catch (Exception e) {
            log.error("getCityInfo error: {} ", e);
        }
        return "";
    }

}
