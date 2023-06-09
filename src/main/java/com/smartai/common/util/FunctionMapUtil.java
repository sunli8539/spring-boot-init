package com.smartai.common.util;

import com.smartai.student.entity.Student;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class FunctionMapUtil {

    // 函数式接口, 解决过多的if else
    public Map<String, Function<Student, String>> map = new HashMap<>();

    {
        map.put("zhangsan", this::dealZhangsan);
        map.put("wulei", (p) -> dealWulei(p.getStName()));

    }

    private String dealZhangsan(Student student) {
        return "";
    }

    private String dealWulei(String name) {
        return "";
    }

    // 调用
    private String doSomething(Student student) {
        Optional<Function<Student, String>> function = Optional.ofNullable(map.get(student.getStName()));
        if (function.isPresent()) {
            return function.get().apply(student);
        }
        return "";
    }

}
