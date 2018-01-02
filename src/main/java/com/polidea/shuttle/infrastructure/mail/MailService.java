package com.polidea.shuttle.infrastructure.mail;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.multipart.FormDataMultiPart;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.MediaType;

@Service
public class MailService {

    private final WebResource webResource;

    public MailService(WebResource webResource) {
        this.webResource = webResource;
    }

    public void sendEmail(FormDataMultiPart formData) {
        new Thread(() -> {
            webResource.type(MediaType.MULTIPART_FORM_DATA_TYPE).post(ClientResponse.class, formData);
        }).start();
    }
}
