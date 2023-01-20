package com.example.custom_proxy.Configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class AppConfiguration {
    
    private Environment env;
    
    @Autowired
    public AppConfiguration(Environment env) {
        this.env=env;
    }

    public String getBackEndHost()
    {
        var value = env.getProperty("backend_host");
        return value;
    }
    public int getBackEndPort()
    {
        String value = env.getProperty("backend_port");
        return Integer.parseInt(value);
    }
    public String getBackEndProtocol()
    {
        var value = env.getProperty("backend_protocol");
        return value;
    }

    public String getProxyHost()
    {
        var value = env.getProperty("proxy_host");
        return value;
    }
    public int getProxyPort()
    {
        String value = env.getProperty("proxy_port");
        return Integer.parseInt(value);
    }
    public String getProxyProtocol()
    {
        var value = env.getProperty("proxy_protocol");
        return value;
    }
}
