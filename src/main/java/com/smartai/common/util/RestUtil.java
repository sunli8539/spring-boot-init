package com.smartai.common.util;

import com.alibaba.fastjson.JSONObject;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Slf4j
@Component
public class RestUtil implements ApplicationContextAware {
    private static RestTemplate restTemplate;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (ObjectUtils.isEmpty(applicationContext)) {
            throw new RuntimeException("applicationContext is empty");
        }
        restTemplate = applicationContext.getBean(RestTemplate.class);
    }

    public static <T, R> R post(String url, T data, Class<R> responseType) {
        ResponseEntity<R> responseEntity = invoke(url, data, HttpMethod.POST, responseType);
        if (null == responseEntity.getBody()) {
            throw new RuntimeException("invoke exception");
        }
        return responseEntity.getBody();
    }

    public static <T, R> R get(String url, T data, Class<R> responseType) {
        ResponseEntity<R> responseEntity = invoke(url, data, HttpMethod.GET, responseType);
        if (null == responseEntity.getBody()) {
            throw new RuntimeException("invoke exception");
        }
        return responseEntity.getBody();
    }

    public static <R> R get(String url, Class<R> responseType) {
        return get(url, null, responseType);
    }

    private static <T, R> ResponseEntity<R> invoke(String url, T data, HttpMethod httpMethod, Class<R> responseType) {
        // 此处对请求header进行修饰
        return invoke(url, data, httpMethod, getHttpHeaders(), responseType);
    }

    public static <T, R> ResponseEntity<R> invoke(String url, T data, HttpMethod httpMethod, HttpHeaders headers,
        Class<R> responseType) {
        RequestEntity<T> requestEntity = new RequestEntity<>(data, headers, httpMethod, URI.create(url));
        log.info("RestTemplate request param \nurl:{},\ndata:{},\nhttpMethod:{},\nheaders:{}", url,
            JSONObject.toJSONString(data), httpMethod, headers);
        long startTime = System.currentTimeMillis();
        ResponseEntity<R> exchange = restTemplate.exchange(requestEntity, responseType);
        log.info("RestTemplate response \ncost:{} ms,\nresponse:{}", System.currentTimeMillis() - startTime,
            JSONObject.toJSONString(exchange.getBody()));
        return exchange;
    }

    private static HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

}
