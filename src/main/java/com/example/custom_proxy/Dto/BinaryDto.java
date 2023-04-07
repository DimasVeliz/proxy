package com.example.custom_proxy.Dto;

public class BinaryDto {

    private byte[] binaryData;
    private String fileExtension;

    public BinaryDto(byte[] binaryData, String fileExtension) {
        this.binaryData = binaryData;
        this.fileExtension = fileExtension;
    }

    public BinaryDto() {
    }

    public byte[] getBinaryData() {
        return binaryData;
    }

    public void setBinaryData(byte[] binaryData) {
        this.binaryData = binaryData;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }
}
