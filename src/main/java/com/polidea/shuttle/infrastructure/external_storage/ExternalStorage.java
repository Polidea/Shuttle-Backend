package com.polidea.shuttle.infrastructure.external_storage;

import org.springframework.web.multipart.MultipartFile;

public interface ExternalStorage {

    ExternalStorageUrl uploadFile(MultipartFile multipartFile, String resourcePathToSet, String contentType);

}
