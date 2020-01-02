/**
 * Class to store the connection details for a given server within the system.
 *
 * @author Bradley Davis
 */
public class ServerConnectionDetails {
    private String ipAddress;
    private int portNumber;

    /**
     * Constructor which sets all internal class variables.
     *
     * @param ipAddress the IP address of the server.
     * @param portNumber the port number on which the server will listen for connections.
     */
    ServerConnectionDetails(String ipAddress, int portNumber) {
        this.ipAddress = ipAddress;
        this.portNumber = portNumber;
    }

    /**
     * Method to get the IP address of the server.
     *
     * @return the IP address of the server.
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Method to set the IP address of the server.
     *
     * @param ipAddress the IP address on which the server can be located.
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * Method to get the port number on which the server will be listening for connections.
     *
     * @return the port number which the server will listen on.
     */
    public int getPortNumber() {
        return portNumber;
    }

    /**
     * Method to set the port number on which the server will be listening for connections.
     *
     * @param portNumber the port number on which the server will be listening for connections.
     */
    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }
}
