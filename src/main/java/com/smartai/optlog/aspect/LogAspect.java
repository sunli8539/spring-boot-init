package com.smartai.optlog.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartai.optlog.annotation.OptRecord;
import com.smartai.optlog.service.OptRecordLoader;
import com.smartai.optlog.vo.ResourceOperateRecordVO;

import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
@Aspect
public class LogAspect {
    private static final String USER_KEY = "_w3Fid";

    // @Pointcut("execution(public * com.smartai..service.*.*(..))")
    @Pointcut("@annotation(com.smartai.optlog.annotation.OptRecord)")
    public void record() { }

    @Around(value = "record() && @annotation(optRecord)")
    public Object recordOptLog(ProceedingJoinPoint joinPoint, OptRecord optRecord) throws Throwable {
        OptRecordLoader recordLoader = doLoad();
        if (Objects.isNull(recordLoader)) {
            return joinPoint.proceed();
        } else {
            Object result;
            Object[] args = joinPoint.getArgs();
            ResourceOperateRecordVO recordVO = new ResourceOperateRecordVO();
            if (args == null || args.length == 0) {
                recordVO.setReqParams(new ObjectMapper().writeValueAsString(new Object[0]));
            } else {
                Object[] params = new Object[args.length];
                for (int i = 0 ; i < args.length; i++) {
                    if (args[i] instanceof HttpServletRequest || args[i] instanceof HttpServletResponse ||
                        args[i] instanceof MultipartFile || args[i] instanceof MultipartFile[]) {
                        continue;
                    } else {
                        params[i] = args[i];
                    }
                }
                recordVO.setReqParams(new ObjectMapper().writeValueAsString(params));
            }
            recordVO.setOperator(this.doGetOperator());
            // recordVO.setResourceModule(optRecord.resourceName());
            // recordVO.setFuncName(optRecord.funcName());
            // recordVO.setFuncDesc(optRecord.funcDesc());
            // recordVO.setOperateType(optRecord.operatorType().toString());
            recordVO.setOperateTime(LocalDateTime.now());

            recordVO.setOperateRet("Success");
            try {
                result = joinPoint.proceed();
                recordLoader.receiveMsg(recordVO);
            } catch (Throwable throwable) {
                recordVO.setOperateRet("Failed");
                recordVO.setOperateRetMsg(throwable.getMessage());
                recordLoader.receiveMsg(recordVO);
                throw throwable;
            }
            return result;
        }
    }

    public OptRecordLoader doLoad() {
        ServiceLoader<OptRecordLoader> loaders = ServiceLoader.load(OptRecordLoader.class);
        Iterator<OptRecordLoader> iterator = loaders.iterator();
        while (iterator.hasNext()) {
            OptRecordLoader recordLoader = iterator.next();
            if (Objects.nonNull(recordLoader)) {
                return recordLoader;
            }
        }
        return null;
    }

    private String doGetOperator() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (Objects.isNull(requestAttributes)) {
            return "administrator";
        }
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        if (Objects.isNull(request.getCookies())) {
            return "administrator";
        }
        Optional<String> user = Arrays.stream(request.getCookies())
            .filter(Objects::nonNull)
            .filter(item -> USER_KEY.equals(item.getName()))
            .map(Cookie::getValue).findFirst();
        return user.orElse("administrator");
    }
}


