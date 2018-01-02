package com.polidea.shuttle.infrastructure.external_storage;

import com.polidea.shuttle.error_codes.ErrorCode;
import com.polidea.shuttle.error_codes.InternalServerErrorException;

public class UploadToExternalStorageFailedException extends InternalServerErrorException {

    public UploadToExternalStorageFailedException() {
        super(
            "Upload failed",
            ErrorCode.UPLOAD_TO_EXTERNAL_STORAGE_FAILED
        );
    }

}
