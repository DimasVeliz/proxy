package com.example.custom_proxy.Configuration;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;

@Configuration
public class AppConfiguration {
    
   
    AppManager appManager;
    
    @Autowired
    public AppConfiguration(AppManager appManager) {
        this.appManager=appManager;
    }

    public int getAppVersion()
    {
        return appManager.getAppInfo()
                         .getVersion();
    }

    public ArrayList<String> getBlackList()
    {
        return appManager.getAppInfo()
                         .getBlacklist();
    }

    
}
