import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
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
    private SongStreamer songStreamer;

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
        }
        catch (IOException e) {
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
                        String toPlay = searchSongs(arguments[1]);
                        if (toPlay.startsWith("ERROR")) {
                            buffer = MessageConverter.stringToByte(toPlay);
                            dataOutputStream.write(buffer);
                        }
                        else {
                            dataOutputStream.write(MessageConverter.stringToByte("SONG"));
                            dataOutputStream.flush();
                            Thread.sleep(100);
                            int amountRead = 0;
                            byte[] songBuffer = new byte[4096];
                            FileInputStream songIn = new FileInputStream(toPlay);

                            while ((amountRead = songIn.read(songBuffer, 0, songBuffer.length)) != -1) {
                                dataOutputStream.write(songBuffer, 0, amountRead);
                                dataOutputStream.flush();
                            }

                            Thread.sleep(100);
                            dataOutputStream.write(MessageConverter.stringToByte("EOF:EOF:EOF"));
                            dataOutputStream.flush();
                        }

                        break;

                    case "SONGLIST":
                        //send back the full list of available songs.
                        List<String> songs = getAllSongs();
                        if (songs.size() == 0) {
                            buffer = MessageConverter.stringToByte("ERROR:No songs");
                            dataOutputStream.write(buffer);
                        }
                        else {
                            for (String song : songs) {
                                buffer = MessageConverter.stringToByte("SONGS:" + song);
                                dataOutputStream.write(buffer);
                                dataOutputStream.flush();
                                Thread.sleep(10);
                            }
                        }
                        break;

                    default:
                        buffer = MessageConverter.stringToByte("MESSAGEUNSUPPORTED");
                        dataOutputStream.write(buffer);
                        break;
                }
                dataOutputStream.flush();
            }

            socket.close();
        }
        catch (IOException e) {
            if (connectionState != ConnectionState.DISCONNECTING) {
                e.printStackTrace();
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
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
                return cachedStorage + fileSeparator + toAdd;
            }
            else {
                return "ERROR:Song not in cache.";
            }
        }

        return "ERROR:Song not in system.";
    }

    //TODO modify this so that it searches externally first and gets a name back. Once received, check for file locally, if not there, get it.

    /**
     * Retrieves the full list of songs.
     *
     * @return the full list of songs.
     */
    private List<String> getAllSongs() {
        File songLocation = new File(cachedStorage);
        String[] songs = songLocation.list();
        List<String> list;
        if (songs != null) {
            list = Arrays.asList(songs);
        }
        else {
            list = new ArrayList<String>();
        }
        return list;
    }
}