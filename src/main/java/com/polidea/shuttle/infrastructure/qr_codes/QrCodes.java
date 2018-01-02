package com.polidea.shuttle.infrastructure.qr_codes;

import com.polidea.shuttle.infrastructure.mail.UnableToGenerateQRCodeException;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class QrCodes {

    public static File generateQrCode(String textContent,
                                      String fileName,
                                      ImageType imageType,
                                      Integer width,
                                      Integer height) throws UnableToGenerateQRCodeException {
        ByteArrayOutputStream qrCodeStream = QRCode.from(textContent)
                                                   .to(imageType)
                                                   .withSize(width, height)
                                                   .stream();
        File qrCodeFile = new File(fileName);

        try {
            FileOutputStream outputStream = new FileOutputStream(qrCodeFile);
            outputStream.write(qrCodeStream.toByteArray());
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            throw new UnableToGenerateQRCodeException(textContent);
        }

        return qrCodeFile;
    }

}
