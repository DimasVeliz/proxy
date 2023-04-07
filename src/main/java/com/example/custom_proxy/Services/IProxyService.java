package com.example.custom_proxy.Services;

import com.example.custom_proxy.Dto.ProxyResponseDto;
import jakarta.servlet.http.HttpServletRequest;

public interface IProxyService {
     ProxyResponseDto processGetRequest(HttpServletRequest request, String trackingID) ;
     ProxyResponseDto processPostRequest(HttpServletRequest request,String body, String trackingID);
     ProxyResponseDto processPutRequest(HttpServletRequest request, String body, String trackingID) ;
     ProxyResponseDto processDeleteRequest(HttpServletRequest request, String trackingID);

}
