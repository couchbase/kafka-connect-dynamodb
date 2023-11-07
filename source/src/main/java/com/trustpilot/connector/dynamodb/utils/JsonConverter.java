package com.trustpilot.connector.dynamodb.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the {@link IJsonConverter}.
 */
public class JsonConverter implements IJsonConverter {
    /**
     * Maximum JSON depth.
     */
    private static final int MAX_DEPTH = 50;

    /**
     * Constructs a {@link JsonConverter}.
     */
    public JsonConverter() {
    }

    /**
     * Asserts the depth is not greater than {@link #MAX_DEPTH}.
     *
     * @param depth Current JSON depth
     * @throws JsonConverterException Depth is greater than {@link #MAX_DEPTH}
     */
    private void assertDepth(final int depth) throws JsonConverterException {
        if (depth > MAX_DEPTH) {
            throw new JsonConverterException("Max depth reached. The object/array has too much depth.");
        }
    }

    /**
     * Gets an DynamoDB representation of a JsonNode.
     *
     * @param node The JSON to convert
     * @param depth Current JSON depth
     * @return DynamoDB representation of the JsonNode
     * @throws JsonConverterException Unknown JsonNode type or JSON is too deep
     */
    private AttributeValue getAttributeValue(final JsonNode node, final int depth) throws JsonConverterException {
        assertDepth(depth);

        switch (node.asToken()) {
            case VALUE_STRING:
                return AttributeValue.builder().s(node.textValue()).build();
            case VALUE_NUMBER_INT:
            case VALUE_NUMBER_FLOAT:
                return AttributeValue.builder().n(node.numberValue().toString()).build();
            case VALUE_TRUE:
            case VALUE_FALSE:
                return AttributeValue.builder().bool(node.booleanValue()).build();
            case VALUE_NULL:
                return AttributeValue.builder().nul(true).build();
            case START_OBJECT:
                return AttributeValue.builder().m(jsonObjectToMap(node, depth)).build();
            case START_ARRAY:
                return AttributeValue.builder().l(jsonArrayToList(node, depth)).build();
            default:
                throw new JsonConverterException("Unknown node type: " + node);
        }

    }

    /**
     * Converts a DynamoDB attribute to a JSON representation.
     *
     * @param av DynamoDB attribute
     * @param depth Current JSON depth
     * @return JSON representation of the DynamoDB attribute
     * @throws JsonConverterException Unknown DynamoDB type or JSON is too deep
     */
    private JsonNode getJsonNode(final AttributeValue av, final int depth) throws JsonConverterException {
        assertDepth(depth);

        // Checking for textNode
        if (av.s() != null) {
            return JsonNodeFactory.instance.textNode(av.s());
        }

        // Checking for NumberNode
        if (av.n() != null) {
            try {
                return JsonNodeFactory.instance.numberNode(Integer.parseInt(av.n()));
            } catch (final NumberFormatException e) {
                // Not an integer
                try {
                    return JsonNodeFactory.instance.numberNode(Float.parseFloat(av.n()));
                } catch (final NumberFormatException e2) {
                    // Not a number
                    throw new JsonConverterException(e.getMessage());
                }
            }
        }

        // Checking for Bool
        if (av.bool() != null) {
            return JsonNodeFactory.instance.booleanNode(av.bool());
        }

        // Checking for NullNode
        if (av.nul() != null) {
            return JsonNodeFactory.instance.nullNode();
        }

        // Checking for List
        if (av.l() != null) {
            return listToJsonArray(av.l(), depth);
        }

        // Checking for Object
        if (av.m() != null) {
            return mapToJsonObject(av.m(), depth);
        }

        // Checking for List of NumberAttributes
        if (av.ns() != null) {
            List<AttributeValue> numberAttributes = new ArrayList<>();
            for (final String value : av.ns()) {
                numberAttributes.add(AttributeValue.builder().n(value).build());
            }
            return listToJsonArray(numberAttributes);
        }

        // Checking for List of StringAttributes
        if (av.ss() != null) {
            List<AttributeValue> stringAttributes = new ArrayList<>();
            for (final String value : av.ss()) {
                stringAttributes.add(AttributeValue.builder().s(value).build());
            }
            return listToJsonArray(stringAttributes);
        }

        throw new JsonConverterException("Unknown type value " + av);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonNode itemListToJsonArray(final List<Map<String, AttributeValue>> items) throws JsonConverterException {
        if (items != null) {
            final ArrayNode array = JsonNodeFactory.instance.arrayNode();
            for (final Map<String, AttributeValue> item : items) {
                array.add(mapToJsonObject(item, 0));
            }
            return array;

        }
        throw new JsonConverterException("Items cannnot be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AttributeValue> jsonArrayToList(final JsonNode node) throws JsonConverterException {
        return jsonArrayToList(node, 0);
    }

    /**
     * Helper method to convert a JsonArrayNode to a DynamoDB list.
     *
     * @param node Array node to convert
     * @param depth Current JSON depth
     * @return DynamoDB list representation of the array node
     * @throws JsonConverterException JsonNode is not an array or depth is too great
     */
    private List<AttributeValue> jsonArrayToList(final JsonNode node, final int depth) throws JsonConverterException {
        assertDepth(depth);

        if (node != null && node.isArray()) {
            final List<AttributeValue> result = new ArrayList<AttributeValue>();
            final Iterator<JsonNode> children = node.elements();

            while (children.hasNext()) {
                final JsonNode child = children.next();
                result.add(getAttributeValue(child, depth));
            }

            return result;
        }

        throw new JsonConverterException("Expected JSON array, but received " + node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, AttributeValue> jsonObjectToMap(final JsonNode node) throws JsonConverterException {
        return jsonObjectToMap(node, 0);
    }

    /**
     * Transforms a JSON object to a DynamoDB object.
     *
     * @param node JSON object
     * @param depth Current JSON depth
     * @return DynamoDB object representation of JSON
     * @throws JsonConverterException JSON is not an object or depth is too great
     */
    private Map<String, AttributeValue> jsonObjectToMap(final JsonNode node, final int depth) throws JsonConverterException {
        assertDepth(depth);

        if (node != null && node.isObject()) {
            final Map<String, AttributeValue> result = new HashMap<String, AttributeValue>();
            final Iterator<String> keys = node.fieldNames();

            while (keys.hasNext()) {
                final String key = keys.next();
                result.put(key, getAttributeValue(node.get(key), depth + 1));
            }

            return result;
        }

        throw new JsonConverterException("Expected JSON Object, but received " + node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonNode listToJsonArray(final List<AttributeValue> item) throws JsonConverterException {
        return listToJsonArray(item, 0);
    }

    /**
     * Converts a DynamoDB list to a JSON list.
     *
     * @param item DynamoDB list
     * @param depth Current JSON depth
     * @return JSON array node representation of DynamoDB list
     * @throws JsonConverterException Null DynamoDB list or JSON too deep
     */
    private JsonNode listToJsonArray(final List<AttributeValue> item, final int depth) throws JsonConverterException {
        assertDepth(depth);

        if (item != null) {
            final ArrayNode node = JsonNodeFactory.instance.arrayNode();

            for (final AttributeValue value : item) {
                node.add(getJsonNode(value, depth + 1));
            }
            return node;
        }

        throw new JsonConverterException("Item cannot be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonNode mapToJsonObject(final Map<String, AttributeValue> item) throws JsonConverterException {
        return mapToJsonObject(item, 0);
    }

    /**
     * Converts a DynamoDB object to a JSON map.
     *
     * @param item DynamoDB object
     * @param depth Current JSON depth
     * @return JSON map representation of the DynamoDB object
     * @throws JsonConverterException Null DynamoDB object or JSON too deep
     */
    private JsonNode mapToJsonObject(final Map<String, AttributeValue> item, final int depth) throws JsonConverterException {
        assertDepth(depth);

        if (item != null) {
            final ObjectNode node = JsonNodeFactory.instance.objectNode();

            for (final Map.Entry<String, AttributeValue> entry : item.entrySet()) {
                node.put(entry.getKey(), getJsonNode(entry.getValue(), depth + 1));
            }
            return node;
        }

        throw new JsonConverterException("Item cannot be null");
    }

}

