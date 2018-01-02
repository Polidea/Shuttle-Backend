package com.polidea.shuttle.infrastructure.avatars;

import org.springframework.stereotype.Component;

@Component
public class AvatarContentTypeFix {

    private static final String BMP_CONTENT_TYPE = "image/bmp";
    private static final String GIF_CONTENT_TYPE = "image/gif";
    private static final String JPEG_CONTENT_TYPE = "image/jpeg";
    private static final String PNG_CONTENT_TYPE = "image/png";
    private static final String UNKNOWN_IMAGE_CONTENT_TYPE = "image";

    /*
     * Content-Type of file uploaded by iOS can be strange. Sometimes it's just correct "image/jpeg" etc.
     * Sadly, there are cases where iOS provides strange Conten-Type: "some.domain.gif", "public.jpeg", "public.png".
     */
    public String fixed(String providedContentType) {
        if (providedContentType == null) {
            return UNKNOWN_IMAGE_CONTENT_TYPE;
        }
        if (providedContentType.endsWith("bmp")) {
            return BMP_CONTENT_TYPE;
        }
        if (providedContentType.endsWith("gif")) {
            return GIF_CONTENT_TYPE;
        }
        if (providedContentType.endsWith("jpeg")) {
            return JPEG_CONTENT_TYPE;
        }
        if (providedContentType.endsWith("jpg")) {
            return JPEG_CONTENT_TYPE;
        }
        if (providedContentType.endsWith("png")) {
            return PNG_CONTENT_TYPE;
        }
        return UNKNOWN_IMAGE_CONTENT_TYPE;
    }

}
