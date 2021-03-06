package com.github.javinator9889.error;

/**
 * If the used file separator is not valid, this exception class is called
 */
public class InvalidFileSeparatorException extends RuntimeException {
    /**
     * Constructs a new runtime exception with the specified detail message. The cause is not
     * initialized, and may subsequently be initialized by a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for later retrieval by the
     *                {@link #getMessage()} method.
     */
    public InvalidFileSeparatorException(String message) {
        super(message);
    }
}
