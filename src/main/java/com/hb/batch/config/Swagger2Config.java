package com.hb.batch.config;

import io.swagger.annotations.Api;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ========== swagger2配置，详见http://localhost:8080/swagger-ui.html ==========
 *
 * @author Mr.huang
 * @version com.hb.web.config.Swagger2Config.java, v1.0
 * @date 2019年06月14日 16时08分
 */
@Configuration
@EnableSwagger2
public class Swagger2Config {

    @Bean
    public Docket createRestApi() {
        Set<String> protocols = new HashSet<>();
        protocols.add("http");
        return new Docket(DocumentationType.SWAGGER_2)
                .securitySchemes(securitySchemes())
                .protocols(protocols)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
                .paths(PathSelectors.any())
                .build()
                .useDefaultResponseMessages(false);
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Spring Boot中使用Swagger2构建RESTful APIs")
                .description("更多Spring Boot相关文章请关注：https://github.com/191059014")
                .termsOfServiceUrl("https://github.com/191059014")
                .version("1.0")
                .build();
    }

    private List<ApiKey> securitySchemes() {
        ArrayList<ApiKey> arrayList = new ArrayList<>();
        arrayList.add(new ApiKey("isEncrypt", "isEncrypt", "header"));
        arrayList.add(new ApiKey("token", "token", "header"));
        return arrayList;
    }


}
