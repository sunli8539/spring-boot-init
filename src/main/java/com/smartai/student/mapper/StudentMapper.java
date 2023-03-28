package com.smartai.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartai.student.entity.Student;
import com.smartai.student.vo.StudentParamVO;
import com.smartai.student.vo.StudentVO;

import org.apache.ibatis.annotations.Param;

public interface StudentMapper extends BaseMapper<Student> {

    IPage<StudentVO> getPageList(IPage<StudentVO> page, @Param("paramVO") StudentParamVO paramVO);

}
