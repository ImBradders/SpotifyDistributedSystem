import java.util.LinkedList;
import java.util.Random;

/**
 * Thread-safe list for storing the list of songs for quick searching.
 *
 * This is a singleton class to ensure that there is one list which is common for this server.
 *
 * @author Bradley Davis
 */
public class RecentSongs {
    private Random random;
    private static RecentSongs instance = null;
    private LinkedList<String> recentSongs;

    private RecentSongs() {
        random = new Random(System.currentTimeMillis());
        recentSongs = new LinkedList<String>();
    }

    public static RecentSongs getInstance() {
        if (instance == null) {
            instance = new RecentSongs();
        }
        return instance;
    }

    /**
     * Add a recently requested song to the list of recently requested songs.
     *
     * @param song the song to be added.
     */
    public synchronized void addToRecents(String song) {
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
    public String getRecommendation() {
        //gives a number between 1 and 10, biased towards 1
        //therefore our most recently requested/most popular songs get recommended
        int weightedRandom = (int) (10 * Math.pow(random.nextDouble(), 2));
        String songName = "ERROR:No recommendation provided.";
        synchronized (recentSongs) {
            if (recentSongs.size() > 0) {
                if (weightedRandom > recentSongs.size()) {
                    //index is 0 based but the size is 1 based.
                    weightedRandom = recentSongs.size() - 1;
                }
                else if (weightedRandom == recentSongs.size()) {
                    weightedRandom -= 1;
                }
                songName = recentSongs.get(weightedRandom);
            }
        }

        return songName;
    }
}
