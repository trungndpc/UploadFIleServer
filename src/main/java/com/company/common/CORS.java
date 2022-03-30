package com.company.common;

import akka.http.javadsl.model.HttpHeader;
import akka.http.javadsl.model.HttpMethods;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.headers.AccessControlAllowMethods;
import akka.http.javadsl.model.headers.AccessControlAllowOrigin;
import akka.http.javadsl.model.headers.HttpOrigin;
import akka.http.javadsl.model.headers.HttpOriginRange;
import akka.http.javadsl.server.RequestContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CORS {
    private static final String ADMIN_NHA_THAU_INSEE_UDEV_COM_VN = "https://admin-nhathau.insee.udev.com.vn";
    private static final String ADMIN_INSEE_UDEV_COM_VN = "https://admin.insee.udev.com.vn";
    private static final String DEV_ADMIN_NHA_THAU_INSEE_UDEV_COM_VN = "https://dev-admin-nhathau.insee.udev.com.vn";
    private static final String LOCALHOST = "http://localhost:3000";
    public static final AccessControlAllowMethods accessControlAllowMethods = AccessControlAllowMethods.create(HttpMethods.GET, HttpMethods.POST);

    public static List<HttpHeader> accessControlHeader(RequestContext requestContext) {
        HttpRequest request = requestContext.getRequest();
        List<HttpHeader> cors = new ArrayList<>();
        Optional<HttpHeader> referer = request.getHeader("referer");
        if (referer.isPresent()) {
            String value = referer.get().value();
            if (value.startsWith(ADMIN_NHA_THAU_INSEE_UDEV_COM_VN)) {
                cors.add(create(ADMIN_NHA_THAU_INSEE_UDEV_COM_VN));
            }
            if (value.startsWith(ADMIN_INSEE_UDEV_COM_VN)) {
                cors.add(create(ADMIN_INSEE_UDEV_COM_VN));
            }

            if (value.startsWith(DEV_ADMIN_NHA_THAU_INSEE_UDEV_COM_VN)) {
                cors.add(create(DEV_ADMIN_NHA_THAU_INSEE_UDEV_COM_VN));
            }

            if (value.contains(LOCALHOST)) {
                cors.add(create(LOCALHOST));
            }
            cors.add(accessControlAllowMethods);
        }
        return cors;
    }

    private static AccessControlAllowOrigin create(String domain) {
        HttpOrigin validOriginHeader = HttpOrigin.parse(domain);
        HttpOriginRange httpOriginRange = HttpOriginRange.create(validOriginHeader);
        return AccessControlAllowOrigin.create(httpOriginRange);
    }
}
