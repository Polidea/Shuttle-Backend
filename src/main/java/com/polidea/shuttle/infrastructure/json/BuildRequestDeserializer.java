package com.polidea.shuttle.infrastructure.json;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.polidea.shuttle.domain.app.input.BuildRequest;

import java.io.IOException;

public class BuildRequestDeserializer extends JsonDeserializer<BuildRequest> {

    private JsonNode node;

    @Override
    public BuildRequest deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        node = getJsonNode(jsonParser);
        BuildRequest buildRequest = new BuildRequest();
        deserializeCommonFields(buildRequest);
        deserializeBuildIdentifier(buildRequest);
        return buildRequest;
    }

    private JsonNode getJsonNode(JsonParser jsonParser) throws IOException {
        ObjectCodec codec = jsonParser.getCodec();
        return codec.readTree(jsonParser);
    }

    private void deserializeCommonFields(BuildRequest buildRequest) {
        buildRequest.setVersion(node.get("version").asText());
        buildRequest.setHref(node.get("href").asText());
        buildRequest.setReleaseNotes(node.get("releaseNotes").asText());
        buildRequest.setBytes(node.get("bytes").asLong());
        buildRequest.setReleaserEmail(node.get("releaserEmail").asText());
    }

    private void deserializeBuildIdentifier(BuildRequest buildRequest) {
        JsonNode prefixSchema = node.get("prefixSchema");
        JsonNode versionCode = node.get("versionCode");
        if (versionCode != null) {
            buildRequest.setBuildIdentifier(versionCode.asText());
        }
        if (prefixSchema != null) {
            buildRequest.setBuildIdentifier(prefixSchema.asText());
        }
    }
}
