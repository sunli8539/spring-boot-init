package com.smartai.student.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.smartai.optlog.enums.OptType;
import com.smartai.common.vo.CommonResponseVO;
import com.smartai.student.entity.Student;
import com.smartai.student.service.IStudentService;
import com.smartai.student.vo.StudentParamVO;
import com.smartai.optlog.annotation.OptRecord;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

@Api("/student")
@RestController
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private IStudentService studentService;

    @PostMapping("/getPageList")
    @ApiOperation("列表")
    @OptRecord(resourceName = "学生管理", funcName = "列表", operatorType = OptType.READ, funcDesc = "查询学生列表信息")
    public CommonResponseVO getPageList(@RequestBody StudentParamVO paramVO) {
        return CommonResponseVO.buildSuccess(studentService.getPageList(paramVO));
    }

    @PostMapping("/getDetailById")
    @ApiOperation("详情")
    @OptRecord(resourceName = "学生管理", funcName = "详情", operatorType = OptType.READ, funcDesc = "查询学生详情信息")
    public CommonResponseVO getDetail(@RequestBody StudentParamVO paramVO) {
        return CommonResponseVO.buildSuccess(studentService.getById(paramVO.getId()));
    }

    @PostMapping("/updateById")
    @ApiOperation("更新")
    @OptRecord(resourceName = "学生管理", funcName = "更新", operatorType = OptType.UPDATE, funcDesc = "更新学生信息")
    public CommonResponseVO updateById(@RequestBody StudentParamVO paramVO) {
        Student student = new Student();
        BeanUtils.copyProperties(paramVO, student);
        return CommonResponseVO.buildSuccess(
            studentService.update(student, new QueryWrapper<Student>().lambda().eq(Student::getId, paramVO.getId())));
    }
}
