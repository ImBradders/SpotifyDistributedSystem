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
    private final List<ServerConnectionDetails> loginServers;
    private final List<ServerConnectionDetails> streamingServers;
    private final List<ServerConnectionDetails> storageServers;
    private final Random numberGen;

    /**
     * Constructor which initialises internal class variables.
     */
    private SharedDataStore() {
        loginServers = new ArrayList<ServerConnectionDetails>();
        streamingServers = new ArrayList<ServerConnectionDetails>();
        storageServers = new ArrayList<ServerConnectionDetails>();
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
     * Safely retrieves a server from the relevant list of currently known servers.
     *
     * @return an IP address to a server.
     */
    public ServerConnectionDetails getServer(ServerType serverType) {
        ServerConnectionDetails toReturn = null;
        switch (serverType) {
            case LOGIN:
                synchronized (loginServers) {
                    toReturn = loginServers.get(numberGen.nextInt(loginServers.size()));
                }
                break;
            case STORAGE:
                synchronized (storageServers) {
                    toReturn = storageServers.get(numberGen.nextInt(storageServers.size()));
                }
                break;
            case STREAMING:
                synchronized (streamingServers) {
                    toReturn = streamingServers.get(numberGen.nextInt(streamingServers.size()));
                }
                break;
            default:
                //lol
                break;
        }

        return toReturn;
    }

    /**
     * Safely adds a server to the list of currently connected servers.
     *
     * @param serverConnectionDetails a class representing the details of the new server.
     */
    public void addServer(ServerConnectionDetails serverConnectionDetails, ServerType serverType) {
        switch (serverType) {
            case LOGIN:
                synchronized (loginServers) {
                    loginServers.add(serverConnectionDetails);
                }
                break;
            case STORAGE:
                synchronized (storageServers) {
                    storageServers.add(serverConnectionDetails);
                }
                break;
            case STREAMING:
                synchronized (streamingServers) {
                    streamingServers.add(serverConnectionDetails);
                }
                break;
            default:
                //lol
                break;
        }
    }

    /**
     * Safely adds a server to the list of currently connected servers.
     *
     * @param serverIp the IP address of the server to be added to the list.
     * @param portNumber the port number on which the server will listen for connections.
     */
    public void addServer(String serverIp, int portNumber, ServerType serverType) {
        ServerConnectionDetails serverConnectionDetails = new ServerConnectionDetails(serverIp, portNumber);

        addServer(serverConnectionDetails, serverType);
    }
}
