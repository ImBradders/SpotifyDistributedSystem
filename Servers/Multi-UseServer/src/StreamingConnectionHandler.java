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
    private StreamingSongQueue songQueue;

    public StreamingConnectionHandler(Socket socket, String cachedStorageLocation, ServerConnectionDetails communicationServer) {
        super(socket, communicationServer);
        cachedStorage = cachedStorageLocation;
        fileSeparator = System.getProperty("file.separator");
        songQueue = new StreamingSongQueue();
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

                String[] arguments = messageToProcess.split(":");

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
        }
        catch (IOException e) {
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
                return "ADDED:" + toAdd;
            } else {
                return "ERROR:Song not in cache.";
            }
        }

        return "ERROR:Song not in system.";
    }
}