import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class LoginConnectionHandler extends ConnectionHandler {

    public LoginConnectionHandler(Socket socket, ServerConnectionDetails communicationServer) {
        super(socket, communicationServer);
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

    private String addUser(String username, String password) {
        List<String> messages = new ArrayList<String>();
        messages.add("ADD:"+username+":"+password);
        messages.add("LOGIN:"+username+":"+password);
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
