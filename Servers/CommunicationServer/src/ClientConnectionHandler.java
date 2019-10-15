import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Handles the connection for each client currently connected to the server.
 *
 * @author Bradley Davis
 */
public class ClientConnectionHandler implements Runnable {
    Socket socket;
    DataInputStream dataIn;
    DataOutputStream dataOut;

    /**
     * This constructor allows for the socket to be passed in when creating the class so that it can communicate over
     * the network.
     *
     * @param inSoc - takes the created socket to create input and output streams
     */
    public ClientConnectionHandler (Socket inSoc) {
        socket = inSoc;
    }

    /**
     * This allows the class to implement runnable so that the class will run as a thread when called to do so.
     */
    public void run() {
        int messageSize;
        byte[] message;
        boolean isConnected;

        try {
            //attempt to get data streams
            dataIn = new DataInputStream(socket.getInputStream());
            dataOut = new DataOutputStream(socket.getOutputStream());

            //connected to server
            isConnected = true;
            //dataOut.writeUTF();
            dataOut.flush();

            while(isConnected) {
                //get the sent data
                messageSize = dataIn.readInt();
                message = new byte[messageSize];
                dataIn.readFully(message, 0, messageSize);

                //convert message to string
                String messageToProcess = MessageConverter.byteToString(message);

                //process messages

                switch(messageToProcess) {
                    //based on the state
                    //choose entered command
                    case "HEARTBEAT" :
                        byte[] toSend = MessageConverter.stringToByte("HEARTBEAT");
                        dataOut.writeInt(toSend.length);
                        dataOut.write(toSend);
                        dataOut.flush();
                        break;

                    default:
                        //state not set properly or in bad state. Reset and terminate connection
                        break;
                }
            }
        }
        catch(IOException ioe) {
            //if not in quitting state, throw error

            //otherwise, run to end
        }
    }
}
