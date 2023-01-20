package com.example.custom_proxy.Configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

public class Configuration {
    
    @Autowired
    private Environment env;

    public String getBackEndHost()
    {
        return env.getProperty("backend_host");
    }
    public int getBackEndPort()
    {
        String value = env.getProperty("backend_port");
        return Integer.parseInt(value);
    }
    public String getBackEndProtocol()
    {
        return env.getProperty("backend_protocol");
    }

    public String getProxyHost()
    {
        return env.getProperty("proxy_host");
    }
    public int getProxyPort()
    {
        String value = env.getProperty("proxy_port");
        return Integer.parseInt(value);
    }
    public String getProxyProtocol()
    {
        return env.getProperty("proxy_protocol");
    }
}
