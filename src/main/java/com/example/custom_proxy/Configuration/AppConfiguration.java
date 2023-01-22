package com.example.custom_proxy.Configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfiguration {

    private AppConfigInfo configInfo;

    @Autowired
    public AppConfiguration(AppConfigInfo configInfo) {
        this.configInfo = configInfo;

    }

    public AppConfigInfo getConfigInfo() {
        return configInfo;
    }

    public boolean hasAPI(String keyToCheck) {
        return getConfigInfo().apisInfo.containsKey(keyToCheck);
    }

    public ApiDetails buildAPIInfo(String key) {
        return getConfigInfo().apisInfo.get(key);
    }

}
