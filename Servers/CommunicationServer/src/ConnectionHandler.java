import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Random;

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
    private String messageToProcess;
    private ConnectionState connectionState;
    private boolean isNetwork;

    /**
     * This constructor allows for the socket to be passed in when creating the class so that it can communicate over
     * the network.
     *
     * @param inSoc - takes the created socket to create input and output streams
     */
    public ConnectionHandler(Socket inSoc) {
        socket = inSoc;
        dataStore = SharedDataStore.getInstance();
        isNetwork = false;
    }

    /**
     * This allows the class to implement runnable so that the class will run as a thread when called to do so.
     */
    @Override
    public void run() {
        try {
            //attempt to get data streams
            dataIn = new DataInputStream(socket.getInputStream());
            dataOut = new DataOutputStream(socket.getOutputStream());

            //connected to endpoint successfully.
            connectionState = ConnectionState.CONNECTED;

            //before we start handling messages, get the connection type
            bytesRead = dataIn.read(buffer);
            String messagePumpToRun = MessageConverter.byteToString(buffer, bytesRead);

            if (messagePumpToRun.startsWith("SERVER")) {
                messageToProcess = messagePumpToRun.substring(6);
                doServerMessagePump();
                if (isNetwork) {
                    doNetwork();
                }
            }
            else if (messagePumpToRun.startsWith("CLIENT")) {
                messageToProcess = messagePumpToRun.substring(6);
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

    /**
     * Runs the network server message pump for this connection.
     */
    private void doNetwork() {
        Random random = new Random(System.currentTimeMillis());
        while (true) {
            try {
                Thread.sleep(random.nextInt(2000));
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }

            String message = dataStore.getNetworkMessage();

            if (message != null) {
                try {
                    dataOut.write(MessageConverter.stringToByte(message));
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Runs the server message pump for this connection.
     */
    void doServerMessagePump() {
        try {
            while (connectionState == ConnectionState.CONNECTED && !isNetwork) {
                if (messageToProcess.length() == 0) {
                    //get the sent data
                    buffer = new byte[100];
                    bytesRead = dataIn.read(buffer);

                    //convert message to string
                    messageToProcess = MessageConverter.byteToString(buffer, bytesRead);
                }

                String[] arguments = messageToProcess.split(":");

                //process messages
                switch(arguments[0]) {
                    //choose entered command
                    case "SERVERTYPE" :
                        //add the server to the corresponding list.
                        int portNumber = Integer.parseInt(arguments[2]);
                        dataStore.addServer(socket.getInetAddress().getHostAddress(), portNumber,
                                Enum.valueOf(ServerType.class, arguments[1]));

                        if (arguments[1].equalsIgnoreCase("NETWORK")) {
                            //this connection handler is special
                            isNetwork = true;
                        }

                        //inform the user that we have stored the server.
                        buffer = MessageConverter.stringToByte("TYPESTORED");
                        dataOut.write(buffer);
                        messageToProcess = messageToProcess.substring(arguments[0].length() + 1 + arguments[1].length() + 1 + arguments[2].length());
                        break;

                    case "GETSERVER" :
                        //here, we give the server an ip and port for a server in our list of online servers.
                        if (arguments[1].equals("STORAGE")) {
                            ServerConnectionDetails serverConnectionDetails = dataStore.getServer(Enum.valueOf(ServerType.class, arguments[1]));
                            if (serverConnectionDetails == null) {
                                buffer = MessageConverter.stringToByte("ERROR:No storage server exists.");
                            }
                            else {
                                buffer = MessageConverter.stringToByte("IP:" + serverConnectionDetails.getIpAddress() +
                                        ":PORT:" + serverConnectionDetails.getPortNumber());
                            }
                        }
                        else { //a server has tried to request something that they should not.
                            buffer = MessageConverter.stringToByte("ERROR:Incorrect server type.");
                        }
                        dataOut.write(buffer);
                        messageToProcess = messageToProcess.substring(arguments[0].length() + 1 + arguments[1].length());
                        break;

                    case "DISCONNECT" :
                        connectionState = ConnectionState.DISCONNECTING;
                        buffer = MessageConverter.stringToByte("DISCONNECT");
                        dataOut.write(buffer);
                        messageToProcess = messageToProcess.substring(arguments[0].length());
                        break;

                    case "DROPPED" :
                        int portNumberDropped = Integer.parseInt(arguments[2]);
                        dataStore.droppedClient(socket.getInetAddress().getHostAddress(), portNumberDropped,
                                Enum.valueOf(ServerType.class, arguments[1]));
                        buffer = MessageConverter.stringToByte("DROPPED");
                        dataOut.write(buffer);
                        messageToProcess = messageToProcess.substring(arguments[0].length() + 1 + arguments[1].length() + 1 + arguments[2].length());
                        break;

                    default:
                        //message sent was unsupported.
                        buffer = MessageConverter.stringToByte("MESSAGEUNSUPPORTED");
                        dataOut.write(buffer);
                        messageToProcess = ""; //there is no real way of handling this.
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

    /**
     * Runs the client message pump for this connection.
     */
    void doClientMessagePump() {
        try {
            while (connectionState == ConnectionState.CONNECTED) {
                if (messageToProcess.length() == 0) {
                    //get the sent data
                    buffer = new byte[100];
                    bytesRead = dataIn.read(buffer);

                    //convert message to string
                    messageToProcess = MessageConverter.byteToString(buffer, bytesRead);
                }

                String[] arguments = messageToProcess.split(":");

                //process messages
                switch(arguments[0]) {
                    //based on the state
                    //choose entered command
                    case "GETSERVER" :
                        //here, we give the client an ip and port for a server in our list of online servers.
                        if (arguments[1].equals("LOGIN") || arguments[1].equals("STREAMING")) {
                            ServerConnectionDetails serverConnectionDetails = dataStore.getServer(Enum.valueOf(ServerType.class, arguments[1]));
                            if (serverConnectionDetails == null) {
                                buffer = MessageConverter.stringToByte("ERROR:No server of type '" + arguments[1] + "' exists.");
                            }
                            else {
                                buffer = MessageConverter.stringToByte("IP:" + serverConnectionDetails.getIpAddress() +
                                        ":PORT:" + serverConnectionDetails.getPortNumber());
                            }
                        }
                        else { //a client has tried to request something that they should not.
                            buffer = MessageConverter.stringToByte("ERROR:Incorrect server type.");
                        }
                        dataOut.write(buffer);
                        messageToProcess = messageToProcess.substring(arguments[0].length() + 1 + arguments[1].length());
                        break;

                    case "HEARTBEAT" :
                        buffer = MessageConverter.stringToByte("HEARTBEAT");
                        dataOut.write(buffer);
                        messageToProcess = messageToProcess.substring(arguments[0].length());
                        break;

                    case "DISCONNECT" :
                        connectionState = ConnectionState.DISCONNECTING;
                        buffer = MessageConverter.stringToByte("DISCONNECT");
                        dataOut.write(buffer);
                        messageToProcess = messageToProcess.substring(arguments[0].length());
                        break;

                    default:
                        //state not set properly or in bad state. Reset and terminate connection
                        buffer = MessageConverter.stringToByte("MESSAGEUNSUPPORTED");
                        dataOut.write(buffer);
                        messageToProcess = "";
                        break;
                }
                //ensure all data is flushed before we start handling the next message.
                dataOut.flush();
            }
        }
        catch (SocketException se) {
            if (connectionState != ConnectionState.DISCONNECTING)
            {
                se.printStackTrace();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
