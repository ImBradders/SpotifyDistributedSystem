import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Handles the connection for each client currently connected to the server.
 *
 * @author Bradley Davis
 */
public class ConnectionHandler implements Runnable {
    private Socket socket;
    private DataInputStream dataIn;
    private DataOutputStream dataOut;
    private SharedDataStore dataStore;
    private int bytesRead = 0;
    private byte[] buffer = new byte[100];
    private ConnectionState connectionState;

    /**
     * This constructor allows for the socket to be passed in when creating the class so that it can communicate over
     * the network.
     *
     * @param inSoc - takes the created socket to create input and output streams
     */
    public ConnectionHandler(Socket inSoc) {
        socket = inSoc;
    }

    /**
     * This allows the class to implement runnable so that the class will run as a thread when called to do so.
     */
    public void run() {
        try {
            //get the shared data store
            dataStore = SharedDataStore.getInstance();

            //attempt to get data streams
            dataIn = new DataInputStream(socket.getInputStream());
            dataOut = new DataOutputStream(socket.getOutputStream());

            //connected to endpoint successfully.
            connectionState = ConnectionState.CONNECTED;

            //before we start handling messages, get the connection type
            bytesRead = dataIn.read(buffer);
            String messagePumpToRun = MessageConverter.byteToString(buffer, bytesRead);

            if (messagePumpToRun.equals("SERVER")) {
                doServerMessagePump();
            }
            else if (messagePumpToRun.equals("CLIENT")) {
                doClientMessagePump();
            }
        }
        catch(IOException ioe) {
            //if not in quitting state, throw error
            if (connectionState != ConnectionState.DISCONNECTING) {
                ioe.printStackTrace();
            }
            else { //otherwise, run to end
                System.out.println("Connection to " + socket.getInetAddress().getHostAddress() + " closing.");
            }
        }
    }

    void doServerMessagePump() {
        try {
            while (connectionState == ConnectionState.CONNECTED) {
                //get the sent data
                buffer = new byte[100];
                bytesRead = dataIn.read(buffer);

                //convert message to string
                String messageToProcess = MessageConverter.byteToString(buffer, bytesRead);

                //this particular regex will split by any given number of spaces plus a colon.
                String[] arguments = messageToProcess.split("//s*://S*");

                //process messages
                switch(arguments[0]) {
                    //choose entered command
                    case "SERVERTYPE" :
                        //add the server to the corresponding list.
                        dataStore.addServer(socket.getInetAddress().getHostAddress(), socket.getPort(),
                                Enum.valueOf(ServerType.class, arguments[1]));

                        //inform the user that we have stored the server.
                        buffer = MessageConverter.stringToByte("TYPESTORED");
                        dataOut.write(buffer);
                        break;

                    case "GETSERVER" :
                        //here, we give the client an ip and port for a server in our list of online servers.
                        if (arguments[1].equals("STORAGE")) {
                            ServerConnectionDetails serverConnectionDetails = dataStore.getServer(Enum.valueOf(ServerType.class, arguments[1]));
                            buffer = MessageConverter.stringToByte("IP : " + serverConnectionDetails.getIpAddress() +
                                    " : PORT : " + serverConnectionDetails.getPortNumber());
                        }
                        else { //a server has tried to request something that they should not.
                            buffer = MessageConverter.stringToByte("ERROR : Incorrect server type.");
                        }
                        dataOut.write(buffer);
                        break;

                    case "DISCONNECT" :
                        connectionState = ConnectionState.DISCONNECTING;
                        buffer = MessageConverter.stringToByte("DISCONNECT");
                        dataOut.write(buffer);
                        break;

                    default:
                        //message sent was unsupported.
                        buffer = MessageConverter.stringToByte("MESSAGEUNSUPPORTED");
                        dataOut.write(buffer);
                        break;
                }
                //ensure all data is flushed before we start to handle another message.
                dataOut.flush();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    void doClientMessagePump() {
        try {
            while (connectionState == ConnectionState.CONNECTED) {
                //get the sent data
                buffer = new byte[100];
                bytesRead = dataIn.read(buffer);

                //convert message to string
                String messageToProcess = MessageConverter.byteToString(buffer, bytesRead);

                //this particular regex will split by any given number of spaces plus a colon.
                String[] arguments = messageToProcess.split("//s*://S*");

                //process messages
                switch(arguments[0]) {
                    //based on the state
                    //choose entered command
                    case "GETSERVER" :
                        //here, we give the client an ip and port for a server in our list of online servers.
                        if (arguments[1].equals("LOGIN") || arguments[1].equals("STREAMING")) {
                            ServerConnectionDetails serverConnectionDetails = dataStore.getServer(Enum.valueOf(ServerType.class, arguments[1]));
                            buffer = MessageConverter.stringToByte("IP : " + serverConnectionDetails.getIpAddress() +
                                    " : PORT : " + serverConnectionDetails.getPortNumber());
                        }
                        else { //a client has tried to request something that they should not.
                            buffer = MessageConverter.stringToByte("ERROR : Incorrect server type.");
                        }
                        dataOut.write(buffer);
                        break;

                    case "HEARTBEAT" :
                        buffer = MessageConverter.stringToByte("HEARTBEAT");
                        dataOut.write(buffer);
                        break;

                    case "DISCONNECT" :
                        connectionState = ConnectionState.DISCONNECTING;
                        buffer = MessageConverter.stringToByte("DISCONNECT");
                        dataOut.write(buffer);
                        break;

                    default:
                        //state not set properly or in bad state. Reset and terminate connection
                        buffer = MessageConverter.stringToByte("MESSAGEUNSUPPORTED");
                        dataOut.write(buffer);
                        break;
                }
                //ensure all data is flushed before we start handling the next message.
                dataOut.flush();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
