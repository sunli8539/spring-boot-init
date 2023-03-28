package com.smartai.optlog.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResourceOperateRecordVO {
    private String resourceModule;
    private String funcName;
    private String operateType;
    private String funcDesc;
    private String operator;
    private String reqParams;
    private String operateRet;
    private String operateRetMsg;
    private LocalDateTime operateTime;
}
