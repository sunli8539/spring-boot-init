package com.smartai.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.annotations.ApiOperation;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;;

/**
 * Knife4j配置类
 *
 * @since 2022-04-14
 */

@Configuration
@EnableOpenApi
public class Knife4jConfig {
    @Value("${knife4j.show:false}")
    private boolean knife4jShow;

    /**
     * 创建knife4j api
     *
     * @return Docket 返回
     */

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.OAS_30).enable(knife4jShow).apiInfo(apiInfo()).select()
            //.apis(RequestHandlerSelectors.basePackage("com.huawei.sbench"))
            //.apis(RequestHandlerSelectors.any())
            .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class)).paths(PathSelectors.any()).build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("辅助工具接口文档").build();
    }

}
