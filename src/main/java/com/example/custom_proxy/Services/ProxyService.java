package com.example.custom_proxy.Services;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Recover;

import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import org.springframework.retry.annotation.Backoff;

@Service
public class ProxyService {

    String domain = "127.0.0.1";
    Integer puerto = 8081;
    String protocolToUse = "http";

    public ProxyService() {
    }

    @Retryable(exclude = {HttpStatusCodeException.class }, 
                include = Exception.class, 
                backoff = @Backoff(delay = 5000, multiplier = 4.0), maxAttempts = 4)
    public ResponseEntity<String> processProxyRequest(String body,
            HttpMethod method, HttpServletRequest request, HttpServletResponse response, String traceId)
            throws URISyntaxException {
        
        ThreadContext.put("traceId", traceId);
        String requestUrl = request.getRequestURI();

       
        URI uri = new URI(protocolToUse, null, domain, puerto, null, null, null);

        // replacing context path form urI to match actual gateway URI
        uri = UriComponentsBuilder.fromUri(uri)
                .path(requestUrl)
                .query(request.getQueryString())
                .build(true).toUri();

        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.set(headerName, request.getHeader(headerName));
        }

        headers.set("TRACE", traceId);
        headers.remove(HttpHeaders.ACCEPT_ENCODING);

        HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);
        ClientHttpRequestFactory factory = new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory());
        RestTemplate restTemplate = new RestTemplate(factory);
        try {

            ResponseEntity<String> serverResponse = restTemplate.exchange(uri, method, httpEntity, String.class);
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.put(HttpHeaders.CONTENT_TYPE, serverResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
           
            return serverResponse;

        } catch (HttpStatusCodeException e) {
            
            return ResponseEntity.status(e.getStatusCode())
                    .headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAsString());
        }

    }

    @Recover
    public ResponseEntity<String> recoverFromRestClientErrors(Exception e, String body,
            HttpMethod method, HttpServletRequest request, HttpServletResponse response, String traceId) {
        System.out.println("retry method for the following url " + request.getRequestURI() + " has failed" + e.getMessage());
        System.out.println(e.getStackTrace());
        throw new RuntimeException("There was an error trying to process you request. Please try again later");

    }

    public void printURL(HttpServletRequest request) {
        System.out.println("URL TO VISIT :");

        String uriToVISIT = request.getRequestURL().toString();
        System.out.println(uriToVISIT);
    }

    public void printHeaders(HttpServletRequest request) {
        Iterator<String> locura = request.getHeaderNames().asIterator();

        Dictionary<String, String> dict = new Hashtable<String, String>();
        HttpHeaders headers = new HttpHeaders();

        System.out.println("HEADERS GOTTEN: ");

        while (locura.hasNext()) {
            String key = locura.next();
            String value = request.getHeader(key);
            System.out.println(key + " " + value);

            dict.put(key, value);
            headers.add(key, value);
        }

    }

    public void printQueryString(HttpServletRequest request) {
        var queryString = request.getQueryString();

        System.out.println("QUERY STRING GOTTEN: ");
        System.out.println(queryString);
    }

    public void printDecodedBody(String body) {        
        System.out.println(body);
    }

}
