package com.example.custom_proxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
@ComponentScan(basePackageClasses = { JacksonProperties.class})
public class CustomProxyApplication {

	public static void main(String[] args) {
		SpringApplication.run(CustomProxyApplication.class, args);
	}

}
