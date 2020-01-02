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
    private boolean isConnected;

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
            isConnected = true;

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

            //otherwise, run to end
        }
    }

    void doServerMessagePump() {
        try {
            while (isConnected) {
                //get the sent data
                buffer = new byte[100];
                bytesRead = dataIn.read(buffer);

                //convert message to string
                String messageToProcess = MessageConverter.byteToString(buffer, bytesRead);

                //process messages
                switch(messageToProcess) {
                    //based on the state
                    //choose entered command
                    case "SERVERTYPE" :
                        //store the type of server that is on this connection
                        break;

                    case "HEARTBEAT" :
                        buffer = MessageConverter.stringToByte("HEARTBEAT");
                        dataOut.write(buffer);
                        dataOut.flush();
                        break;

                    default:
                        //state not set properly or in bad state. Reset and terminate connection
                        buffer = MessageConverter.stringToByte("MESSAGEUNSUPPORTED");
                        dataOut.write(buffer);
                        dataOut.flush();
                        break;
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    void doClientMessagePump() {
        try {
            while (isConnected) {
                //get the sent data
                buffer = new byte[100];
                bytesRead = dataIn.read(buffer);

                //convert message to string
                String messageToProcess = MessageConverter.byteToString(buffer, bytesRead);

                //process messages
                switch(messageToProcess) {
                    //based on the state
                    //choose entered command
                    case "GETSERVER" :
                        //here, we give the client an ip and port for a server in our list of online servers.
                        break;

                    case "HEARTBEAT" :
                        buffer = MessageConverter.stringToByte("HEARTBEAT");
                        dataOut.write(buffer);
                        dataOut.flush();
                        break;

                    case "DISCONNECT" :

                        break;

                    default:
                        //state not set properly or in bad state. Reset and terminate connection
                        buffer = MessageConverter.stringToByte("MESSAGEUNSUPPORTED");
                        dataOut.write(buffer);
                        dataOut.flush();
                        break;
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
