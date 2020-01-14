import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
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

    public StreamingConnectionHandler(Socket socket, String cachedStorageLocation, ServerConnectionDetails communicationServer, BaseServer parent) {
        super(socket, communicationServer, parent);
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
        finally {
            parent.numConnections--;
            done("STREAMING");
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
                            songIn.close();

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
                                Thread.sleep(50);
                            }
                        }
                        break;

                    case "RECOMMENDATION":
                        buffer = MessageConverter.stringToByte(getRecommendation());
                        dataOutputStream.write(buffer);
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

    /**
     * Sends a message out to the storage server to get a recommendation
     *
     * @return the recommendation given.
     */
    private String getRecommendation() {
        String recommendation = "ERROR:Unable to get recommendation.";
        ConnectionState storageServerConnectionState = ConnectionState.CONNECTED;
        Socket storageServer = null;
        if (myStorageServer == null) {
            //if we have no storage server, attempt to get it one more time.
            getStorageServer();
            if (myStorageServer == null) {
                recommendation = "ERROR:Storage server inaccessible.";
                return recommendation;
            }
        }

        try {
            storageServer = new Socket(myStorageServer.getIpAddress(), myStorageServer.getPortNumber());
            DataOutputStream storageServerOut = new DataOutputStream(storageServer.getOutputStream());
            DataInputStream storageServerIn = new DataInputStream(storageServer.getInputStream());

            byte[] buffer = new byte[100];
            int bytesRead = 0;

            dataOutputStream.write(MessageConverter.stringToByte("RECOMMENDATION"));

            bytesRead = dataInputStream.read(buffer);
            recommendation = MessageConverter.byteToString(buffer, bytesRead);

            //safely disconnect
            storageServerOut.write(MessageConverter.stringToByte("DISCONNECT"));
            storageServerOut.flush();
            storageServerConnectionState = ConnectionState.DISCONNECTING; //this is set here as the storage server may close the socket before we process its reply.
            bytesRead = storageServerIn.read(buffer);
            String message = MessageConverter.byteToString(buffer, bytesRead);
            if (message.equalsIgnoreCase("DISCONNECT")) {
                storageServer.close();
            }
        }
        catch (UnknownHostException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            if (storageServerConnectionState != ConnectionState.DISCONNECTING) {
                e.printStackTrace();
            }
        }
        finally {
            try {
                if (storageServer != null && storageServer.isClosed()) {
                    storageServer.close();
                }
            }
            catch (IOException e) {
                //we couldnt close the socket but we can ignore this.
                e.printStackTrace();
            }
        }

        return recommendation;
    }

    private List<String> getAllSongsStorage() {
        List<String> replies = new ArrayList<String>();
        ConnectionState storageServerConnectionState = ConnectionState.CONNECTED;
        Socket storageServer = null;
        if (myStorageServer == null) {
            //if we have no storage server, attempt to get it one more time.
            getStorageServer();
            if (myStorageServer == null) {
                replies.add("ERROR:Storage server inaccessible.");
                return replies;
            }
        }

        try {
            storageServer = new Socket(myStorageServer.getIpAddress(), myStorageServer.getPortNumber());
            DataOutputStream storageServerOut = new DataOutputStream(storageServer.getOutputStream());
            DataInputStream storageServerIn = new DataInputStream(storageServer.getInputStream());

            storageServerOut.write(MessageConverter.stringToByte("SONGLIST"));

            boolean receiving = true;
            String temp;
            byte[] buffer = new byte[200];
            int bytesRead = 0;

            bytesRead = storageServerIn.read(buffer);
            temp = MessageConverter.byteToString(buffer, bytesRead);
            if (temp.contains("ERROR:") || temp.contains("MESSAGEUNSUPPORTED")) {
                receiving = false;
            }
            else if (temp.contains("EOF:EOF:EOF")) {
                //were done
                receiving = false;
            }
            else {
                replies.add(temp);
            }

            while (receiving) {
                bytesRead = storageServerIn.read(buffer);
                temp = MessageConverter.byteToString(buffer, bytesRead);
                if (temp.contains("EOF:EOF:EOF")) {
                    //were done
                    receiving = false;
                }
                else {
                    replies.add(temp);
                }
            }

            //safely disconnect
            storageServerOut.write(MessageConverter.stringToByte("DISCONNECT"));
            storageServerOut.flush();
            storageServerConnectionState = ConnectionState.DISCONNECTING; //this is set here as the storage server may close the socket before we process its reply.
            bytesRead = storageServerIn.read(buffer);
            String message = MessageConverter.byteToString(buffer, bytesRead);
            if (message.equalsIgnoreCase("DISCONNECT")) {
                storageServer.close();
            }
        }
        catch (UnknownHostException e) {
            replies.add("ERROR:Storage server inaccessible.");
        }
        catch (IOException e) {
            if (storageServerConnectionState != ConnectionState.DISCONNECTING) {
                //we died before our time
                replies.add("ERROR:Storage server inaccessible.");
            }
        }
        finally {
            try {
                if (storageServer != null && storageServer.isClosed()) {
                    storageServer.close();
                }
            }
            catch (IOException e) {
                //we couldnt close the socket but we can ignore this.
                e.printStackTrace();
            }
        }

        return replies;
    }

    private boolean getSongExternal(String toAdd) {
        ConnectionState storageServerConnectionState = ConnectionState.CONNECTED;
        Socket storageServer = null;
        if (myStorageServer == null) {
            //if we have no storage server, attempt to get it one more time.
            getStorageServer();
            if (myStorageServer == null) {
                return false;
            }
        }

        FileOutputStream outputStream;
        try {
            File file = new File(cachedStorage + fileSeparator + toAdd);
            try {
                byte[] data = new byte[] {};
                outputStream = new FileOutputStream(file);
                outputStream.write(data);
            }
            catch (IOException e) {
                return false;
            }

            storageServer = new Socket(myStorageServer.getIpAddress(), myStorageServer.getPortNumber());
            DataOutputStream storageServerOut = new DataOutputStream(storageServer.getOutputStream());
            DataInputStream storageServerIn = new DataInputStream(storageServer.getInputStream());

            storageServerOut.write(MessageConverter.stringToByte("SONG:" + toAdd));

            boolean receiving = true;
            String temp;
            byte[] buffer = new byte[4069];
            int bytesRead = 0;

            bytesRead = storageServerIn.read(buffer);
            temp = MessageConverter.byteToString(buffer, bytesRead);
            if (temp.contains("ERROR:") || temp.contains("MESSAGEUNSUPPORTED")) {
                receiving = false;
            }
            else if (temp.contains("EOF:EOF:EOF")) {
                //were done
                receiving = false;
            }

            while (receiving) {
                bytesRead = storageServerIn.read(buffer);
                temp = MessageConverter.byteToString(buffer, bytesRead);
                if (temp.contains("EOF:EOF:EOF")) {
                    //were done
                    receiving = false;
                    outputStream.write(buffer, 0, bytesRead - "EOF:EOF:EOF".length());
                }
                else {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            outputStream.close();

            //safely disconnect
            storageServerOut.write(MessageConverter.stringToByte("DISCONNECT"));
            storageServerOut.flush();
            storageServerConnectionState = ConnectionState.DISCONNECTING; //this is set here as the storage server may close the socket before we process its reply.
            bytesRead = storageServerIn.read(buffer);
            String message = MessageConverter.byteToString(buffer, bytesRead);
            if (message.equalsIgnoreCase("DISCONNECT")) {
                storageServer.close();
            }
        }
        catch (UnknownHostException e) {
            return false;
        }
        catch (IOException e) {
            if (storageServerConnectionState != ConnectionState.DISCONNECTING) {
                //we died before our time
                return false;
            }
        }
        finally {
            try {
                if (storageServer != null && storageServer.isClosed()) {
                    storageServer.close();
                }
            }
            catch (IOException e) {
                //we couldnt close the socket but we can ignore this.
                e.printStackTrace();
            }
        }

        return true;
    }

    /**
     * Method which searches the list of songs.
     *
     * @param songToFind the song which the user is searching for.
     * @return the message to be returned to the client.
     */
    private String searchSongs(String songToFind) {
        Random randomNumberGenerator = new Random(System.currentTimeMillis());
        List<String> songs = getAllSongs();
        List<String> songsFound = new ArrayList<String>();

        if (songs.size() > 0) {
            for (String song : songs) {
                if (song.contains(songToFind)) {
                    songsFound.add(song);
                }
            }

            if (songsFound.size() > 0) {
                String toAdd = songsFound.get(randomNumberGenerator.nextInt(songsFound.size()));
                String songPath = cachedStorage + fileSeparator + toAdd;
                File song = new File(songPath);
                if (song.exists()) {
                    return songPath;
                }
                else { //the song is not in our cache, get it from the storage server.
                    if (getSongExternal(toAdd)) {
                        return songPath;
                    }
                    else {
                        return "ERROR:Unable to retrieve song from storage server.";
                    }
                }
            }
            else {
                return "ERROR:Song not in system.";
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
        File songLocation = new File(cachedStorage);
        String[] songs = songLocation.list();
        List<String> list = getAllSongsStorage();
        //if we got nothing from the storage server, just output the local songs.
        if (list.size() == 0){
            if (songs != null) {
                list = Arrays.asList(songs);
            }
            else {
                list = new ArrayList<String>();
            }
        }
        return list;
    }
}