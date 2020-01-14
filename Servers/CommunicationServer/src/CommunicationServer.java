import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * The main class to start the communication server.
 *
 * @author Bradley Davis
 */
public class CommunicationServer {
    private int portNumber;

    /**
     * Constructor to start the communication server.
     *
     * @param portNumber the port for the server to listen for clients on.
     */
    public CommunicationServer(int portNumber) {
        this.portNumber = portNumber;
    }

    /**
     * Starts the server running.
     *
     * Listens for connecting clients to handle.
     */
    public void Start() {
        boolean isRunning = true;
        try{
            ServerSocket serverSoc = new ServerSocket(portNumber);

            while(isRunning) {
                System.out.println("Waiting for connections.");

                Socket socket = serverSoc.accept();

                ConnectionHandler connectionHandler = new ConnectionHandler(socket);
                Thread handleConnection = new Thread(connectionHandler);
                handleConnection.start();
            }
        }
        catch(SocketException se) {
            System.out.println("Exception in socket: " + se.getMessage());
        }
        catch(IOException ioe) {
            System.out.println("IO exception: " + ioe.getMessage());
        }
    }
}
