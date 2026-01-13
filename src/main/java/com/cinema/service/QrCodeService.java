package com.cinema.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;

@Service
@Slf4j
public class QrCodeService {

    @Value("${app.ticketing.qr.size:280}")
    private int qrSize;

    public String generateBookingCode(String bookingNumber) {
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix matrix = new QRCodeWriter()
                .encode(bookingNumber, BarcodeFormat.QR_CODE, qrSize, qrSize, hints);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                ImageIO.write(image, "PNG", baos);
                return Base64.getEncoder().encodeToString(baos.toByteArray());
            }
        } catch (WriterException | IOException ex) {
            log.error("Failed to generate QR code for booking {}", bookingNumber, ex);
            throw new IllegalStateException("Unable to generate QR code", ex);
        }
    }
}
