package com.example.PoloniexTrader.service.model;

public enum Headers {
    KEY("Key"),
    SIGN("Sign"),
    CONTENT_TYPE("Content-Type");

    private String header;

    Headers(String header) {
        this.header = header;
    }

    public String getHeaderName() {
        return header;
    }
}
