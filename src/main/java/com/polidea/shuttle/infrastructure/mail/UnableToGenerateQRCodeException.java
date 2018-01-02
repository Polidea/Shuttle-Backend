package com.polidea.shuttle.infrastructure.mail;

import java.io.IOException;

import static java.lang.String.format;

public class UnableToGenerateQRCodeException extends IOException {
    public UnableToGenerateQRCodeException(String href) {
        super(format("Problem while generating QR Code for href: '%s'", href));
    }
}

