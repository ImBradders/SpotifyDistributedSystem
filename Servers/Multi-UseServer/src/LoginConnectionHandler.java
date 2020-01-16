import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * The connection handler to run if this is to be a login server.
 *
 * @author Bradley Davis
 */
public class LoginConnectionHandler extends ConnectionHandler {

    public LoginConnectionHandler(Socket socket, ServerConnectionDetails communicationServer, BaseServer parent) {
        super(socket, communicationServer, parent);
    }

    @Override
    public void run() {
        try {
            //attempt to get storage server
            getStorageServer();

            //attempt to get data streams
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());

            //connected to endpoint successfully.
            connectionState = ConnectionState.CONNECTED;

            doMessagePump();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            parent.numConnections--;
            done("LOGIN");
        }
    }

    private void doMessagePump() {
        try {
            while (connectionState != ConnectionState.DISCONNECTING) {
                buffer = new byte[200];
                bytesRead = dataInputStream.read(buffer);

                //convert message to string for easier handling
                String messageToProcess = MessageConverter.byteToString(buffer, bytesRead);

                String[] arguments = messageToProcess.split(":");

                switch (arguments[0]) {
                    case "DISCONNECT":
                        connectionState = ConnectionState.DISCONNECTING;
                        dataOutputStream.write(MessageConverter.stringToByte("DISCONNECT"));
                        break;

                    case "CREATE":
                        if (arguments.length < 3) {
                            dataOutputStream.write(MessageConverter.stringToByte("ERROR:Not enough params"));
                        }
                        else {
                            dataOutputStream.write(MessageConverter.stringToByte(addUser(arguments[1], arguments[2])));
                        }
                        break;

                    case "LOGIN":
                        if (arguments.length < 3) {
                            dataOutputStream.write(MessageConverter.stringToByte("ERROR:Not enough params"));
                        }
                        else {
                            dataOutputStream.write(MessageConverter.stringToByte(login(arguments[1], arguments[2])));
                        }
                        break;

                    default:
                        buffer = MessageConverter.stringToByte("MESSAGEUNSUPPORTED");
                        dataOutputStream.write(buffer);
                        break;
                }
                dataOutputStream.flush();
            }
        }
        catch (IOException e) {
            if (connectionState != ConnectionState.DISCONNECTING) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Checks with the storage server to see if the user exists.
     *
     * @param username the username of the user.
     * @param password the password of the user.
     * @return whether or not the user was logged in successfully.
     */
    private String login(String username, String password) {
        List<String> messages = new ArrayList<String>();
        messages.add("LOGIN:"+username+":"+password);
        messages = messageStorageServer(messages);

        for (String message : messages) {
            if (message.startsWith("ERROR")) {
                //this needs to be reported to the user so this is what we will return. The user can then handle this again.
                return message;
            }
        }

        return "AUTH";
    }

    /**
     * Adds a user to the system, contacting the external storage server to ensure that the new user is stored on disk.
     *
     * @param username the username that the user wishes to use.
     * @param password the password that the user wishes to use.
     * @return whether or not the user was stored.
     */
    private String addUser(String username, String password) {
        List<String> messages = new ArrayList<String>();
        messages.add("ADD:"+username+":"+password);
        messages = messageStorageServer(messages);

        for (String message : messages) {
            if (message.startsWith("ERROR")) {
                //this needs to be reported to the user so this is what we will return. The user can then handle this again.
                return message;
            }
        }

        return "ADDED";
    }
}
