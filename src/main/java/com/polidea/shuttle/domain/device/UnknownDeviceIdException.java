package com.polidea.shuttle.domain.device;

import com.polidea.shuttle.error_codes.BadRequestException;
import com.polidea.shuttle.error_codes.ErrorCode;

import static java.lang.String.format;

public class UnknownDeviceIdException extends BadRequestException {

    public UnknownDeviceIdException(String accessToken) {
        super(format("Cannot register Push Token because Access Token used for authentication doesn't have any Device ID assigned. Access Token: %s",
                     accessToken),
              ErrorCode.UNKNOWN_DEVICE_ID);
    }

}
