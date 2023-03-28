package com.smartai.common.parallel;

import lombok.Data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

@Data
public class ExecutionLog {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionLog.class);

    /**
     * 日志类型-任务执行前
     */
    public static final String TYPE_BEGIN = "begin";

    /**
     * 日志类型-任务执行后
     */
    public static final String TYPE_END = "end";

    private Integer id;

    /* ip */
    private String ip;

    /* 线程池名称 */
    private String executorName;

    /* 容器名称 */
    private String containerName;

    private Integer poolSize;

    private Integer activeThreads;

    private Long completedTasks;

    private Integer coreSize;

    private Integer maxSize;

    private Integer timeout;

    private Integer queueCapatity;

    private String externInfo;

    private Date createTime;

    private Integer queuedTasks;

    private String taskName;

    private String threadName;

    private String type;

    public Date getCreateTime() {
        if (createTime == null) {
            return null;
        }
        return (Date) createTime.clone();
    }

    public void setCreateTime(Date createTime) {
        if (createTime != null) {
            try {
                this.createTime = (Date) createTime.clone();
            } catch (ClassCastException exception) {
                LOGGER.error("ClassCastException:" + exception.getMessage());
                throw new RuntimeException(exception.getMessage());
            }
        } else {
            this.createTime = null;
        }
    }
}
