import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Class which will be used to stream music to the client.
 *
 * @author Bradley Davis
 */
public class StreamingServer extends BaseServer {

    public StreamingServer(int portNumber) {
        super(portNumber);
    }

    /**
     * Starts the server indication to the communication server that this is a streaming server.
     *
     * @return whether or not the server started successfully.
     */
    @Override
    public boolean start()
    {
        try {
            //Set up storage
            String fileSeparator = System.getProperty("file.separator");
            File file = new File(System.getProperty("user.dir") + fileSeparator + "MusicCache");
            if (!file.exists()) {
                file = file.mkdirs() ? file : new File(System.getProperty("user.dir"));
                // if the file didn't get created, resort to a location that does exist.
            }
            String musicCache = file.toString();

            if (!(file.canWrite() && file.canRead())) {
                //we cannot read and write to the specified file location - therefore we are useless.
                return false;
            }

            communicationServerDetails = getCommunicationServerDetails();

            boolean communicationServerContacted = contactCommunicationServer();

            if (!communicationServerContacted) {
                return false;
            }

            //create server socket for client communication
            ServerSocket serverSocket = new ServerSocket(portNumber);
            //set a 60 second timeout on the server socket.
            serverSocket.setSoTimeout(60000);
            boolean firstTime = true;

            while (true) {
                if (!firstTime && numConnections == 0) {
                    //I cannot think of another way out of this loop.
                    break;
                }

                System.out.println("Awaiting clients to stream to...");

                try {
                    Socket socket = serverSocket.accept();
                    numConnections++;

                    StreamingConnectionHandler streamingConnectionHandler = new StreamingConnectionHandler(socket, musicCache, communicationServerDetails, this);
                    Thread connectionHandler = new Thread(streamingConnectionHandler);
                    connectionHandler.start();
                    if (firstTime) {
                        firstTime = false;
                    }
                }
                catch (IOException acceptFailed) {
                    System.out.println("Accept timed out.");
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Sends the communication to the communication server to tell it that this is a streaming server and that it exists.
     *
     * @return whether or not the communication was successful.
     */
    @Override
    protected boolean contactCommunicationServer() {
        ConnectionState connectionState = null;
        try {
            Socket communicationServerConnection = new Socket(communicationServerDetails.getIpAddress(), communicationServerDetails.getPortNumber());
            DataOutputStream communicationServerOutput = new DataOutputStream(communicationServerConnection.getOutputStream());
            DataInputStream communicationServerInput = new DataInputStream(communicationServerConnection.getInputStream());
            connectionState = ConnectionState.CONNECTED;

            //send necessary commands to communication server to tell it that we exist.
            byte[] buffer = new byte[100];
            int messageSize = 0;
            String messageReceived = null;

            communicationServerOutput.write(MessageConverter.stringToByte("SERVER"));
            communicationServerOutput.flush();

            communicationServerOutput.write(MessageConverter.stringToByte("SERVERTYPE:STREAMING:"+portNumber));
            communicationServerOutput.flush();

            messageSize = communicationServerInput.read(buffer);
            messageReceived = MessageConverter.byteToString(buffer, messageSize);

            if (!messageReceived.equals("TYPESTORED")) {
                System.out.println("Communication server was unable to recognise this server - shutting down.");
                return false;
            }

            communicationServerOutput.write(MessageConverter.stringToByte("DISCONNECT"));
            communicationServerOutput.flush();

            messageSize = communicationServerInput.read(buffer);
            messageReceived = MessageConverter.byteToString(buffer, messageSize);

            if (!messageReceived.equals("DISCONNECT")) {
                System.out.println("Unable to disconnect properly from communication server - shutting down.");
                return false;
            }

            connectionState = ConnectionState.DISCONNECTING;
            communicationServerConnection.close();
        }
        catch (IOException e) {
            if (connectionState != ConnectionState.DISCONNECTING)
            {
                System.out.println("Error in contacting communication server - shutting down.");
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }
}
