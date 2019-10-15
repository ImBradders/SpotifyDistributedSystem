import java.nio.charset.StandardCharsets;

/**
 * Allows for the conversion of various data types to bytes to send over the network.
 *
 * @author Bradley Davis
 */
public class MessageConverter {
    /**
     * This method converts a byte array to an UTF 8 string and converts any characters that aren't in the character set
     * to a standard character that is in the set.
     *
     * @param b the byte array to be converted
     * @return a string containing only UTF 8 characters
     */
    public static String byteToString(byte[] b) {
        if(b == null){
            return "";
        }

        return new String(b, StandardCharsets.UTF_8);
    }

    /**
     * This method converts an string to a byte array that will only contain UTF 8 characters.
     *
     * @param s the string to be converted
     * @return a byte array containing the byte values of ASCII characters only
     */
    public static byte[] stringToByte(String s) {
        if(s == null){
            return "".getBytes();
        }

        return s.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * This method converts a string to an UTF 8 string to ensure that there are no
     *
     * @param s the string to be converted
     * @return a string containing only UTF 8 characters
     */
    public static String stringToUTF8(String s) {
        byte[] b = MessageConverter.stringToByte(s);
        return MessageConverter.byteToString(b);
    }
}
