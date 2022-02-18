package com.company.util;

import akka.http.javadsl.model.Host;
import akka.http.javadsl.model.HttpRequest;

public class CommonUtil {

    public static String getDomain(HttpRequest request) {
        String scheme = request.getUri().scheme();
        String address = request.getUri().getHost().address();
        return address;
    }

    public static String getFullDomain(HttpRequest request) {
        String scheme = request.getUri().scheme();
        String address = request.getUri().getHost().address();
        return scheme + "://" + address;
    }
}
