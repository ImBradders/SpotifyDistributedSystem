import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
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
    private final LoginDetailsList loginDetailsList;
    private ServerConnectionDetails communicationServer;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private ConnectionState connectionState;
    private RecentSongs recentSongs;

    public ConnectionHandler(Socket socket, String musicStorage, String loginStorage, ServerConnectionDetails communicationServer) {
        this.socket = socket;
        this.communicationServer = communicationServer;
        this.musicStorage = musicStorage;
        this.loginStorage = loginStorage;
        this.recentSongs = RecentSongs.getInstance();
        this.fileSeparator = System.getProperty("file.separator");
        this.loginDetailsList = LoginDetailsList.getInstance();
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
                        String toSend = searchSongs(arguments[1]);
                        if (toSend.startsWith("ERROR")) {
                            buffer = MessageConverter.stringToByte(toSend);
                        }
                        else {
                            buffer = MessageConverter.stringToByte("TITLE:" + toSend);
                        }
                        dataOutputStream.write(buffer);
                        break;

                    case "GET":
                        //send the song file to the streaming server

                        break;

                    case "SONGLIST":
                        List<String> songs = getAllSongs();
                        if (songs.size() == 0) {
                            buffer = MessageConverter.stringToByte("ERROR:No songs");
                            dataOutputStream.write(buffer);
                        }
                        else {
                            for (String song : songs) {
                                buffer = MessageConverter.stringToByte(song);
                                dataOutputStream.write(buffer);
                                dataOutputStream.flush();
                                Thread.sleep(50);
                            }
                            buffer = MessageConverter.stringToByte("EOF:EOF:EOF");
                            dataOutputStream.write(buffer);
                            dataOutputStream.flush();
                        }
                        break;

                    case "ADD":
                        //add an account
                        buffer = MessageConverter.stringToByte(processAddUser(arguments));
                        dataOutputStream.write(buffer);
                        dataOutputStream.flush();
                        loginDetailsList.writeToFile(loginStorage);
                        break;

                    case "LOGIN":
                        //confirm that the user exists
                        buffer = MessageConverter.stringToByte(processLogin(arguments));
                        dataOutputStream.write(buffer);
                        break;

                    case "SONG":
                        //search list of files to see if any of them contain the search term
                        String toPlay = searchSongs(arguments[1]);
                        if (toPlay.startsWith("ERROR")) {
                            buffer = MessageConverter.stringToByte(toPlay);
                            dataOutputStream.write(buffer);
                        }
                        else {
                            recentSongs.addToRecents(toPlay);
                            dataOutputStream.write(MessageConverter.stringToByte("SONG"));
                            dataOutputStream.flush();
                            Thread.sleep(100);
                            int amountRead = 0;
                            byte[] songBuffer = new byte[4096];
                            FileInputStream songIn = new FileInputStream(musicStorage + fileSeparator + toPlay);

                            while ((amountRead = songIn.read(songBuffer, 0, songBuffer.length)) != -1) {
                                dataOutputStream.write(songBuffer, 0, amountRead);
                                dataOutputStream.flush();
                            }

                            Thread.sleep(100);
                            dataOutputStream.write(MessageConverter.stringToByte("EOF:EOF:EOF"));
                            dataOutputStream.flush();
                        }
                        break;

                    case "RECOMMEND" :
                        buffer = MessageConverter.stringToByte(recentSongs.getRecommendation());
                        dataOutputStream.write(buffer);
                        dataOutputStream.flush();
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


    private String processLogin(String[] arguments) {
        String toReturn;
        if (arguments.length < 3) {
            toReturn = "ERROR:Unable to log in";
        }
        else {
            String userName = arguments[1];
            String password = arguments[2];

            if (!loginDetailsList.userExists(userName)) {
                toReturn = "ERROR:Username does not exist";
            }
            else {
                if (loginDetailsList.verifyUser(new LoginDetails(userName, password))) {
                    toReturn = "AUTH";
                }
                else {
                    toReturn = "ERROR:Password incorrect";
                }
            }
        }

        return toReturn;
    }

    /**
     * Checks to ensure that a user does not already exist with the same name and then adds the new user to the system.
     *
     * @param arguments the arguments passed to the server to be processed.
     * @return the message to be returned.
     */
    private String processAddUser(String[] arguments) {
        String toReturn;
        if (arguments.length < 3) {
            toReturn = "ERROR:Unable to create account";
        }
        else {
            String userName = arguments[1];
            String password = arguments[2];

            if (loginDetailsList.userExists(userName)) {
                toReturn = "ERROR:Username already exists";
            }
            else {
                if (loginDetailsList.addUser(userName, password)) {
                    toReturn = "ADDED";
                }
                else {
                    toReturn = "ERROR:Unable to create account";
                }
            }
        }

        return toReturn;
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
                return songFound;
            }
        }

        return "ERROR:Song not in system.";
    }

    /**
     * Retrieves the full list of songs.
     *
     * @return the full list of songs.
     */
    private List<String> getAllSongs() {
        File songLocation = new File(musicStorage);
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
