import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class which handles updating the shared song list.
 *
 * @author Bradley Davis
 */
public class SongListManager implements Runnable {
    private SharedSongList songList;
    private String musicPath;

    public SongListManager(String musicPath) {
        songList = SharedSongList.getInstance();
        this.musicPath = musicPath;
    }

    @Override
    public void run() {
        File musicFolder = new File(musicPath);

        boolean isRunning = true;

        while (isRunning) {
            if (musicFolder.exists()) {
                ArrayList<String> oldSongs = songList.getSongList();
                if (musicFolder.list() != null) {
                    List<String> songsInDirectory = Arrays.asList(musicFolder.list());

                    if (oldSongs.size() == 0) {
                        //song list is empty, simply add all in the directory
                        songList.addSong(songsInDirectory);
                    }
                    else {
                        for (int i = 0; i < oldSongs.size(); i++) {
                            if (!songsInDirectory.contains(oldSongs.get(i))) {
                                //if the list of song files does not contain an item within the old list of songs, remove it from the old list of songs
                                songList.removeSong(i);
                                oldSongs.remove(i);
                            }
                        }
                        oldSongs.trimToSize();

                        for (String song : songsInDirectory) {
                            if (!oldSongs.contains(song)) {
                                //if the old song list does not contain an item which is in the file system, add it
                                oldSongs.add(song);
                                songList.addSong(song);
                            }
                        }
                    }
                }
                else {
                    //our folder is empty, we have no songs - kill the list.
                    for (int i = 0; i < oldSongs.size(); i++) {
                        songList.removeSong(i);
                    }
                }
            }

            try {
                //attempt to sleep for 10 mins. Update the list every 10 mins
                Thread.sleep(600000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
