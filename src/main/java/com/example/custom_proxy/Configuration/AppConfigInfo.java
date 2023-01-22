package com.example.custom_proxy.Configuration;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;



@Component
@PropertySource(value = "src/main/resources/:application.properties.json")
@ConfigurationProperties
public class AppConfigInfo {

    public Server server;
    public Frontend frontend;
    public HashMap<String, ApiDetails> apisInfo;
}
