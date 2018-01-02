package com.polidea.shuttle.infrastructure;

import com.polidea.shuttle.error_codes.ErrorCode;
import com.polidea.shuttle.error_codes.InternalServerErrorException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static java.lang.String.format;

@Component
public class DownloadUrls {

    @Value("${shuttle.host:}")
    private String host;

    public String createIosDownloadUrlForStoreHref(String storeHref) {
        return format("%s/download/redirect?url=%s",
                      this.host,
                      encodeForUrlQueryParam(storeHref));
    }

    public String createAndroidDownloadUrlForStoreHref(String storeHref) {
        return storeHref;
    }

    private String encodeForUrlQueryParam(String value) {
        String encoding = "UTF-8";
        try {
            return URLEncoder.encode(value, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new InternalServerErrorException(
                format("Encoding %s is not supported", encoding),
                ErrorCode.NOT_DEFINED);
        }
    }

}
