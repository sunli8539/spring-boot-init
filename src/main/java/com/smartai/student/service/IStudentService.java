package com.smartai.student.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.smartai.student.entity.Student;
import com.smartai.student.vo.StudentParamVO;
import com.smartai.student.vo.StudentVO;

public interface IStudentService extends IService<Student> {

    IPage<StudentVO> getPageList(StudentParamVO paramVO);

}
