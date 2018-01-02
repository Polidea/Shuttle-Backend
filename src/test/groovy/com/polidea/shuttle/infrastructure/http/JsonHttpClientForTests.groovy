package com.polidea.shuttle.infrastructure.http

import groovy.json.JsonSlurper
import groovy.transform.Memoized
import okhttp3.Headers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.springframework.boot.test.context.TestComponent

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE

@TestComponent
class JsonHttpClientForTests {

    private final OkHttpClient okHttpClient = new OkHttpClient()

    JsonResponse performOptionsRequest(String url, Map<String, String> headersAsMap) {
        def okHttpRequest = new Request.Builder()
                .headers(Headers.of(headersAsMap))
                .url(url)
                .method("OPTIONS", null)
                .build()
        return new JsonResponse(perform(okHttpRequest))
    }

    JsonResponse performGetRequest(String url, Map<String, String> headersAsMap) {
        def okHttpRequest = new Request.Builder()
                .headers(Headers.of(headersAsMap))
                .url(url)
                .build()
        return new JsonResponse(perform(okHttpRequest))
    }

    JsonResponse performPostRequest(String url, Map<String, String> headersAsMap, String jsonBodyAsText) {
        def okHttpRequest = new Request.Builder()
                .headers(Headers.of(headersAsMap))
                .url(url)
                .post(requestBodyFrom(jsonBodyAsText))
                .build()
        return new JsonResponse(perform(okHttpRequest))
    }

    JsonResponse performPostRequest(String url, Map<String, String> headersAsMap, FileAsMultipartForm fileAsMultipartForm) {
        def okHttpRequest = new Request.Builder()
                .headers(Headers.of(headersAsMap))
                .url(url)
                .post(requestBodyFrom(fileAsMultipartForm))
                .build()
        return new JsonResponse(perform(okHttpRequest))
    }

    JsonResponse performPatchRequest(String url, Map<String, String> headersAsMap, String jsonBodyAsText) {
        def okHttpRequest = new Request.Builder()
                .headers(Headers.of(headersAsMap))
                .url(url)
                .patch(requestBodyFrom(jsonBodyAsText))
                .build()
        return new JsonResponse(perform(okHttpRequest))
    }

    JsonResponse performDeleteRequest(String url, Map<String, String> headersAsMap) {
        def okHttpRequest = new Request.Builder()
                .headers(Headers.of(headersAsMap))
                .url(url)
                .delete()
                .build()
        return new JsonResponse(perform(okHttpRequest))
    }

    private RequestBody requestBodyFrom(String jsonBody) {
        return RequestBody.create(MediaType.parse(APPLICATION_JSON_VALUE), jsonBody)
    }

    private RequestBody requestBodyFrom(FileAsMultipartForm fileAsMultipartForm) {
        def multipartBodyBuilder = new MultipartBody.Builder()
                .setType(MediaType.parse(MULTIPART_FORM_DATA_VALUE))
                .addFormDataPart(fileAsMultipartForm.formFieldName,
                                 fileAsMultipartForm.fileName,
                                 RequestBody.create(
                                         fileAsMultipartForm.mimeType(),
                                         fileAsMultipartForm.bytes()
                                 ))
        return multipartBodyBuilder.build()
    }

    private Response perform(Request okHttpRequest) {
        return okHttpClient.newCall(okHttpRequest).execute()
    }

    static class JsonResponse {

        private final JsonSlurper jsonSlurper = new JsonSlurper()
        private Response okHttpResponse

        JsonResponse(Response okHttpResponse) {
            this.okHttpResponse = okHttpResponse
        }

        int code() {
            return okHttpResponse.code()
        }

        String header(String name) {
            return okHttpResponse.header(name)
        }

        // This call is memoized because OkHttp Response body can be read once only.
        @Memoized
        Object body() {
            return jsonSlurper.parseText(okHttpResponse.body().string())
        }

    }

    static class FileAsMultipartForm {

        private final String mimeType
        private final String formFieldName
        private final String fileName
        private final File file

        FileAsMultipartForm(String mimeType,
                            String formFieldName,
                            String fileName,
                            File file) {
            this.file = file
            this.fileName = fileName
            this.formFieldName = formFieldName
            this.mimeType = mimeType
        }

        byte[] bytes() {
            return file.getBytes()
        }

        MediaType mimeType() {
            return MediaType.parse(mimeType)
        }

    }
}
