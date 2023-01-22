package com.example.custom_proxy.Services;

import com.example.custom_proxy.Configuration.ApiDetails;

public class ResolverInfo {

    private boolean IsAvailable;
    private ApiDetails apiDetails;

    public void setApiDetails(ApiDetails value) {
        this.apiDetails = value;
    }

    public ApiDetails getApiDetails() {
        return apiDetails;
    }

    public boolean isIsAvailable() {
        return IsAvailable;
    }

    public void setIsAvailable(boolean value) {
        IsAvailable = value;
    }

}
