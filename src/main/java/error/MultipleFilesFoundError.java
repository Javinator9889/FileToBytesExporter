package error;

/**
 * Exception when multiple files where found and the option to read them all is not active
 */
public class MultipleFilesFoundError extends RuntimeException {
    /**
     * Constructs a new runtime exception with the specified detail message. The cause is not
     * initialized, and may subsequently be initialized by a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for later retrieval by the
     *                {@link #getMessage()} method.
     */
    public MultipleFilesFoundError(String message) {
        super(message);
    }
}
