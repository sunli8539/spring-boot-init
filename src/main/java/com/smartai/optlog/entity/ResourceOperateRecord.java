package com.smartai.optlog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("resource_operate_record")
@ApiModel(value = "ResourceOperateRecord对象", description = "")
public class ResourceOperateRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("自增主键")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty("资源模块")
    private String resourceModule;

    @ApiModelProperty("功能名称")
    private String funcName;

    @ApiModelProperty("操作类型：增删改")
    private String operateType;

    @ApiModelProperty("功能描述")
    private String funcDesc;

    @ApiModelProperty("操作人")
    private String operator;

    @ApiModelProperty("请求参数")
    private String reqParams;

    @ApiModelProperty("响应结果")
    private String operateRet;

    @ApiModelProperty("响应结果描述")
    private String operateRetMsg;

    @ApiModelProperty("操作时间")
    private LocalDateTime operateTime;
}
