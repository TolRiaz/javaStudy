package com.example.demo.constatnt;

public enum Url {

    IMAGE("/images/item/");

    private final String prefixUrl;

    Url(String prefixUrl) { this.prefixUrl = prefixUrl; }

    public String getUrl(String postUrl) {
        return prefixUrl + "/" + postUrl;
    }

    public String getUrl() {
        return prefixUrl + "/";
    }

}
