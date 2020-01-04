import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Thread-safe list for storing the list of songs for quick searching.
 *
 * This is a singleton class to ensure that there is one list which is common for this server.
 *
 * @author Bradley Davis
 */
public class SharedSongList {
    private Random random;
    private static SharedSongList instance = null;
    private final ArrayList<String> songList;

    private SharedSongList() {
        random = new Random(System.currentTimeMillis());
        songList = new ArrayList<String>();
    }

    public static SharedSongList getInstance() {
        if (instance == null) {
            instance = new SharedSongList();
        }
        return instance;
    }

    public synchronized void addSong(String song) {
        songList.add(song);
    }

    public synchronized void addSong(List<String> songs) {
        songList.addAll(songs);
    }

    public synchronized void removeSong(int index) {
        songList.remove(index);
        songList.trimToSize();
    }

    public synchronized ArrayList<String> getSongList() {
        return songList;
    }

    public String searchForSong(String keyword) {
        List<String> foundSongs = new ArrayList<String>();
        keyword = keyword.toLowerCase();
        synchronized (songList) {
            for (String song : songList) {
                if (song.toLowerCase().contains(keyword)) { //ignore case in the keyword.
                    foundSongs.add(song);
                }
            }
        }

        String songToReturn;
        if (foundSongs.size() > 1) {
            songToReturn = foundSongs.get(random.nextInt(foundSongs.size()));
        }
        else if (foundSongs.size() > 0) {
            songToReturn = foundSongs.get(0);
        }
        else {
            songToReturn = null;
        }

        return songToReturn;
    }
}
