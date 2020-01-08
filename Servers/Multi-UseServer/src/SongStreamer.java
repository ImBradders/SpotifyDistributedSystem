import java.io.*;
import java.nio.charset.StandardCharsets;

public class SongStreamer implements Runnable {
    private DataOutputStream dataOutputStream;
    private StreamingSongQueue songs;
    private StreamerStatus streamerStatus;

    public SongStreamer(DataOutputStream outputStream, StreamingSongQueue songs)
    {
        this.dataOutputStream = outputStream;
        this.songs = songs;
        streamerStatus = StreamerStatus.NOTSTARTED;
    }

    @Override
    public void run() {
        streamerStatus = StreamerStatus.RUNNING;
        while (!songs.isEmpty()) {
            String song = songs.dequeue();
            File file = new File(song);
            //create buffer
            byte[] buffer = new byte[2048];
            byte[] songChars = "SONG:".getBytes(StandardCharsets.UTF_8);
            for (int i = 0; i < songChars.length; i++) {
                buffer[i] = songChars[i];
            }

            int amount = 0;

            try {
                FileInputStream songIn = new FileInputStream(file);

                while ((amount = songIn.read(buffer, songChars.length, buffer.length - songChars.length)) != -1) {
                    dataOutputStream.write(buffer);
                    dataOutputStream.flush();
                }

                dataOutputStream.write(MessageConverter.stringToByte("EOF:EOF:EOF"));
                dataOutputStream.flush();
            }
            catch (FileNotFoundException e) {
                streamerStatus = StreamerStatus.DIED;
                e.printStackTrace();
            }
            catch (IOException e) {
                streamerStatus = StreamerStatus.DIED;
                e.printStackTrace();
            }
        }
        streamerStatus = StreamerStatus.COMPLETED;
    }

    public StreamerStatus getStreamerStatus() {
        return streamerStatus;
    }
}
