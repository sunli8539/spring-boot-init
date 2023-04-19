package com.smartai.ip2region.aspect;

import com.smartai.common.util.CommonUtil;
import com.smartai.common.util.HttpUtil;

import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Component
@Aspect
public class IpAspect {

    @Pointcut("@annotation(com.smartai.ip2region.annotation.Ip)")
    public void ip() {
    }

    @Around("ip()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = HttpUtil.getHttpServletRequest();
        String ip = HttpUtil.getIpAddress(request);
        log.info(MessageFormat.format("当前IP为:[{0}], 当前IP地址解析出来的地址为:[{1}]", ip, CommonUtil.getCityInfo(ip)));
        return joinPoint.proceed();
    }
}
