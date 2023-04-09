package com.example.custom_proxy.Models;

import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public class RequestResources {
    private boolean isBinary;
    private String uri;
    private Mono<String> body;
    MultiValueMap<String, String> paramInfo;
    private String base;

    public RequestResources(String uri, Mono<String> bodyMono, MultiValueMap<String, String> queryParams,String base) {

        this.uri = uri;
        this.body = bodyMono;
        this.paramInfo = queryParams;
        this.base=base;
    }


    public void setUri(String uri) {
        this.uri = uri;
    }

    public Mono<String> getBody() {
        return body;
    }

    public void setBody(Mono<String> body) {
        this.body = body;
    }

    public boolean isBinary() {
        return isBinary;
    }

    public void setBinary(boolean isBinary) {
        this.isBinary = isBinary;
    }

    public String getUri() {
        return uri;
    }

    public MultiValueMap<String, String> getParamInfo() {
        return paramInfo;
    }

    public void setParamInfo(MultiValueMap<String, String> paramInfo) {
        this.paramInfo = paramInfo;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }
}
