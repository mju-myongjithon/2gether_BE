package com.twogether.backend.verification.client;

import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.verification.config.VerificationAiProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Set;

@Component
@ConditionalOnExpression("'${app.ai.verification.mode:mock}' == 'gemini' or '${app.ai.verification.mode:mock}' == 'demo'")
class SecureVerificationImageDownloader implements VerificationImageDownloader {
    private static final int MAX_REDIRECTS = 3;
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    private final VerificationAiProperties properties;

    SecureVerificationImageDownloader(VerificationAiProperties properties) {
        this.properties = properties;
    }

    @Override
    public DownloadedImage download(String photoUrl) {
        URI uri = parseAndValidateUri(photoUrl);
        for (int redirects = 0; redirects <= MAX_REDIRECTS; redirects++) {
            HttpsURLConnection connection = null;
            try {
                validateResolvedAddresses(uri.getHost());
                connection = (HttpsURLConnection) uri.toURL().openConnection();
                connection.setInstanceFollowRedirects(false);
                connection.setConnectTimeout(toMillis(properties.getConnectTimeout()));
                connection.setReadTimeout(toMillis(properties.getReadTimeout()));
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "image/jpeg,image/png,image/gif,image/webp");

                int status = connection.getResponseCode();
                if (status >= 300 && status < 400) {
                    if (redirects == MAX_REDIRECTS) throw imageUrlError();
                    String location = connection.getHeaderField("Location");
                    if (location == null || location.isBlank()) throw imageUrlError();
                    uri = parseAndValidateUri(uri.resolve(location).toString());
                    continue;
                }
                if (status < 200 || status >= 300) {
                    throw new BusinessException(ErrorCode.VERIFICATION_IMAGE_DOWNLOAD_FAILED);
                }

                long contentLength = connection.getContentLengthLong();
                if (contentLength > properties.getMaxImageSize()) {
                    throw new BusinessException(ErrorCode.VERIFICATION_IMAGE_TOO_LARGE);
                }
                String declaredMime = normalizeMime(connection.getContentType());
                try (InputStream input = connection.getInputStream()) {
                    byte[] bytes = readLimited(input);
                    String detectedMime = detectMime(bytes);
                    if (!ALLOWED_MIME_TYPES.contains(declaredMime) || !declaredMime.equals(detectedMime)) {
                        throw new BusinessException(ErrorCode.VERIFICATION_IMAGE_INVALID_MIME_TYPE);
                    }
                    return new DownloadedImage(bytes, detectedMime);
                }
            } catch (BusinessException exception) {
                throw exception;
            } catch (IOException exception) {
                throw new BusinessException(ErrorCode.VERIFICATION_IMAGE_DOWNLOAD_FAILED);
            } finally {
                if (connection != null) connection.disconnect();
            }
        }
        throw imageUrlError();
    }

    URI parseAndValidateUri(String value) {
        try {
            URI uri = new URI(value);
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                    || uri.getUserInfo() != null || uri.getFragment() != null
                    || (uri.getPort() != -1 && uri.getPort() != 443)) {
                throw imageUrlError();
            }
            String host = uri.getHost().toLowerCase(Locale.ROOT);
            boolean allowed = properties.getAllowedImageDomains().stream()
                    .filter(domain -> domain != null && !domain.isBlank())
                    .map(domain -> domain.toLowerCase(Locale.ROOT).replaceFirst("^\\.", ""))
                    .anyMatch(domain -> host.equals(domain) || host.endsWith("." + domain));
            if (!allowed) {
                throw new BusinessException(ErrorCode.VERIFICATION_IMAGE_DOMAIN_NOT_ALLOWED);
            }
            return uri;
        } catch (URISyntaxException | NullPointerException exception) {
            throw imageUrlError();
        }
    }

    DownloadedImage validateDownloadedImage(byte[] bytes, String contentType) {
        if (bytes.length > properties.getMaxImageSize()) {
            throw new BusinessException(ErrorCode.VERIFICATION_IMAGE_TOO_LARGE);
        }
        String declared = normalizeMime(contentType);
        String detected = detectMime(bytes);
        if (!ALLOWED_MIME_TYPES.contains(declared) || !declared.equals(detected)) {
            throw new BusinessException(ErrorCode.VERIFICATION_IMAGE_INVALID_MIME_TYPE);
        }
        return new DownloadedImage(bytes, detected);
    }

    private byte[] readLimited(InputStream input) throws IOException {
        long limit = properties.getMaxImageSize();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        long total = 0;
        while ((read = input.read(buffer)) != -1) {
            total += read;
            if (total > limit) throw new BusinessException(ErrorCode.VERIFICATION_IMAGE_TOO_LARGE);
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }

    private void validateResolvedAddresses(String host) {
        try {
            for (InetAddress address : InetAddress.getAllByName(host)) {
                byte[] raw = address.getAddress();
                boolean privateV6 = raw.length == 16 && (raw[0] & 0xfe) == 0xfc;
                if (address.isAnyLocalAddress() || address.isLoopbackAddress()
                        || address.isLinkLocalAddress() || address.isSiteLocalAddress()
                        || address.isMulticastAddress() || privateV6) {
                    throw imageUrlError();
                }
            }
        } catch (UnknownHostException exception) {
            throw new BusinessException(ErrorCode.VERIFICATION_IMAGE_DOWNLOAD_FAILED);
        }
    }

    private String detectMime(byte[] bytes) {
        if (startsWith(bytes, new int[]{0xff, 0xd8, 0xff})) return "image/jpeg";
        if (startsWith(bytes, new int[]{0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a})) return "image/png";
        if (bytes.length >= 6 && (new String(bytes, 0, 6).equals("GIF87a") || new String(bytes, 0, 6).equals("GIF89a"))) return "image/gif";
        if (bytes.length >= 12 && new String(bytes, 0, 4).equals("RIFF") && new String(bytes, 8, 4).equals("WEBP")) return "image/webp";
        return "";
    }

    private boolean startsWith(byte[] bytes, int[] signature) {
        return bytes.length >= signature.length && java.util.stream.IntStream.range(0, signature.length)
                .allMatch(index -> (bytes[index] & 0xff) == signature[index]);
    }

    private String normalizeMime(String contentType) {
        if (contentType == null) return "";
        return contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
    }

    private int toMillis(java.time.Duration duration) {
        long value = duration == null ? 0 : duration.toMillis();
        return (int) Math.max(1, Math.min(Integer.MAX_VALUE, value));
    }

    private BusinessException imageUrlError() {
        return new BusinessException(ErrorCode.VERIFICATION_IMAGE_URL_INVALID);
    }
}
