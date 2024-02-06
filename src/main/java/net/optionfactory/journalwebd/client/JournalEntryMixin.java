package net.optionfactory.journalwebd.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JournalEntryMixin(
        @JsonProperty(value = "_BOOT_ID")
        String bootId,
        @JsonProperty(value = "_PID")
        String pid,
        @JsonProperty(value = "_HOSTNAME")
        String hostname,
        @JsonProperty(value = "_SYSTEMD_UNIT")
        String systemdUnit,
        @JsonProperty(value = "MESSAGE")
        @JsonDeserialize(using = FromStringOrArray.class)
        String message,
        @JsonProperty(value = "__REALTIME_TIMESTAMP")
        long timestamp) {

    public static class FromStringOrArray extends StdDeserializer<String> {

        public FromStringOrArray() {
            super((Class) null);
        }

        @Override
        public String deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            final JsonNode node = jp.getCodec().readTree(jp);
            if (node.isNull()) {
                return null;
            }
            if (node.isTextual()) {
                return node.asText();
            }
            if (!node.isArray()) {
                return node.toString();
            }
            final byte[] bytes = new byte[node.size()];
            final Iterator<JsonNode> iter = node.elements();
            int i = 0;
            while (iter.hasNext()) {
                bytes[i] = (byte) iter.next().asInt();
                ++i;
            }
            return new String(bytes, StandardCharsets.UTF_8);
        }

    }

}
