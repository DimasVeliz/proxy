package com.example.custom_proxy.Controllers;

import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.custom_proxy.Services.ProxyService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
public class ProxyController {

    @Autowired
    ProxyService service;
    public ProxyController(ProxyService service) {
        super();
        this.service=service;
    }
    
    @RequestMapping("/**")
    public ResponseEntity<String> sendRequestToSPM(@RequestBody(required = false) String body,
                                                   HttpMethod method, HttpServletRequest request, HttpServletResponse response)
            throws URISyntaxException {
        
        service.printURL(request);
        service.printQueryString(request);
        service.printHeaders(request);
        service.printDecodedBody(body);        
        

        
        return service.processProxyRequest(body,method,request,response, UUID.randomUUID().toString());
    }
    
}
