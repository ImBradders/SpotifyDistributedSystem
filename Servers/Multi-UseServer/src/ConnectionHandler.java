import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Base class for handling connections to clients.
 *
 * @author Bradley Davis
 */
public class ConnectionHandler implements Runnable {
    protected Socket socket;
    protected DataInputStream dataInputStream;
    protected DataOutputStream dataOutputStream;
    protected ConnectionState connectionState;
    protected ServerConnectionDetails communicationServer;
    protected ServerConnectionDetails myStorageServer;
    protected byte[] buffer;
    protected int bytesRead;

    protected ConnectionHandler(Socket socket, ServerConnectionDetails communicationServer) {
        this.socket = socket;
        this.communicationServer = communicationServer;
    }

    @Override
    public void run() {
        //stub to allow for runnable to be implemented but this should always be overridden.
    }

    /**
     * Gets the IP and port number of a storage server to get un-cached music from.
     */
    protected void getStorageServer() {
        ConnectionState connectionState = null;
        try {
            //connect to the communication server.
            Socket communicationServerConnection = new Socket(communicationServer.getIpAddress(), communicationServer.getPortNumber());
            DataOutputStream communicationServerOutput = new DataOutputStream(communicationServerConnection.getOutputStream());
            DataInputStream communicationServerInput = new DataInputStream(communicationServerConnection.getInputStream());
            connectionState = ConnectionState.CONNECTED;

            int messageSize = 0;
            String messageReceived = null;

            //tell it that we are a server
            communicationServerOutput.write(MessageConverter.stringToByte("SERVER"));
            communicationServerOutput.flush();

            //request details for a storage server.
            communicationServerOutput.write(MessageConverter.stringToByte("GETSERVER:STORAGE"));
            communicationServerOutput.flush();

            //handle response.
            messageSize = communicationServerInput.read(buffer);
            messageReceived = MessageConverter.byteToString(buffer, messageSize);
            String[] responses = messageReceived.split(":");

            if (responses[0].equals("IP")) {
                //we got the message back fine
                String ipAddress = responses[1];
                String port = responses[3];
                int portToUse = parseInt(port);
                if (portToUse > 0) {
                    myStorageServer = new ServerConnectionDetails(ipAddress, portToUse);
                }
                else {
                    System.out.println("Error getting port number of server.");
                    myStorageServer = null;
                }
            }
            else if (responses[0].equals("ERROR")) {
                //something died.
                if (responses[1].equals("No storage server exists.")) {
                    myStorageServer = null;
                }
                else {
                    System.out.println(messageReceived);
                }
            }
            else {
                System.out.println("Some error occurred getting the storage server.");
            }

            //disconnect safely.
            communicationServerOutput.write(MessageConverter.stringToByte("DISCONNECT"));
            communicationServerOutput.flush();

            messageSize = communicationServerInput.read(buffer);
            messageReceived = MessageConverter.byteToString(buffer, messageSize);

            if (!messageReceived.equals("DISCONNECT")) {
                System.out.println("Unable to disconnect properly from communication server.");
            }

            connectionState = ConnectionState.DISCONNECTING;
            communicationServerConnection.close();
        }
        catch (UnknownHostException e) {
            e.printStackTrace();
            System.out.println("Communication server is dead.");
        }
        catch (IOException e) {
            if (connectionState != ConnectionState.DISCONNECTING) {
                System.out.println("Error getting storage server details.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Small function to parse what is expected to be a positive integer.
     *
     * @param number the number to parse.
     * @return the parsed integer or -1 if unable.
     */
    private int parseInt(String number) {
        try {
            return Integer.parseInt(number);
        }
        catch (NumberFormatException nfe) {
            return -1;
        }
    }
}
