package com.polidea.shuttle.infrastructure.web;

import static java.lang.String.format;

public class UnableToGenerateBase64QrCodeImageException extends RuntimeException {
    public UnableToGenerateBase64QrCodeImageException(String href) {
        super(format("Unable to generate QR Code for href: '%s'", href));
    }
}
