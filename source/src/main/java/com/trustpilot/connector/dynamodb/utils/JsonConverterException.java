package com.trustpilot.connector.dynamodb.utils;

/**
 * Exception occurred while transforming between representations.
 */
public class JsonConverterException extends Exception {

    /**
     * Serial Version.
     */
    private static final long serialVersionUID = 8457313895307710705L;

    /**
     * Constructs a new {@link JsonConverterException} with the provided message.
     *
     * @param message Error message detailing exception
     */
    public JsonConverterException(final String message) {
        super(message);
    }

}
