import java.util.ArrayList;
import java.util.LinkedList;
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
    private LinkedList<String> recentSongs;

    private SharedSongList() {
        random = new Random(System.currentTimeMillis());
        songList = new ArrayList<String>();
        recentSongs = new LinkedList<String>();
    }

    public static SharedSongList getInstance() {
        if (instance == null) {
            instance = new SharedSongList();
        }
        return instance;
    }

    /**
     * Adds a song to the list - likely because a new file has appeared.
     *
     * @param song the song to be added.
     */
    public synchronized void addSong(String song) {
        songList.add(song);
    }

    /**
     * Adds a bunch of songs to the list - likely because the server just started up.
     *
     * @param songs the songs to be added
     */
    public synchronized void addSong(List<String> songs) {
        songList.addAll(songs);
    }

    /**
     * Removes a song from the list - likely because the file has been deleted.
     *
     * @param index the index at which the song is to be removed from.
     */
    public synchronized void removeSong(int index) {
        songList.remove(index);
        songList.trimToSize();
    }

    /**
     * Gets all the songs so that they can be advertised to the user should the user request it.
     *
     * @return the list of songs.
     */
    public synchronized ArrayList<String> getSongList() {
        return songList;
    }

    /**
     * Searches for a song within the list of songs and returns a single song which matches the search criteria.
     *
     * @param keyword the word to be searched for.
     * @return a song matching the keyword.
     */
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

        String songToReturn = null;
        if (foundSongs.size() > 1) {
            songToReturn = foundSongs.get(random.nextInt(foundSongs.size()));
        }
        else if (foundSongs.size() > 0) {
            songToReturn = foundSongs.get(0);
        }
        
        if (songToReturn != null) {
            addToRecents(songToReturn);
        }

        return songToReturn;
    }

    /**
     * Add a recently requested song to the list of recently requested songs.
     *
     * @param song the song to be added.
     */
    private synchronized void addToRecents(String song) {
        //if the song is in the list, shuffle it to the front
        //this will keep it in the list for longer as it is clearly well listened to.
        if (recentSongs.contains(song)) {
            //move the item to the front
            recentSongs.remove(song);
        }
        //if the list is larger than 10, we remove a song to keep the list at length 10.
        else if (recentSongs.size() > 10) {
            recentSongs.removeLast();
        }
        recentSongs.addFirst(song);
    }

    /**
     * Get a song recommendation to be sent to the user.
     *
     * @return a song name from the most recently requested songs.
     */
    private String getRecommendation() {
        //gives a number between 1 and 10, biased towards 1
        //therefore our most recently requested/most popular songs get recommended
        int weightedRandom = (int) (10 * Math.pow(random.nextDouble(), 2));
        String songName;
        synchronized (recentSongs) {
            songName = recentSongs.get(weightedRandom);
        }
        return songName;
    }
}
