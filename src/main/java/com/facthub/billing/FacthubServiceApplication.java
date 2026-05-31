package com.facthub.billing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.facthub.billing", "io.github.project.openubl.spring.xsender.runtime"})
public class FacthubServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FacthubServiceApplication.class, args);
    }

}
