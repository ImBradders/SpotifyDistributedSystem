import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

/**
 * Base class for handling connections to clients.
 *
 * @author Bradley Davis
 */
public class ConnectionHandler implements Runnable{
    protected Socket socket;
    protected DataInputStream dataInputStream;
    protected DataOutputStream dataOutputStream;
    protected ConnectionState connectionState;

    protected ConnectionHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        //stub to allow for runnable to be implemented but this should always be overridden.
    }
}
