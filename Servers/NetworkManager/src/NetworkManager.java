import java.io.*;
import java.net.Socket;

/**
 * This is the network manager which tells the load balancing server that this computer is within our network and can create login or streaming servers at will.
 *
 * @author Bradley Davis
 */
public class NetworkManager {

    private int portNumber;

    public NetworkManager(int portNumber) {
        this.portNumber = portNumber;
    }

    /**
     * This starts the network manager running.
     */
    public void start() {
        try {
            ServerConnectionDetails communicationServer = getCommunicationServerDetails();

            Socket communicationServerConnection = new Socket(communicationServer.getIpAddress(), communicationServer.getPortNumber());
            DataOutputStream dataOutputStream = new DataOutputStream(communicationServerConnection.getOutputStream());
            DataInputStream dataInputStream = new DataInputStream(communicationServerConnection.getInputStream());

            byte[] buffer = new byte[100];
            int messageSize = 0;
            String messageReceived = null;

            dataOutputStream.write(MessageConverter.stringToByte("SERVER"));
            dataOutputStream.flush();

            dataOutputStream.write(MessageConverter.stringToByte("SERVERTYPE:NETWORK:"+portNumber));
            dataOutputStream.flush();

            messageSize = dataInputStream.read(buffer);
            messageReceived = MessageConverter.byteToString(buffer, messageSize);

            if (!messageReceived.equals("TYPESTORED")) {
                System.out.println("Communication server was unable to recognise this server - shutting down.");
                return;
            }

            while (true) {
                messageSize = dataInputStream.read(buffer);
                messageReceived = MessageConverter.byteToString(buffer, messageSize);
                String[] messages = messageReceived.split(":");
                switch (messages[0]) {
                    case "SPAWN":
                        switch (messages[1]) {
                            case "LOGIN":
                                System.out.println("Spawning login server.");
                                Runtime.getRuntime().exec("java -jar Multi-UseServer.jar login");
                                break;
                            case "STREAMING":
                                System.out.println("Spawning streaming server.");
                                Runtime.getRuntime().exec("java -jar Multi-UseServer.jar streaming");
                                break;
                            default:
                                dataOutputStream.write(MessageConverter.stringToByte("ERROR:Type does not exist."));
                                break;
                        }
                        break;
                    default:
                        dataOutputStream.write(MessageConverter.stringToByte("MESSAGEUNSUPPORTED"));
                        break;
                }
                dataOutputStream.flush();
            }
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Reads the file containing the details for the communication server.
     *
     * @return the communication server details.
     * @throws IOException if the file cannot be read.
     */
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
                    case "IP" :
                        ipAddress = lineData[1];
                        break;
                    case "PORT" :
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
            throw new IOException("Server or port number was missing/");
        }

        return new ServerConnectionDetails(ipAddress, portNumber);
    }
}
