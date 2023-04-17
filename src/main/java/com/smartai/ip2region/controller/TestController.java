package com.smartai.ip2region.controller;

import com.smartai.ip2region.annotation.Ip;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ip")
public class TestController {

    @Ip
    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }
}
