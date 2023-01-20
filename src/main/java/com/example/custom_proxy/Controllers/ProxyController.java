package com.example.custom_proxy.Controllers;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
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

    ProxyService service;

    @Autowired
    public ProxyController(ProxyService service) {
        super();
        this.service = service;
    }

    @RequestMapping("/**")
    public ResponseEntity<String> sendRequestToSPM(@RequestBody(required = false) String body,
            HttpMethod method, HttpServletRequest request, HttpServletResponse response)
            throws URISyntaxException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException {

        service.printURL(request);
        service.printQueryString(request);
        service.printHeaders(request);
        service.printDecodedBody(body);
        service.printCookies(request);

        var forwardedResponse = service.processProxyRequest(body, method, request, response,
                UUID.randomUUID().toString());
        return forwardedResponse;
    }

}
