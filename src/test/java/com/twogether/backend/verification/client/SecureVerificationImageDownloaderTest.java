package com.twogether.backend.verification.client;

import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.verification.config.VerificationAiProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecureVerificationImageDownloaderTest {
    @Test void rejectsHttpUrl() { assertError(() -> downloader().parseAndValidateUri("http://images.example.com/a.jpg"), ErrorCode.VERIFICATION_IMAGE_URL_INVALID); }
    @Test void rejectsUnallowedDomain() { assertError(() -> downloader().parseAndValidateUri("https://evil.example/a.jpg"), ErrorCode.VERIFICATION_IMAGE_DOMAIN_NOT_ALLOWED); }
    @Test void allowsConfiguredDomain() { assertThat(downloader().parseAndValidateUri("https://cdn.images.example.com/a.jpg").getHost()).isEqualTo("cdn.images.example.com"); }
    @Test void rejectsInvalidMime() { assertError(() -> downloader().validateDownloadedImage(new byte[]{1, 2, 3}, "text/html"), ErrorCode.VERIFICATION_IMAGE_INVALID_MIME_TYPE); }
    @Test void rejectsMismatchedMime() { assertError(() -> downloader().validateDownloadedImage(png(), "image/jpeg"), ErrorCode.VERIFICATION_IMAGE_INVALID_MIME_TYPE); }
    @Test void rejectsOversizedImage() {
        VerificationAiProperties properties = properties(); properties.setMaxImageSize(2);
        assertError(() -> new SecureVerificationImageDownloader(properties).validateDownloadedImage(new byte[]{1, 2, 3}, "image/jpeg"), ErrorCode.VERIFICATION_IMAGE_TOO_LARGE);
    }

    private SecureVerificationImageDownloader downloader() { return new SecureVerificationImageDownloader(properties()); }
    private VerificationAiProperties properties() {
        VerificationAiProperties value = new VerificationAiProperties();
        value.setAllowedImageDomains(List.of("images.example.com")); return value;
    }
    private byte[] png() { return new byte[]{(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a}; }
    private void assertError(org.assertj.core.api.ThrowableAssert.ThrowingCallable callable, ErrorCode code) {
        assertThatThrownBy(callable).isInstanceOfSatisfying(BusinessException.class,
                exception -> assertThat(exception.getErrorCode()).isEqualTo(code));
    }
}
