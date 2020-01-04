import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//Help: https://stackoverflow.com/questions/17044644/sending-audio-stream-over-tcp-unsupportedaudiofileexception

/**
 * Connection handler for streaming server. This will handle the connection for each client connected to the streaming server.
 *
 * @author Bradley Davis
 */
public class StreamingConnectionHandler extends ConnectionHandler {
    private final String cachedStorage;
    private final String fileSeparator;
    private final ServerConnectionDetails communicationServer;
    private ServerConnectionDetails myStorageServer = null;
    private StreamingSongQueue songQueue;
    private int bytesRead = 0;
    private byte[] buffer = new byte[200];

    public StreamingConnectionHandler(Socket socket, String cachedStorageLocation, ServerConnectionDetails communicationServer) {
        super(socket);
        cachedStorage = cachedStorageLocation;
        fileSeparator = System.getProperty("file.separator");
        this.communicationServer = communicationServer;
        songQueue = new StreamingSongQueue();
    }

    @Override
    public void run() {
        try {
            //attempt to get data streams
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());

            //connected to endpoint successfully.
            connectionState = ConnectionState.CONNECTED;

            doMessagePump();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * The message pump to process each message which is sent by the client.
     */
    private void doMessagePump() {
        try {
            while (connectionState != ConnectionState.DISCONNECTING) {
                buffer = new byte[200];
                bytesRead = dataInputStream.read(buffer);

                //convert message to string for easier handling
                String messageToProcess = MessageConverter.byteToString(buffer, bytesRead);

                String[] arguments = messageToProcess.split(" : ");

                switch (arguments[0]) {
                    case "DISCONNECT":
                        connectionState = ConnectionState.DISCONNECTING;
                        dataOutputStream.write(MessageConverter.stringToByte("DISCONNECT"));
                        break;

                    case "SONG":
                        //search list of files to see if any of them contain the search term
                        buffer = MessageConverter.stringToByte(searchSongs(arguments[1]));
                        dataOutputStream.write(buffer);
                        break;

                    default:
                        buffer = MessageConverter.stringToByte("MESSAGEUNSUPPORTED");
                        dataOutputStream.write(buffer);
                        break;
                }
                dataOutputStream.flush();
            }
        } catch (IOException e) {
            if (connectionState != ConnectionState.DISCONNECTING) {
                e.printStackTrace();
            }
        }
    }

    //TODO modify this so that it searches externally first and gets a name back. Once received, check for file locally, if not there, get it.

    /**
     * Method which searches the list of songs.
     *
     * @param songToFind the song which the user is searching for.
     * @return the message to be returned to the client.
     */
    private String searchSongs(String songToFind) {
        Random randomNumberGenerator = new Random(System.currentTimeMillis());
        File songLocation = new File(cachedStorage);
        String[] songs = songLocation.list();
        List<String> songsFound = new ArrayList<String>();

        if (songs != null) {
            for (String song : songs) {
                if (song.contains(songToFind)) {
                    songsFound.add(song);
                }
            }

            if (songsFound.size() > 0) {
                String toAdd = songsFound.get(randomNumberGenerator.nextInt(songsFound.size()));
                songQueue.enqueue(toAdd);
                return "ADDED : " + toAdd;
            } else {
                return "ERROR : Song not in cache.";
            }
        }

        return "ERROR : Song not in system.";
    }

    /**
     * Gets the IP and port number of a storage server to get un-cached music from.
     */
    private void getStorageServer() {
        ConnectionState connectionState = null;
        try {
            //connect to the communication server.
            Socket communicationServerConnection = new Socket(communicationServer.getIpAddress(), communicationServer.getPortNumber());
            DataOutputStream communicationServerOutput = new DataOutputStream(communicationServerConnection.getOutputStream());
            DataInputStream communicationServerInput = new DataInputStream(communicationServerConnection.getInputStream());
            connectionState = ConnectionState.CONNECTED;

            byte[] buffer = new byte[100];
            int messageSize = 0;
            String messageReceived = null;

            //tell it that we are a server
            communicationServerOutput.write(MessageConverter.stringToByte("SERVER"));
            communicationServerOutput.flush();

            //request details for a storage server.
            communicationServerOutput.write(MessageConverter.stringToByte("GETSERVER : STORAGE"));
            communicationServerOutput.flush();

            //handle response.
            messageSize = dataInputStream.read(buffer);
            messageReceived = MessageConverter.byteToString(buffer, messageSize);
            String[] responses = messageReceived.split(" : ");

            if (responses[0].equals("IP")) {
                //we got the message back fine
                String ipAddress = responses[1];
                String port = responses[3];
                int portToUse = parseInt(port);
                if (portToUse > 0) {
                    myStorageServer = new ServerConnectionDetails(ipAddress, portToUse);
                }
                else {
                    System.out.println("Error getting port number of server.");
                    myStorageServer = null;
                }
            }
            else if (responses[0].equals("ERROR")) {
                //something died.
                if (responses[1].equals("No storage server exists.")) {
                    myStorageServer = null;
                }
                else {
                    System.out.println(messageReceived);
                }
            }
            else {
                System.out.println("Some error occurred getting the storage server.");
            }

            //disconnect safely.
            communicationServerOutput.write(MessageConverter.stringToByte("DISCONNECT"));
            communicationServerOutput.flush();

            messageSize = communicationServerInput.read(buffer);
            messageReceived = MessageConverter.byteToString(buffer, messageSize);

            if (!messageReceived.equals("DISCONNECT")) {
                System.out.println("Unable to disconnect properly from communication server.");
            }

            connectionState = ConnectionState.DISCONNECTING;
            communicationServerConnection.close();
        }
        catch (UnknownHostException e) {
            e.printStackTrace();
            System.out.println("Communication server is dead.");
        }
        catch (IOException e) {
            if (connectionState != ConnectionState.DISCONNECTING) {
                System.out.println("Error getting storage server details.");
                e.printStackTrace();
            }
        }

    }

    /**
     * Small function to parse what is expected to be a positive integer.
     *
     * @param number the number to parse.
     * @return the parsed integer or -1 if unable.
     */
    private int parseInt(String number) {
        try {
            return Integer.parseInt(number);
        }
        catch (NumberFormatException nfe) {
            return -1;
        }
    }
}