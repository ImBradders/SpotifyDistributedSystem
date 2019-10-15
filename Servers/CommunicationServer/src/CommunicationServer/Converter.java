package Server;

import java.nio.charset.StandardCharsets;

/**
 * The converter class allows me to convert byte arrays that have been passed over the network to strings.
 * I can then store the strings and compare them with things in the program.
 *
 * @author Bradley Davis
 */
class Converter{
    /**
     * This method converts a byte array to an ASCII string and converts any characters that aren't in the character set
     * to a standard character that is in the set.
     *
     * @param b the byte array to be converted
     * @return a string containing only ASCII characters
     * @throws ByteConversionError if either the byte array or the string becomes null
     */
	public static String byteToString(byte[] b) throws ByteConversionError{
		if(b == null){
			throw new ByteConversionError("Byte array was null in conversion.");
		}

		String s = new String(b, StandardCharsets.US_ASCII);

		if(s == null){
			throw new ByteConversionError("Generated string was null in conversion.");
		}

		return s;
	}

    /**
     * This method converts an string to a byte array that will only contain ASCII characters.
     *
     * @param s the string to be converted
     * @return a byte array containing the byte values of ASCII characters only
     * @throws ByteConversionError if either the string or the byte array becomes null
     */
	public static byte[] stringToByte(String s) throws ByteConversionError{
		if(s == null){
			throw new ByteConversionError("String to convert was null");
		}

		byte[] b = s.getBytes(StandardCharsets.US_ASCII);

		if(b == null){
			throw new ByteConversionError("Byte array created was null");
		}

		return b;
	}

    /**
     * This method converts a string to an ASCII string to ensure that there are no
     *
     * @param s the string to be converted
     * @return a string containing only ASCII characters
     * @throws ByteConversionError if there are any errors in the conversion
     */
	public static String stringToASCII(String s) throws ByteConversionError{
		try{
			byte[] b = Converter.stringToByte(s);
			return Converter.byteToString(b);
		}
		catch(ByteConversionError bce){
			throw new ByteConversionError("Error converting string to ascii string");
		}
	}
}
