package com.polidea.shuttle.domain.user.access_token;

import com.polidea.shuttle.error_codes.ErrorCode;
import com.polidea.shuttle.error_codes.NotFoundException;

public class TokenNotFoundException extends NotFoundException {

    public TokenNotFoundException() {
        super(ErrorCode.NOT_DEFINED, "Access Token not found");
    }

}
