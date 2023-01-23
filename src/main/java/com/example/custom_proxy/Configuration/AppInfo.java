package com.example.custom_proxy.Configuration;

import java.util.ArrayList;

public class AppInfo {
    private int version;
    public int getVersion() {
        return version;
    }
    public void setVersion(int version) {
        this.version = version;
    }
    private ArrayList<String> blacklist;
    public ArrayList<String> getBlacklist() {
        return blacklist;
    }
    public void setBlacklist(ArrayList<String> blacklist) {
        this.blacklist = blacklist;
    }
    
    
}
