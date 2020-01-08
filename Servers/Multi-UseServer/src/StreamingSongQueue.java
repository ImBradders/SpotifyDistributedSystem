import java.util.LinkedList;
import java.util.Queue;

/**
 * Class to handle the Clients Song Queue in a thread safe way.
 *
 * @author Bradley Davis
 */
public class StreamingSongQueue {
    private Queue<String> songQueue;

    StreamingSongQueue() {
        songQueue = new LinkedList<String>();
    }

    /**
     * Thread safe method of adding a song to the song queue.
     *
     * @param song the song to be added to the queue.
     */
    public synchronized void enqueue(String song) {
        songQueue.add(song);
    }

    /**
     * Thread safe method of removing a song from the queue.
     *
     * @return the song to be played.
     */
    public synchronized String dequeue() {
        if (songQueue.isEmpty()) {
            return null;
        }
        return songQueue.remove();
    }

    public synchronized boolean isEmpty() {
        return songQueue.isEmpty();
    }
}
