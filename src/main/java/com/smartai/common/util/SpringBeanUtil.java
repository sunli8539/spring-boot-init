package com.smartai.common.util;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class SpringBeanUtil implements ApplicationContextAware, EnvironmentAware {
    private static ApplicationContext context;

    private static Environment env;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    @Override
    public void setEnvironment(Environment environment) {
        env = environment;
    }

    /**
     * 获取bean
     *
     * @param clazz Class<T>
     * @return T
     */
    public static <T> T getBean(Class<T> clazz) {
        try {
            return context.getBean(clazz);
        } catch (BeansException e) {
            log.error("getBean exception {}", e);
            throw new RuntimeException("getBean error");
        }

    }

    /**
     * 获取bean
     *
     * @param beanName beanName
     * @return T
     */
    public static <T> T getBean(String beanName) {
        try {
            return (T) context.getBean(beanName);
        } catch (BeansException e) {
            log.error("getBean exception {}", e);
            throw new RuntimeException("getBean error");
        }
    }

    /**
     * 获取bean
     *
     * @param clazz Class<T>
     * @return Map<String, T>
     */
    public static <T> Map<String, T> getBeanOfType(Class<T> clazz) {
        try {
            return context.getBeansOfType(clazz);
        } catch (BeansException e) {
            log.error("getBean exception {}", e);
            throw new RuntimeException("getBean error");
        }
    }

    /**
     * 获取配置信息
     *
     * @param key 配置key
     * @return String
     */
    public static String getEnvConf(String key) {
        try {
            return env.getProperty(key);
        } catch (BeansException e) {
            log.error("getEnvConf exception {}", e);
            throw new RuntimeException("getEnvConf error");
        }
    }

}
