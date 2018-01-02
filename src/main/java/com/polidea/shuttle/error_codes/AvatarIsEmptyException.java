package com.polidea.shuttle.error_codes;

public class AvatarIsEmptyException extends BadRequestException {

    public AvatarIsEmptyException() {
        super("Avatar is empty (has size of 0 bytes)", ErrorCode.AVATAR_IS_EMPTY);
    }
}
