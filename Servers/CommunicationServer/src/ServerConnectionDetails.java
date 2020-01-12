/**
 * Class to store the connection details for a given server within the system.
 *
 * @author Bradley Davis
 */
public class ServerConnectionDetails {
    private String ipAddress;
    private int portNumber;
    private int currentClients;

    /**
     * Constructor which sets all internal class variables.
     *
     * @param ipAddress the IP address of the server.
     * @param portNumber the port number on which the server will listen for connections.
     */
    ServerConnectionDetails(String ipAddress, int portNumber) {
        this.ipAddress = ipAddress;
        this.portNumber = portNumber;
        this.currentClients = 0;
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

    /**
     * Method to get the current number of clients connected to this server.
     *
     * @return the number of clients that the communication server thinks that this server has on it.
     */
    public int getCurrentClients() {
        return currentClients;
    }

    /**
     * Adds a client to the number of clients that the communication server believes are currently connected to this server.
     */
    public void addClient() {
        currentClients += 1;
    }

    /**
     * Removes a client from the number of clients that the communication server currently believes is connected to this server.
     */
    public void removeClient () {
        currentClients -= 1;
    }
}
