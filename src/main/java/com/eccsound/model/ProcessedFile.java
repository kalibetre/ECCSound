package com.eccsound.model;

public class ProcessedFile {
    String extension;
    byte[] data;

    public ProcessedFile(String extention, byte[] data) {
        this.extension = extention;
        this.data = data;
    }

    public String getExtension() {
        return extension;
    }

    public byte[] getData() {
        return data;
    }
}
