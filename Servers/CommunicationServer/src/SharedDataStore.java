import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Allows for shared data storage across all threads.
 *
 * @author Bradley Davis
 */
public class SharedDataStore {
    private static SharedDataStore classInstance = null;
    private final List<ServerConnectionDetails> onlineServers;
    private final Random numberGen;

    /**
     * Constructor which initialises internal class variables.
     */
    private SharedDataStore() {
        onlineServers = new ArrayList<ServerConnectionDetails>();
        numberGen = new Random(System.currentTimeMillis());
    }

    /**
     * Method to allow for this class to follow the Singleton class design.
     * Ensures that only 1 shared data store can exist.
     *
     * @return the instance for the singleton class.
     */
    public static SharedDataStore getInstance()
    {
        if (classInstance == null)
        {
            classInstance = new SharedDataStore();
        }

        return classInstance;
    }

    /**
     * Safely retrieves a server from the list of currently connected servers.
     *
     * @return an IP address to a server.
     */
    public ServerConnectionDetails getServer() {
        ServerConnectionDetails toReturn = null;
        synchronized (onlineServers)
        {
            toReturn = onlineServers.get(numberGen.nextInt(onlineServers.size()));
        }
        return toReturn;
    }

    /**
     * Safely adds a server to the list of currently connected servers.
     *
     * @param serverConnectionDetails a class representing the details of the new server.
     */
    public void addServer(ServerConnectionDetails serverConnectionDetails) {
        synchronized (onlineServers)
        {
            onlineServers.add(serverConnectionDetails);
        }
    }

    /**
     * Safely adds a server to the list of currently connected servers.
     *
     * @param serverIp the IP address of the server to be added to the list.
     * @param portNumber the port number on which the server will listen for connections.
     */
    public void addServer(String serverIp, int portNumber) {
        ServerConnectionDetails serverConnectionDetails = new ServerConnectionDetails(serverIp, portNumber);

        addServer(serverConnectionDetails);
    }
}
