package com.example.mixinfix

import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode

import java.time.Duration

class DurationDeserializer extends JsonDeserializer<Duration>{

    @Override
    Duration deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        JsonNode node = p.getCodec().readTree(p)
        long seconds = node.get("seconds").asLong()
        return Duration.ofSeconds(seconds)
    }
}
