package com.trustpilot.connector.dynamodb.utils;

import com.fasterxml.jackson.databind.JsonNode;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;

public interface IJsonConverter {

    /**
     * Converts a list of maps of AttributeValues to a JsonNode instance that represents the list of maps.
     *
     * @param items A list of maps of AttributeValues
     * @return A JsonNode instance that represents the converted JSON array.
     * @throws JsonConverterException Error converting DynamoDB item to JSON
     */
    JsonNode itemListToJsonArray(List<Map<String, AttributeValue>> items) throws JsonConverterException;

    /**
     * Converts a JSON array to a list of AttributeValues.
     *
     * @param array A JsonNode instance that represents the target JSON array.
     * @return A list of AttributeValues that represents the JSON array.
     * @throws JsonConverterException if JsonNode is not an array
     */
    List<AttributeValue> jsonArrayToList(JsonNode array) throws JsonConverterException;

    /**
     * Converts a JSON object to a map of AttributeValues.
     *
     * @param object A JsonNode instance that represents the target JSON object.
     * @return A map of AttributeValues that represents the JSON object.
     * @throws JsonConverterException if JsonNode is not an object.
     */
    Map<String, AttributeValue> jsonObjectToMap(JsonNode object) throws JsonConverterException;

    /**
     * Converts a list of AttributeValues to a JsonNode instance that represents the list.
     *
     * @param list A list of AttributeValues
     * @return A JsonNode instance that represents the converted JSON array.
     * @throws JsonConverterException Error converting DynamoDB item to JSON
     */
    JsonNode listToJsonArray(List<AttributeValue> list) throws JsonConverterException;

    /**
     * Converts a map of AttributeValues to a JsonNode instance that represents the map.
     *
     * @param map A map of AttributeValues
     * @return A JsonNode instance that represents the converted JSON object.
     * @throws JsonConverterException Error converting DynamoDB item to JSON
     */
    JsonNode mapToJsonObject(Map<String, AttributeValue> map) throws JsonConverterException;
}

