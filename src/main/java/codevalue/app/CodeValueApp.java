package codevalue.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "codevalue.controllers",
        "codevalue.services",
        "codevalue.config"
})
public class CodeValueApp extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(CodeValueApp.class, args);
    }

}
