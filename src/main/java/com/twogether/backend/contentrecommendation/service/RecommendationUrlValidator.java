package com.twogether.backend.contentrecommendation.service;

import java.net.InetAddress;
import java.net.URI;

final class RecommendationUrlValidator {

    private RecommendationUrlValidator() {
    }

    static boolean isSafe(String value) {
        if (value == null || value.isBlank()) return false;
        try {
            URI uri = URI.create(value);
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getUserInfo() != null || uri.getPort() != -1) return false;
            String host = uri.getHost();
            if (host == null || host.isBlank() || "localhost".equalsIgnoreCase(host)) return false;
            if (isLiteralAddress(host)) {
                InetAddress address = InetAddress.getByName(host);
                return !(address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
                        || address.isSiteLocalAddress() || address.isMulticastAddress());
            }
            return true;
        } catch (IllegalArgumentException | java.net.UnknownHostException exception) {
            return false;
        }
    }

    private static boolean isLiteralAddress(String host) {
        return host.indexOf(':') >= 0 || host.matches("[0-9.]+");
    }
}
