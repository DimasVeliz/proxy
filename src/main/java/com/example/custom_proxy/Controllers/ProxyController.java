package com.example.custom_proxy.Controllers;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.Collectors;

import com.example.custom_proxy.Dto.ProxyResponseDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.custom_proxy.Services.Impl.ProxyService;
import com.example.custom_proxy.WebModels.ResourceNotFoundException;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class ProxyController {

    Logger LOGGER = LogManager.getLogger(ProxyController.class);

    ProxyService service;

    @Autowired
    public ProxyController(ProxyService service) {
        this.service = service;
    }

    @GetMapping("/**")
    public ResponseEntity<Object> processGetRequest(HttpServletRequest request) {

        logIncomingRequestInfo(request);


        if(!service.PassesfilterPetition(request))
        {
            throw new ResourceNotFoundException(); 
        }

        var forwardedResponse = service.processGetRequest(request,UUID.randomUUID().toString());
        
        return setResponse(forwardedResponse);
    }
    @PostMapping("/**")
    public ResponseEntity<Object> processPostRequest(@RequestBody String body,HttpServletRequest request) {

        logIncomingRequestInfo(request);


        if(!service.PassesfilterPetition(request))
        {
            throw new ResourceNotFoundException();
        }

        var forwardedResponse = service.processPostRequest(request,body,UUID.randomUUID().toString());

        return setResponse(forwardedResponse);
    }
    @PutMapping("/**")
    public ResponseEntity<Object> processPutRequest(@RequestBody String body, HttpServletRequest request) {

        logIncomingRequestInfo(request);


        if(!service.PassesfilterPetition(request))
        {
            throw new ResourceNotFoundException();
        }

        var forwardedResponse = service.processPutRequest(request,body,UUID.randomUUID().toString());

        return setResponse(forwardedResponse);
    }
    @DeleteMapping("/**")
    public ResponseEntity<Object> processDeleteRequest(HttpServletRequest request) {

        logIncomingRequestInfo(request);


        if(!service.PassesfilterPetition(request))
        {
            throw new ResourceNotFoundException();
        }

        var forwardedResponse = service.processDeleteRequest(request,UUID.randomUUID().toString());

        return setResponse(forwardedResponse);
    }

    private ResponseEntity<Object> setResponse(ProxyResponseDto forwardedResponse) {
        HttpHeaders headers = new HttpHeaders();
        if(forwardedResponse.isBinary()){
            headers.add(HttpHeaders.CONTENT_DISPOSITION,forwardedResponse.getBinaryData().getFileExtension());
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
            return new ResponseEntity<>(forwardedResponse.getBinaryData().getEncodedContent(),headers, HttpStatus.OK);

        }
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return new ResponseEntity<>(forwardedResponse.getJsonData(),headers, HttpStatus.OK);
    }

    private void logIncomingRequestInfo(HttpServletRequest request) {
        String body = "";
        try {
            body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        service.printURL(request);
        service.printQueryString(request);
        service.printHeaders(request);
        service.printDecodedBody(body);
        service.printCookies(request);
    }

}
