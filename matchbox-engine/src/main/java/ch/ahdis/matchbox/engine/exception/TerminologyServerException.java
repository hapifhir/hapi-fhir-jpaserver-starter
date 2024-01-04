package ch.ahdis.matchbox.engine.exception;

/**
 * Exception in relation to the terminology server.
 *
 * @author Quentin Ligier
 */
public class TerminologyServerException extends MatchboxEngineCreationException {

	/**
	 * Constructs a new exception with the specified detail message. The cause is not initialized, and may
	 * subsequently be initialized by a call to {@link #initCause}.
	 *
	 * @param message the detail message. The detail message is saved for later retrieval by the {@link #getMessage()}
	 *                method.
	 */
	public TerminologyServerException(final String message) {
		super(message);
	}

	/**
	 * Constructs a new exception with the specified detail message and cause.  <p>Note that the detail message
	 * associated with {@code cause} is <i>not</i> automatically incorporated in this runtime exception's detail message.
	 *
	 * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method).
	 * @param cause   the cause (which is saved for later retrieval by the {@link #getCause()} method).  (A {@code null}
	 *                value is permitted, and indicates that the cause is nonexistent or unknown.)
	 * @since 1.4
	 */
	public TerminologyServerException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new exception with the specified cause and a detail message of
	 * {@code (cause==null ? null : cause.toString())} (which typically contains the class and detail message of
	 * {@code cause}).  This constructor is useful for runtime exceptions that are little more than wrappers for other
	 * throwables.
	 *
	 * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).  (A {@code null} value
	 *              is permitted, and indicates that the cause is nonexistent or unknown.)
	 * @since 1.4
	 */
	public TerminologyServerException(final Throwable cause) {
		super("Terminology server: " + cause.getMessage(), cause);
	}
}
