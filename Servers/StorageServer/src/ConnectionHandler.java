import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

/**
 * Class for handling connections to other servers.
 *
 * @author Bradley Davis
 */
public class ConnectionHandler implements Runnable {
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private ConnectionState connectionState;

    protected ConnectionHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

    }
}
