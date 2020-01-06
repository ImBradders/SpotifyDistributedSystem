import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Class for handling connections to other servers.
 *
 * @author Bradley Davis
 */
public class ConnectionHandler implements Runnable {
    private Socket socket;
    private final String musicStorage;
    private final String loginStorage;
    private final String fileSeparator;
    private ServerConnectionDetails communicationServer;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private ConnectionState connectionState;

    public ConnectionHandler(Socket socket, String musicStorage, String loginStorage, ServerConnectionDetails communicationServer) {
        this.socket = socket;
        this.communicationServer = communicationServer;
        this.musicStorage = musicStorage;
        this.loginStorage = loginStorage;
        this.fileSeparator = System.getProperty("file.separator");
    }

    @Override
    public void run() {
        try {
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());

            connectionState = ConnectionState.CONNECTED;

            doMessagePump();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doMessagePump() {
        byte[] buffer = new byte[200];
        int bytesRead = 0;

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

                    case "SEARCH":
                        //search list of files to see if any of them contain the search term
                        buffer = MessageConverter.stringToByte(searchSongs(arguments[1]));
                        dataOutputStream.write(buffer);
                        break;

                    case "GET":
                        //send the song file to the streaming server
                        break;

                    case "ADD":
                        //add an account
                        break;

                    case "LOGIN":
                        //confirm that the user exists

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
    }

    /**
     * Method which searches the list of songs.
     *
     * @param songToFind the song which the user is searching for.
     * @return the message to be returned to the streaming server.
     */
    private String searchSongs(String songToFind) {
        Random randomNumberGenerator = new Random(System.currentTimeMillis());
        File songLocation = new File(musicStorage);
        String[] songs = songLocation.list();
        List<String> songsFound = new ArrayList<String>();

        if (songs != null) {
            for (String song : songs) {
                if (song.contains(songToFind)) {
                    songsFound.add(song);
                }
            }

            if (songsFound.size() > 0) {
                String songFound = songsFound.get(randomNumberGenerator.nextInt(songsFound.size()));
                return "TITLE:" + songFound;
            }
        }

        return "ERROR:Song not in system.";
    }
}
