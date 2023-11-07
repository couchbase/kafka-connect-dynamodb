package com.trustpilot.connector.dynamodb.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.connect.errors.DataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

public class JsonConverterUtil {
    private static final Logger logger = LoggerFactory.getLogger(JsonConverterUtil.class);

    private static final Gson gson = new Gson();
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * @param value value in DynamoJSON format
     * @return value in regularJSON format
     */

    public static Pair<Object, Boolean> dynamoJsonToRegularJsonConverter(Object value) {
        try {
            Object newValue;
            if (value == null) {

                return null;
            } else if (value instanceof String) {

                final Map<String, AttributeValue> tempAttributeMap = gson.fromJson(value.toString(), new TypeToken<Map<String, AttributeValue>>() {}.getType());

                JsonNode convertedJson = new JsonConverter().mapToJsonObject(tempAttributeMap);
                newValue = mapper.convertValue(convertedJson, new TypeReference<Map<String, Object>>() {});

            } else if (value instanceof Map) {

                JsonElement jsonElement = gson.toJsonTree(value);
                final Map<String, AttributeValue> tempAttributeMap = gson.fromJson(jsonElement, new TypeToken<Map<String, AttributeValue>>() {}.getType());
                JsonNode convertedJson = new JsonConverter().mapToJsonObject(tempAttributeMap);
                newValue = mapper.convertValue(convertedJson, new TypeReference<Map<String, Object>>() {});

            } else {
                throw new DataException("received value is not Json or a JsonString");
            }

            return Pair.of(newValue, true);
        } catch (Exception e) {
            logger.warn("COUCHBASE_KAFKA_CONNECT_SMTS_ConversionError: Could not convert the object to regular JSON Object: ", e);
        }
        return Pair.of(value, false);
    }

}
