import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

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
}
