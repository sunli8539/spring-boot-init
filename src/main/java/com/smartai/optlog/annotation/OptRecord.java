package com.smartai.optlog.annotation;

import com.smartai.optlog.enums.OptType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OptRecord {
    /**
     *  资源名称
     *
     * @return 资源名称
     */
    String resourceName();

    /**
     *  功能名称
     *
     * @return 功能名称
     */
    String funcName();

    /**
     *  操作类型
     *
     * @return OptType
     */
    OptType operatorType() default OptType.CREATE;

    /**
     *  功能描述
     *
     * @return 功能描述
     */
    String funcDesc();
}
