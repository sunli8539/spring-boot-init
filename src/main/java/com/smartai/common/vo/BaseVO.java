package com.smartai.common.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BaseVO {
    /**
     * 当前页
     */
    private Integer pageIndex = 1;

    /**
     * 页面大小
     */
    private Integer pageSize = 10;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
