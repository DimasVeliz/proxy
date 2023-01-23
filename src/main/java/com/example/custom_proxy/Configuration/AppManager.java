package com.example.custom_proxy.Configuration;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class AppManager {
    @Value("classpath:proxy.data.json")
    private Resource resource;

    public AppInfo getAppInfo()
    {
        ObjectMapper mapper = new ObjectMapper();
        try {
            var appInfo = mapper.readValue( resource.getFile(),AppInfo.class);
            return appInfo;

        } catch (IOException e) {
           
            e.printStackTrace();
        }
        return null;
    }

}
