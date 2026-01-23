package com.fontal.fonpicturebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.fontal.fonpicturebackend.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class FonPictureBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FonPictureBackendApplication.class, args);
    }

}
