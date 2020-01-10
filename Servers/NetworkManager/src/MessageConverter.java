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
    public static String byteToString(byte[] b, int bytesToReadTo) {
        if (b == null) {
            return "";
        }
        else if (b.length == bytesToReadTo) {
            return new String(b, StandardCharsets.UTF_8);
        }
        else {
            byte[] toConvert = new byte[bytesToReadTo];
            for (int i = 0; i < bytesToReadTo; i++) {
                toConvert[i] = b[i];
            }
            return new String(toConvert, StandardCharsets.UTF_8);
        }
    }

    //This could do with adjusting to see if there is a way to ensure that this does not return a new buffer but use the old one instead.
    /**
     * This method converts an string to a byte array that will only contain UTF 8 characters.
     *
     * @param s the string to be converted
     * @return a byte array containing the byte values of ASCII characters only
     */
    public static byte[] stringToByte(String s) {
        if (s == null) {
            return "".getBytes();
        }

        return s.getBytes(StandardCharsets.UTF_8);
    }
}
