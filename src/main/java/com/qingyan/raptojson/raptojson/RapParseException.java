package com.qingyan.raptojson.raptojson;

/**
 * @author xuzhou
 * @since 2022/8/5
 */
public class RapParseException extends Exception{
    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public RapParseException(String message) {
        super(message);
    }
}
