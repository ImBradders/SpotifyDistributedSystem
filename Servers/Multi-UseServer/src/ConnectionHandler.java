import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

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
    protected BaseServer parent;
    protected byte[] buffer;
    protected int bytesRead;

    protected ConnectionHandler(Socket socket, ServerConnectionDetails communicationServer, BaseServer parent) {
        this.socket = socket;
        this.communicationServer = communicationServer;
        this.parent = parent;
        this.buffer = new byte[200];
        bytesRead = 0;
    }

    @Override
    public void run() {
        //stub to allow for runnable to be implemented but this should always be overridden.
    }

    /**
     * Gets the IP and port number of a storage server to get un-cached music from.
     */
    protected void getStorageServer() {
        ConnectionState communicationServerConnectionState = null;
        try {
            //connect to the communication server.
            Socket communicationServerConnection = new Socket(communicationServer.getIpAddress(), communicationServer.getPortNumber());
            DataOutputStream communicationServerOutput = new DataOutputStream(communicationServerConnection.getOutputStream());
            DataInputStream communicationServerInput = new DataInputStream(communicationServerConnection.getInputStream());
            communicationServerConnectionState = ConnectionState.CONNECTED;

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

            communicationServerConnectionState = ConnectionState.DISCONNECTING;
            communicationServerConnection.close();
        }
        catch (UnknownHostException e) {
            e.printStackTrace();
            System.out.println("Communication server is dead.");
        }
        catch (IOException e) {
            if (communicationServerConnectionState != ConnectionState.DISCONNECTING) {
                System.out.println("Error getting storage server details.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Sends a list of messages to the storage server and stores the responses that it gets back.
     *
     * @param messages the list of messages to be sent to the storage server. This should not include the disconnect message.
     * @return the messages returned from the storage server.
     */
    protected List<String> messageStorageServer(List<String> messages) {
        List<String> replies = new ArrayList<String>();
        ConnectionState storageServerConnectionState = ConnectionState.CONNECTED;
        Socket storageServer = null;
        if (myStorageServer == null) {
            //if we have no storage server, attempt to get it one more time.
            getStorageServer();
            if (myStorageServer == null) {
                replies.add("ERROR:Storage server inaccessible.");
                return replies;
            }
        }

        try {
            storageServer = new Socket(myStorageServer.getIpAddress(), myStorageServer.getPortNumber());
            DataOutputStream storageServerOut = new DataOutputStream(storageServer.getOutputStream());
            DataInputStream storageServerIn = new DataInputStream(storageServer.getInputStream());

            byte[] buffer = new byte[200];
            int bytesRead = 0;

            //send all messages
            for (String message : messages) {
                storageServerOut.write(MessageConverter.stringToByte(message));
                storageServerOut.flush();
                bytesRead = storageServerIn.read(buffer);
                replies.add(MessageConverter.byteToString(buffer, bytesRead));
            }

            //safely disconnect
            storageServerOut.write(MessageConverter.stringToByte("DISCONNECT"));
            storageServerOut.flush();
            storageServerConnectionState = ConnectionState.DISCONNECTING; //this is set here as the storage server may close the socket before we process its reply.
            bytesRead = storageServerIn.read(buffer);
            String message = MessageConverter.byteToString(buffer, bytesRead);
            if (message.equalsIgnoreCase("DISCONNECT")) {
                storageServer.close();
            }
        }
        catch (UnknownHostException e) {
            replies.add("ERROR:Storage server inaccessible.");
        }
        catch (IOException e) {
            if (storageServerConnectionState != ConnectionState.DISCONNECTING) {
                //we died before our time
                replies.add("ERROR:Storage server inaccessible.");
            }
        }
        finally {
            try {
                if (storageServer != null && storageServer.isClosed())
                {
                    storageServer.close();
                }
            }
            catch (IOException e) {
                //we couldnt close the socket but we can ignore this.
                e.printStackTrace();
            }

        }

        return replies;
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

    protected void done(String serverType, int portNumber) {
        ConnectionState communicationServerConnectionState = ConnectionState.CONNECTED;
        Socket communicationServerSocket = null;

        if (communicationServer == null) {
            return;
        }

        try {
            byte[] communicationServerBuffer = new byte[200];
            int communicationServerBytesRead = 0;

            communicationServerSocket = new Socket(communicationServer.getIpAddress(), communicationServer.getPortNumber());
            DataInputStream communicationServerInputStream = new DataInputStream(communicationServerSocket.getInputStream());
            DataOutputStream communicationServerOutputStream = new DataOutputStream(communicationServerSocket.getOutputStream());

            communicationServerOutputStream.write(MessageConverter.stringToByte("SERVER"));
            communicationServerOutputStream.flush();

            communicationServerOutputStream.write(MessageConverter.stringToByte("DROPPED:"+serverType+":"+portNumber));
            communicationServerOutputStream.flush();
            communicationServerBytesRead = communicationServerInputStream.read(communicationServerBuffer);
            String message = MessageConverter.byteToString(communicationServerBuffer, communicationServerBytesRead);
            //we get this reply but we don't really care about it.

            communicationServerOutputStream.write(MessageConverter.stringToByte("DISCONNECT"));
            communicationServerOutputStream.flush();
            communicationServerConnectionState = ConnectionState.DISCONNECTING;
            communicationServerBytesRead = communicationServerInputStream.read(communicationServerBuffer);

            if (MessageConverter.byteToString(buffer, communicationServerBytesRead).equals("DISCONNECT")) {
                socket.close();
            }
        }
        catch (UnknownHostException e) {
            System.out.println("Unable to contact communication server.");
            e.printStackTrace();
        }
        catch (IOException e) {
            if (communicationServerConnectionState != ConnectionState.DISCONNECTING) {
                e.printStackTrace();
            }
        }
        finally {
            if (!socket.isClosed()) {
                try {
                    socket.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
