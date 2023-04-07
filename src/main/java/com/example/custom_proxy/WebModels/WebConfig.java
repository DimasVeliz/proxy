package com.example.custom_proxy.WebModels;

import com.example.custom_proxy.Constants.Constants;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class WebConfig {
    public WebClient getBinaryClient() {
        WebClient client = WebClient.builder()
                .baseUrl(Constants.BASE_HOST)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .codecs(codecs -> codecs
                        .defaultCodecs()
                        .maxInMemorySize(5000 * 1024))
                .build();

        return client;
    }

    public WebClient getJSONClient() {
        WebClient client = WebClient.builder()
                .baseUrl(Constants.BASE_HOST)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .codecs(codecs -> codecs
                        .defaultCodecs()
                        .maxInMemorySize(5000 * 1024))
                .build();

        return client;
    }
}