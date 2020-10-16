package com.picpay.mockserver;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import lombok.Data;
import wiremock.com.fasterxml.jackson.core.type.TypeReference;
import wiremock.com.fasterxml.jackson.databind.DeserializationFeature;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true, value = {"server"})
public class MockedServices {

    private static final wiremock.com.fasterxml.jackson.databind.ObjectMapper wiremockObjectMapper = new wiremock.com.fasterxml.jackson.databind.ObjectMapper();
    private static final com.fasterxml.jackson.databind.ObjectMapper yamlObjectMapper = new com.fasterxml.jackson.databind.ObjectMapper(new YAMLFactory());
    private static final com.fasterxml.jackson.databind.ObjectMapper jsonObjectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    static {
        wiremockObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @JsonProperty("serviceName")
    private String serviceName;
    @JsonProperty("port")
    private int port;
    @JsonProperty("isRunning")
    private boolean isRunning;
    @JsonProperty("stubs")
    @JsonDeserialize(using = StubDeserializer.class)
    private List<StubMapping> stubs = new LinkedList<>();
    private WireMockServer server;

    public void start() {
        this.server.start();
        this.server.resetAll();
    }

    public void addStubsMapping() {
        this.stubs.forEach(stub -> {
            server.addStubMapping(stub);
        });
    }

    public void stop() {
        this.server.shutdownServer();
        this.server.shutdown();
        this.server.stop();
    }

    public static class StubDeserializer extends StdDeserializer<List<StubMapping>> {

        public StubDeserializer() {
            this(null);
        }

        public StubDeserializer(final Class<List<StubMapping>> stubsMapping) {
            super(stubsMapping);
        }

        @Override
        public List<StubMapping> deserialize(final JsonParser jsonParser,
            final DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            final Object object = yamlObjectMapper.readValue(jsonParser, Object.class);
            final String text = jsonObjectMapper.writeValueAsString(object);
            final List<StubMapping> stubs = wiremockObjectMapper.readValue(text, new TypeReference<List<StubMapping>>() {
            });
            return stubs;
        }

    }

}
