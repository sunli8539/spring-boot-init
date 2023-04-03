
package com.smartai.common.util;

import java.lang.management.ManagementFactory;
import java.util.Locale;

public class SnowFlakeUtil {
    // 开始时间截 (2022-07-07)
    private static final long TWEPOCH = 1657174320335L;

    // 机器id所占的位数
    private static final long WORKERID_BITS = 9L;

    // 数据标识id所占的位数
    private static final long DATACENTERID_BITS = 1L;

    // 支持的最大机器id，结果是512 (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数)
    private static final long MAX_WORKERID = -1L ^ (-1L << WORKERID_BITS);

    // 序列在id中占的位数
    private static final long SEQUENCE_BITS = 12L;

    // 机器ID向左移12位
    private static final long WORKERID_SHIFT = SEQUENCE_BITS;

    // 数据标识id向左移21位(12+9)
    private static final long DATACENTERID_SHIFT = SEQUENCE_BITS + WORKERID_BITS;

    // 时间截向左移22位(1+9+12)
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKERID_BITS + DATACENTERID_BITS;

    // 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095)
    private static final long SEQUENCE_MASK = -1L ^ (-1L << SEQUENCE_BITS);

    // 工作机器ID(0~512)
    private static long workerId;

    // 数据中心ID(默认设为0)
    private static long dataCenterId = 0L;

    // 毫秒内序列(0~4095)
    private static long sequence = 0L;

    // 上次生成ID的时间截
    private static long lastTimestamp = -1L;

    static {
        workerId = getMaxWorkerId(dataCenterId, MAX_WORKERID);
    }

    /**
     * 获取下一个ID
     *
     * @return long ID
     */
    public static synchronized long nextId() {
        long curTimestamp = System.currentTimeMillis();
        if (curTimestamp < lastTimestamp) {
            throw new IllegalArgumentException(
                    String.format(Locale.ROOT, "Clock moved backwards.Refusing to generate id for %d milliseconds",
                            lastTimestamp - curTimestamp));
        }

        if (lastTimestamp == curTimestamp) {
            // 当前毫秒内，则+1
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                // 当前毫秒内计数满了，则等待下一秒
                curTimestamp = getNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = curTimestamp;
        // ID偏移组合生成最终的ID，并返回ID
        return ((curTimestamp - TWEPOCH) << TIMESTAMP_LEFT_SHIFT) | (dataCenterId << DATACENTERID_SHIFT) | (workerId
                << WORKERID_SHIFT) | sequence;
    }

    private static long getNextMillis(final long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    /**
     * 获取 maxWorkerId
     *
     * @param datacenterId 数据中心ID
     * @param maxWorkerId  工作机器ID
     * @return long maxWorkerId
     */
    protected static long getMaxWorkerId(long datacenterId, long maxWorkerId) {
        StringBuilder mpid = new StringBuilder();
        mpid.append(datacenterId);
        String name = ManagementFactory.getRuntimeMXBean().getName();
        if (!name.isEmpty()) {
            /*
             * GET jvmPid
             */
            String[] split = name.split("@");
            if (split == null || split.length < 1) {
                throw new RuntimeException("GET jvmPid exception");
            }
            mpid.append(split[0]);
        }
        /*
         * MAC + PID 的 hashcode 获取16个低位
         */
        return (mpid.toString().hashCode() & 0xffff) % (maxWorkerId + 1);
    }

}
