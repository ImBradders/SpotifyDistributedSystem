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
    private final List<ServerConnectionDetails> networkServers;
    private final Random numberGen;

    /**
     * Constructor which initialises internal class variables.
     */
    private SharedDataStore() {
        loginServers = new ArrayList<ServerConnectionDetails>();
        streamingServers = new ArrayList<ServerConnectionDetails>();
        storageServers = new ArrayList<ServerConnectionDetails>();
        networkServers = new ArrayList<ServerConnectionDetails>();
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

    //TODO this is where you will need to do the load balancing.

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
                    if (loginServers.size() > 0) {
                        toReturn = loginServers.get(numberGen.nextInt(loginServers.size()));
                    }
                }
                break;
            case STORAGE:
                synchronized (storageServers) {
                    if (storageServers.size() > 0) {
                        toReturn = storageServers.get(numberGen.nextInt(storageServers.size()));
                    }
                }
                break;
            case STREAMING:
                synchronized (streamingServers) {
                    if (streamingServers.size() > 0) {
                        toReturn = streamingServers.get(numberGen.nextInt(streamingServers.size()));
                    }
                }
                break;
            case NETWORK:
                synchronized (networkServers) {
                    if (networkServers.size() > 0) {
                        toReturn = networkServers.get(numberGen.nextInt(networkServers.size()));
                    }
                }
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
            case NETWORK:
                synchronized (networkServers) {
                    networkServers.add(serverConnectionDetails);
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

    /**
     * Safely removes a server from the list of currently active servers.
     *
     * @param serverConnectionDetails the server to be removed.
     * @param serverType the type of server that is to be removed.
     */
    public void removeServer(ServerConnectionDetails serverConnectionDetails, ServerType serverType) {
        switch (serverType) {
            case LOGIN:
                synchronized (loginServers) {
                    loginServers.remove(serverConnectionDetails);
                }
                break;
            case STORAGE:
                synchronized (storageServers) {
                    storageServers.remove(serverConnectionDetails);
                }
                break;
            case STREAMING:
                synchronized (streamingServers) {
                    streamingServers.remove(serverConnectionDetails);
                }
                break;
            case NETWORK:
                synchronized (networkServers) {
                    networkServers.remove(serverConnectionDetails);
                }
                break;
            default:
                //lol
                break;
        }
    }

    /**
     * Safely removes a server from the list of currently active servers.
     *
     * @param serverIP the IP address of the server to be removed.
     * @param portNumber the port number that the server to be removed is listening on.
     * @param serverType the type of server to be removed.
     */
    public void removeServer(String serverIP, int portNumber, ServerType serverType) {
        ServerConnectionDetails serverConnectionDetails = new ServerConnectionDetails(serverIP, portNumber);

        removeServer(serverConnectionDetails, serverType);
    }
}
