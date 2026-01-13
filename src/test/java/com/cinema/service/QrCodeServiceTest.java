package com.cinema.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class QrCodeServiceTest {

    private QrCodeService qrCodeService;

    @BeforeEach
    void setUp() {
        qrCodeService = new QrCodeService();
        ReflectionTestUtils.setField(qrCodeService, "qrSize", 120);
    }

    @Test
    void generateBookingCode_ReturnsBase64String() {
        String encoded = qrCodeService.generateBookingCode("BOOK-1");

        assertThat(encoded).isNotBlank();
        assertThat(encoded).doesNotContain("\n");
    }

    @Test
    void generateBookingCode_WhenImageWriteFails_ThrowsIllegalState() {
        try (MockedStatic<ImageIO> mockedImageIo = Mockito.mockStatic(ImageIO.class)) {
            mockedImageIo.when(() -> ImageIO.write(any(BufferedImage.class), eq("PNG"), any(ByteArrayOutputStream.class)))
                .thenThrow(new IOException("boom"));

            assertThatThrownBy(() -> qrCodeService.generateBookingCode("BOOK-2"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unable to generate QR code");
        }
    }
}
