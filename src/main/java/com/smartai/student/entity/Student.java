package com.smartai.student.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smartai.common.entity.BaseEntity;

import lombok.Data;

@Data
@TableName("t_student")
public class Student extends BaseEntity {

    @TableField("st_name")
    private String stName;

    @TableField("team")
    private String team;
}
