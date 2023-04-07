package com.example.custom_proxy.Dto;

import com.fasterxml.jackson.databind.JsonNode;

public class ProxyResponseDto {
    private boolean isBinary;
    private BinaryDto binaryData;
    private JsonNode jsonData;


    public ProxyResponseDto() {
    }

    public ProxyResponseDto(boolean isBinary, BinaryDto binaryData, JsonNode jsonData) {
        this.isBinary = isBinary;
        this.binaryData = binaryData;
        this.jsonData = jsonData;
    }

    public boolean isBinary() {
        return isBinary;
    }

    public void setBinary(boolean binary) {
        isBinary = binary;
    }

    public BinaryDto getBinaryData() {
        return binaryData;
    }

    public void setBinaryData(BinaryDto binaryData) {
        this.binaryData = binaryData;
    }

    public JsonNode getJsonData() {
        return jsonData;
    }

    public void setJsonData(JsonNode jsonData) {
        this.jsonData = jsonData;
    }
}
