package com.example.custom_proxy.Services.Impl;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.SSLContext;

import com.example.custom_proxy.Dto.BinaryDto;
import com.example.custom_proxy.Dto.ProxyResponseDto;
import com.example.custom_proxy.Models.RequestResources;
import com.example.custom_proxy.Services.IProxyService;
import com.example.custom_proxy.WebModels.ProxyServiceException;
import com.example.custom_proxy.WebModels.WebConfig;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.client5.http.ssl.TrustSelfSignedStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.custom_proxy.Configuration.AppConfiguration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import reactor.core.publisher.Mono;

@Service
public class ProxyService implements IProxyService {

    AppConfiguration configuration;
    private WebConfig config;

    @Autowired
    public ProxyService(AppConfiguration config, WebConfig webconfig) {
        this.configuration = config;
        this.config = webconfig;
    }

    @Retryable(exclude = {
            HttpStatusCodeException.class }, include = Exception.class, backoff = @Backoff(delay = 5000, multiplier = 4.0), maxAttempts = 4)
    public ResponseEntity<String> processProxyRequest(String body,
            HttpMethod method, HttpServletRequest request, HttpServletResponse response, String traceId)
            throws URISyntaxException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException {

        ThreadContext.put("traceId", traceId);

        URI uri = this.buildURI(request);
        HttpHeaders headers = loadHeaders(request);
        SetAdditionalHeaders(headers, traceId);
        RemoveExtraHeaders(headers);

        HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);

        CloseableHttpClient httpClient = buildClient(request);

        HttpComponentsClientHttpRequestFactory clientrequestFactory = new HttpComponentsClientHttpRequestFactory();

        clientrequestFactory.setHttpClient(httpClient);

        RestTemplate restTemplate = new RestTemplate(clientrequestFactory);

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

    private CloseableHttpClient buildClient(HttpServletRequest request) {
        // version 5.0
        try {
            final SSLContext sslcontext = this.configureSSLContext(request);

            final SSLConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactoryBuilder.create()
                    .setSslContext(sslcontext)
                    .build();
            final HttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
                    .setSSLSocketFactory(sslSocketFactory)
                    .build();
            return HttpClients.custom()
                    .setConnectionManager(cm)
                    .evictExpiredConnections()
                    .build();

        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {

        }
        return null;
    }

    private String buildProtocol(HttpServletRequest request) {
        var scheme = request.getScheme();
        if (scheme.contains("https"))
            return "https";
        return "http";
    }

    public SSLContext configureSSLContext(HttpServletRequest request)
            throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        String protocolToUse = buildProtocol(request);

        if (!protocolToUse.equals("https")) {
            return SSLContexts.custom()
                    .loadTrustMaterial(null, new TrustAllStrategy())
                    .build();
        }
        SSLContext sslContext = new SSLContextBuilder()
                .loadTrustMaterial(null, new TrustSelfSignedStrategy())
                .build();
        return sslContext;
    }

    private void RemoveExtraHeaders(HttpHeaders headers) {
        headers.remove(HttpHeaders.ACCEPT_ENCODING);
    }

    private void SetAdditionalHeaders(HttpHeaders headers, String traceId) {
        headers.set("TRACE", traceId);
    }

    private HttpHeaders loadHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.set(headerName, request.getHeader(headerName));
        }

        return headers;
    }

    private URI buildURI(HttpServletRequest request) throws URISyntaxException {

        String resourcePath = request.getRequestURI();
        String protocolToUse = buildProtocol(request);

        String domain = request.getServerName();
        int port = request.getServerPort();

        URI uri = new URI(protocolToUse, null, domain, port, null, null, null);

        // replacing context path form urI to match actual gateway URI
        uri = UriComponentsBuilder.fromUri(uri)
                .path(resourcePath)
                .query(request.getQueryString())
                .build(true).toUri();
        return uri;
    }

    @Recover
    public ResponseEntity<String> recoverFromRestClientErrors(Exception e, String body,
            HttpMethod method, HttpServletRequest request, HttpServletResponse response, String traceId) {
        System.out.println(
                "retry method for the following url " + request.getRequestURI() + " has failed" + e.getMessage());
        System.out.println(e.getStackTrace());
        throw new RuntimeException("There was an error trying to process you request. Please try again later");

    }

    // Extra, for printing info

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
        System.out.println("Body GOTTEN: ");
        System.out.println(body);
    }

    public void printCookies(HttpServletRequest request) {
        var cookies = request.getCookies();
        System.out.println("Cookies GOTTEN: ");
        if (cookies == null) {
            System.out.println("-");
            return;
        }
        for (var cookie : cookies) {
            System.out.println(cookie);
        }
    }

    public boolean PassesfilterPetition(HttpServletRequest request) {
        String domain = request.getRequestURL().toString();
        var blackList = configuration.getBlackList();
        if (blackList.contains(domain))
            return false;
        return true;
    }

    private RequestResources extractResources(HttpServletRequest request, String body) {
        // Extract the host, port, and protocol from the request object
        String host = request.getServerName();
        int port = request.getServerPort();
        String protocol = request.getScheme();

        // Create a URL string from the extracted values
        String baseURL = String.format("%s://%s:%d", protocol, host, port);

        String rawURI = request.getRequestURI();
        String uri = StringUtils.hasText(rawURI)?rawURI:"";
        Mono<String> bodyMono = StringUtils.hasText(body)?Mono.just(body):Mono.never();

        String queryString = request.getQueryString();

        MultiValueMap<String, String> queryParams = extractQueryParams(queryString);

        RequestResources resources = new RequestResources(uri,bodyMono,queryParams,baseURL);


        return resources;
    }

    private MultiValueMap<String, String> extractQueryParams(String queryString) {
        Map<String, String> queryParams = new HashMap<>();
        if (queryString != null) {
            String[] queryParamsArray = queryString.split("&");
            for (String queryParam : queryParamsArray) {
                String[] keyValue = queryParam.split("=");
                if (keyValue.length == 2) {
                    String key = null;
                    String value = null;
                    try {
                        key = URLDecoder.decode(keyValue[0], "UTF-8");
                        value = URLDecoder.decode(keyValue[1], "UTF-8");

                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                    queryParams.put(key, value);
                }
            }
        }
        LinkedMultiValueMap<String,String> response = new LinkedMultiValueMap<>(queryParams);

        return response;
    }

    private ProxyResponseDto resolveResponse(RequestResources extractedResources, String method) {
        WebClient client = clientSelector(extractedResources.isBinary(),extractedResources.getBase());
        WebClient.RequestHeadersSpec<?> base = createBase(extractedResources, client, method);
        ProxyResponseDto response = decodeResponse(base, extractedResources);
        return response;
    }

    private WebClient.RequestHeadersSpec<?> createBase(RequestResources resources, WebClient client,
            String method) {
        WebClient.RequestHeadersSpec<?> base;
        switch (method) {
            case "GET":
                base = client.get()
                        .uri(resources.getUri(),
                                uriBuilder -> uriBuilder.queryParams(resources.getParamInfo()).build());
                break;
            case "POST":
                base = client.post()
                        .uri(resources.getUri(), uriBuilder -> uriBuilder.queryParams(resources.getParamInfo()).build())
                        .body(resources.getBody(), String.class);
                break;
            case "PUT":
                base = client.put()
                        .uri(resources.getUri(), uriBuilder -> uriBuilder.queryParams(resources.getParamInfo()).build())
                        .body(resources.getBody(), String.class);
                break;
            case "DELETE":
                base = client.delete()
                        .uri(resources.getUri(),
                                uriBuilder -> uriBuilder.queryParams(resources.getParamInfo()).build());
                break;
            default:
                base = null;
        }
        return base;
    }

    private ProxyResponseDto decodeResponse(WebClient.RequestHeadersSpec<?> base, RequestResources resources) {
        if (resources.isBinary()) {
            byte[] data;
            AtomicReference<HttpHeaders> headers = new AtomicReference<>();
            Mono<byte[]> mono = base.exchangeToMono(responseData -> {
                headers.set(responseData.headers().asHttpHeaders());
                return responseData.bodyToMono(byte[].class);
            });
            data = mono.block();
            String fileExtension = extractFileExtension(headers);
            return new ProxyResponseDto(true, new BinaryDto(data, fileExtension), null);
        }
        WebClient.ResponseSpec responseSpec = base.retrieve();
        JsonNode body = responseSpec.bodyToMono(JsonNode.class).block();
        return new ProxyResponseDto(false, null, body);
    }

    private String extractFileExtension(AtomicReference<HttpHeaders> headers) {
        String value = headers.get().getFirst("Content-Disposition");
        if (null == value)
            throw new ProxyServiceException("Content-Disposition header not present");
        return value;
    }

    private WebClient clientSelector(boolean isBinaryClient,String base) {
        return isBinaryClient ? config.getBinaryClient(base) : config.getJSONClient(base);
    }

    public ProxyResponseDto processGetRequest(HttpServletRequest request, String trackingID) {
        RequestResources extractedResources = extractResources(request, null);

        ProxyResponseDto responseDto = resolveResponse(extractedResources, "GET");
        return responseDto;
    }

    public ProxyResponseDto processPostRequest(HttpServletRequest request, String body, String trackingID) {
        RequestResources extractedResources = extractResources(request, body);
        ProxyResponseDto responseDto = resolveResponse(extractedResources, "POST");
        return responseDto;
    }

    public ProxyResponseDto processPutRequest(HttpServletRequest request, String body, String trackingID) {
        RequestResources extractedResources = extractResources(request, body);
        ProxyResponseDto responseDto = resolveResponse(extractedResources, "PUT");
        return responseDto;
    }

    public ProxyResponseDto processDeleteRequest(HttpServletRequest request, String trackingID) {
        RequestResources extractedResources = extractResources(request, null);
        ProxyResponseDto responseDto = resolveResponse(extractedResources, "DELETE");
        return responseDto;
    }
}
