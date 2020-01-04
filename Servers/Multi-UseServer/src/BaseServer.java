import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Base server class for the others to be built on top of.
 *
 * @author Bradley Davis
 */
public class BaseServer {
    protected int portNumber;

    /**
     * Constructor for base class to set up the necessary internal variables.
     *
     * @param portNumber the port number on which this server will listen for communications.
     */
    public BaseServer(int portNumber) {
        this.portNumber = portNumber;
    }

    /**
     * Starts the server running. This should always be overridden so it will return false here.
     *
     * @return whether the server was successfully started or not.
     */
    protected boolean start() {
        return false;
    }

    /**
     * Contacts the communication server to tell it that we exist. This should always be overridden so it will return false here.
     *
     * @return whether the communication was successful or not.
     */
    protected boolean contactCommunicationServer() {
        return false;
    }

    protected ServerConnectionDetails getCommunicationServerDetails() throws IOException {
        String ipAddress = null;
        int portNumber = 0;
        try {
            File file = new File("CommunicationServerIP.txt");
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String currentLine = bufferedReader.readLine();

            while (currentLine != null) {
                String[] lineData = currentLine.split(" : ");

                switch (lineData[0]) {
                    case "IP" :
                        ipAddress = lineData[1];
                        break;
                    case "PORT" :
                        portNumber = Integer.parseInt(lineData[1]);
                        break;
                    default:
                        System.out.println("Part of CommunicationServerIP.txt was unreadable.");
                        break;
                }

                currentLine = bufferedReader.readLine();
            }

            fileReader.close();
        }
        catch (NumberFormatException nfe) {
            System.out.println("Unable to read communication server port number as integer - shutting down.");
            throw new IOException("Error reading communication server port number from file.");
        }
        catch (IOException ioe) {
            System.out.println("Unable to load information for communication server - shutting down.");
            throw ioe;
        }

        if (ipAddress == null || portNumber == 0) {
            System.out.println("Server or port number was missing - shutting down.");
            throw new IOException("Server or port number was missing/");
        }

        return new ServerConnectionDetails(ipAddress, portNumber);
    }
}
