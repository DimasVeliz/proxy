package com.example.custom_proxy.Dto;

public class BinaryDto {

    private byte[] encodedContent;
    private String fileExtension;

    public BinaryDto(byte[] binaryData, String fileExtension) {
        this.encodedContent = binaryData;
        this.fileExtension = fileExtension;
    }

    public BinaryDto() {
    }

    public byte[] getEncodedContent() {
        return encodedContent;
    }

    public void setEncodedContent(byte[] encodedContent) {
        this.encodedContent = encodedContent;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }
}
