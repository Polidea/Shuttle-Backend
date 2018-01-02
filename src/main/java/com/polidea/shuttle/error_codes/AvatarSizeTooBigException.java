package com.polidea.shuttle.error_codes;

import static java.lang.String.format;

public class AvatarSizeTooBigException extends BadRequestException {

    public AvatarSizeTooBigException(long sizeInBytes, long maxAllowedSizeInBytes) {
        super(
            format("Avatar size is too big, because it has %s bytes and it's more than allowed %s bytes",
                   sizeInBytes,
                   maxAllowedSizeInBytes),
            ErrorCode.AVATAR_SIZE_TOO_BIG
        );
    }
}
