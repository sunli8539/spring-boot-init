package com.smartai.common.util;

import com.google.common.base.Strings;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

public class HttpUtil {

    private static final String[] headerIps = {
        "X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP",
        "HTTP_X_FORWARDED_FOR"
    };

    private HttpUtil() {
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

    /**
     * 获取远程调用的ip
     *
     * @param request request
     * @return String
     */
    public static String getIpAddress(HttpServletRequest request) {
        String xFor;
        for (String headerIp : headerIps) {
            xFor = request.getHeader(headerIp);
            if (!Strings.nullToEmpty(xFor).trim().isEmpty() && !"unknown".equalsIgnoreCase(xFor)) {
                if (headerIps[0].equals(headerIp)) {
                    int index = xFor.indexOf(",");
                    return index != -1 ? xFor.substring(0, index) : xFor;
                }
                return xFor;
            }
        }
        return request.getRemoteAddr();
    }

}
