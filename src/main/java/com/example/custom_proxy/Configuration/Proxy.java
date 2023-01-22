package com.example.custom_proxy.Configuration;

public class Proxy {
    private String host;
    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }
    private int port;
    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }
    private String protocol;
    public String getProtocol() {
        return protocol;
    }
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}