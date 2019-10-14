package Server;

/**
 * ByteConversionError class to allow me to catch conversion errors better
 *
 * @author Bradley Davis
 */
class ByteConversionError extends Exception{

    /**
     * Constructor to add a message to the error
     *
     * @param message the message string
     */
	public ByteConversionError(String message) {

	    super(message);
	}
}
