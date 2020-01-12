import java.util.*;

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
    private final Queue<String> networkMessages;
    private final Random numberGen;

    /**
     * Constructor which initialises internal class variables.
     */
    private SharedDataStore() {
        loginServers = new ArrayList<ServerConnectionDetails>();
        streamingServers = new ArrayList<ServerConnectionDetails>();
        storageServers = new ArrayList<ServerConnectionDetails>();
        networkServers = new ArrayList<ServerConnectionDetails>();
        networkMessages = new LinkedList<String>();
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
                    if (loginServers.size() > 0) {
                        ServerConnectionDetails suitableServer = null;
                        for (ServerConnectionDetails details : loginServers) {
                            if (details.getCurrentClients() < 2) {
                                if (suitableServer == null) {
                                    suitableServer = details;
                                }
                                else if (suitableServer.getCurrentClients() > details.getCurrentClients()) {
                                    suitableServer = details;
                                }
                            }
                        }
                        if (suitableServer == null) {
                            addNetworkMessage("SPAWN:LOGIN");
                        }
                        toReturn = suitableServer;
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
                        ServerConnectionDetails suitableServer = null;
                        for (ServerConnectionDetails details : streamingServers) {
                            if (details.getCurrentClients() < 2) {
                                if (suitableServer == null) {
                                    suitableServer = details;
                                }
                                else if (suitableServer.getCurrentClients() > details.getCurrentClients()) {
                                    suitableServer = details;
                                }
                            }
                        }
                        if (suitableServer == null) {
                            addNetworkMessage("SPAWN:STREAMING");
                        }
                        toReturn = suitableServer;
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

    public void droppedClient(ServerConnectionDetails serverConnectionDetails, ServerType serverType) {
        switch (serverType) {
            case LOGIN:
                synchronized (loginServers) {
                    for (int i = 0; i < loginServers.size(); i++) {
                        ServerConnectionDetails current = loginServers.get(i);
                        if (current.getIpAddress().equals(serverConnectionDetails.getIpAddress()) &&
                                current.getPortNumber() == serverConnectionDetails.getPortNumber()) {
                            current.removeClient();
                            //if we have removed a client and there are no clients left, this server has been shut down.
                            if (current.getCurrentClients() == 0) {
                                loginServers.remove(i);
                                checkNetworkServer(serverConnectionDetails.getIpAddress());
                            }
                            break;
                        }
                    }
                }
                break;
            case STREAMING:
                synchronized (streamingServers) {
                    for (int i = 0; i < streamingServers.size(); i++) {
                        ServerConnectionDetails current = streamingServers.get(i);
                        if (current.getIpAddress().equals(serverConnectionDetails.getIpAddress()) &&
                                current.getPortNumber() == serverConnectionDetails.getPortNumber()) {
                            current.removeClient();
                            //if we have removed a client and there are no clients left, this server has been shut down.
                            if (current.getCurrentClients() == 0) {
                                streamingServers.remove(i);
                                checkNetworkServer(serverConnectionDetails.getIpAddress());
                            }
                            break;
                        }
                    }
                }
                break;
            default:
                //lol
                break;
        }
    }

    public void droppedClient(String serverIP, int portNumber, ServerType serverType) {
        ServerConnectionDetails connectionDetails = new ServerConnectionDetails(serverIP, portNumber);

        droppedClient(connectionDetails, serverType);
    }

    private void checkNetworkServer(String ipAddress) {
        synchronized (networkServers) {
            for (int i = 0; i < networkServers.size(); i++) {
                if (networkServers.get(i).getIpAddress().equals(ipAddress)) {
                    networkServers.get(i).removeClient();
                    break;
                }
            }
        }
    }

    private void addNetworkMessage(String message) {
        synchronized (networkMessages) {
            networkMessages.add(message);
        }
    }

    public String getNetworkMessage() {
        String toReturn = null;
        synchronized (networkMessages) {
            if (networkMessages.size() > 0) {
                toReturn = networkMessages.remove();
            }
        }
        return toReturn;
    }
}
