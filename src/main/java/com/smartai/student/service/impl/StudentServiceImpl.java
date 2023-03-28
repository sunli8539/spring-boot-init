package com.smartai.student.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartai.student.entity.Student;
import com.smartai.student.mapper.StudentMapper;
import com.smartai.student.service.IStudentService;
import com.smartai.student.vo.StudentParamVO;
import com.smartai.student.vo.StudentVO;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements IStudentService {

    @Resource
    private StudentMapper studentMapper;

    @Override
    public IPage<StudentVO> getPageList(StudentParamVO paramVO) {
        IPage<StudentVO> page = new Page<>(paramVO.getPageIndex(), paramVO.getPageSize());
        return studentMapper.getPageList(page, paramVO);
    }
}
