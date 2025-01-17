import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This server type will be used as bulk storage for the system
 *
 * @author Bradley Davis
 */
public class StorageServer {
    private int portNumber;
    private ServerConnectionDetails communicationServerDetails;

    /**
     * Constructor to create the class with all internal variables set up.
     *
     * @param portNumber the port number for this server to listen on.
     */
    public StorageServer(int portNumber) {
        this.portNumber = portNumber;
    }

    public boolean start() {
        boolean isRunning = true;
        try {
            //Set up/check storage
            String fileSeparator = System.getProperty("file.separator");
            //music storage
            File file = new File(System.getProperty("user.dir") + fileSeparator + "MusicBank");
            if (!file.exists()) {
                //we were unable to find the music storage
                if (!file.mkdirs()) {
                    //we were unable to create the music storage.
                    return false;
                }
            }
            String musicStorage = file.toString() + fileSeparator;

            if (!(file.canWrite() && file.canRead())) {
                //we cannot read and write to the specified file location - therefore we are useless.
                return false;
            }

            //login details storage
            file = new File(System.getProperty("user.dir") + fileSeparator + "LoginDetails.txt");
            if (!file.exists()) {
                //if the login file does not exist, create it.
                try {
                    byte[] data = new byte[] {};
                    FileOutputStream outputStream = new FileOutputStream(file.getName());
                    outputStream.write(data);
                    outputStream.close();
                }
                catch (IOException e) {
                    return false;
                }
            }
            String loginStorage = file.toString();

            if (!(file.canWrite() && file.canRead())) {
                //we cannot read and write to the specified file location - therefore we are useless.
                return false;
            }

            LoginDetailsList loginDetailsList = LoginDetailsList.getInstance();
            loginDetailsList.loadFromFile(loginStorage);

            //contact the communication server to tell it that we exist
            boolean communicationServerContacted = contactCommunicationServer();

            if (!communicationServerContacted) {
                return false;
            }

            //create server socket for client communication
            ServerSocket serverSocket = new ServerSocket(portNumber);

            while (isRunning) {
                System.out.println("Awaiting connection requests...");

                Socket socket = serverSocket.accept();

                ConnectionHandler connectionHandler = new ConnectionHandler(socket, musicStorage, loginStorage, communicationServerDetails);
                Thread handler = new Thread(connectionHandler);
                handler.start();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }


    /**
     * Sends the communication to the communication server to tell it that this is a streaming server and that it exists.
     *
     * @return whether or not the communication was successful.
     */
    protected boolean contactCommunicationServer() {
        ConnectionState connectionState = null;
        try {
            communicationServerDetails = getCommunicationServerDetails();
            Socket communicationServerConnection = new Socket(communicationServerDetails.getIpAddress(), communicationServerDetails.getPortNumber());
            DataOutputStream communicationServerOutput = new DataOutputStream(communicationServerConnection.getOutputStream());
            DataInputStream communicationServerInput = new DataInputStream(communicationServerConnection.getInputStream());
            connectionState = ConnectionState.CONNECTED;

            //send necessary commands to communication server to tell it that we exist.
            byte[] buffer = new byte[100];
            int messageSize = 0;
            String messageReceived = null;

            communicationServerOutput.write(MessageConverter.stringToByte("SERVER"));
            communicationServerOutput.flush();

            communicationServerOutput.write(MessageConverter.stringToByte("SERVERTYPE:STORAGE:"+portNumber));
            communicationServerOutput.flush();

            messageSize = communicationServerInput.read(buffer);
            messageReceived = MessageConverter.byteToString(buffer, messageSize);

            if (!messageReceived.equals("TYPESTORED")) {
                System.out.println("Communication server was unable to recognise this server - shutting down.");
                return false;
            }

            communicationServerOutput.write(MessageConverter.stringToByte("DISCONNECT"));
            communicationServerOutput.flush();

            messageSize = communicationServerInput.read(buffer);
            messageReceived = MessageConverter.byteToString(buffer, messageSize);

            if (!messageReceived.equals("DISCONNECT")) {
                System.out.println("Unable to disconnect properly from communication server - shutting down.");
                return false;
            }

            connectionState = ConnectionState.DISCONNECTING;
            communicationServerConnection.close();
        }
        catch (IOException e) {
            if (connectionState != ConnectionState.DISCONNECTING)
            {
                System.out.println("Error in contacting communication server - shutting down.");
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }

    private ServerConnectionDetails getCommunicationServerDetails() throws IOException {
        String ipAddress = null;
        int portNumber = 0;
        try {
            File file = new File("CommunicationServerIP.txt");
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String currentLine = bufferedReader.readLine();

            while (currentLine != null) {
                String[] lineData = currentLine.split(" : ");

                switch (lineData[0]) {
                    case "IP":
                        ipAddress = lineData[1];
                        break;
                    case "PORT":
                        portNumber = Integer.parseInt(lineData[1]);
                        break;
                    default:
                        System.out.println("Part of CommunicationServerIP.txt was unreadable.");
                        break;
                }

                currentLine = bufferedReader.readLine();
            }

            fileReader.close();
        }
        catch (NumberFormatException nfe) {
            System.out.println("Unable to read communication server port number as integer - shutting down.");
            throw new IOException("Error reading communication server port number from file.");
        }
        catch (IOException ioe) {
            System.out.println("Unable to load information for communication server - shutting down.");
            throw ioe;
        }

        if (ipAddress == null || portNumber == 0) {
            System.out.println("Server or port number was missing - shutting down.");
            throw new IOException("Server or port number was missing.");
        }

        return new ServerConnectionDetails(ipAddress, portNumber);
    }
}
