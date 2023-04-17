package com.smartai.common.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

public class HttpContextUtil {

    private HttpContextUtil() {
    }

    public static HttpServletRequest getHttpServletRequest() {
        return ((ServletRequestAttributes) Objects.requireNonNull(
            RequestContextHolder.getRequestAttributes())).getRequest();
    }

    public static String getOrigin() {
        HttpServletRequest httpServletRequest = getHttpServletRequest();
        String origin = httpServletRequest.getHeader("Origin");
        System.err.println(origin);
        return origin;
    }

}
