import java.io.IOException;
import java.net.ServerSocket;

/**
 * Bootstrap class containing main method to launch the program.
 *
 * @author Bradley Davis
 */
public class MultiUseServerBootstrap {
    /**
     * The main method so that the program can run.
     *
     * @param args command line arguments that may be passed in.
     *             First command line argument to be the server type, either "login" or "streaming".
     *             Second command line argument should be the port to listen on.
     */
    public static void main (String[] args) {
        String serverTypeToSpawn;
        int portNumberToUse;

        int len = args.length;
        len = len > 3 ? 3 : len; //cap the length at 3 for the switch statement.

        switch (args.length) {
            case 3:
                System.out.println("More than 2 arguments provided - ignoring extraneous args.");
            case 2: //type and port provided
                serverTypeToSpawn = validateType(args[0]);
                portNumberToUse = validatePort(args[1]);
                break;
            case 1: //only server type provided
                serverTypeToSpawn = validateType(args[0]);
                portNumberToUse = validatePort("0"); //this should get us any available port.
                break;
            default: // nothing provided
                serverTypeToSpawn = "streaming"; //spawn streaming servers by default
                portNumberToUse = validatePort("0"); //this should get us any available port.
                break;
        }

        if (portNumberToUse < 0){
            System.out.println("Unable to get a port - killing program.");
            return;
        }

        if (serverTypeToSpawn.equalsIgnoreCase("streaming")) {
            System.out.println("Spawning streaming server.");
            StreamingServer streamingServer = new StreamingServer(portNumberToUse);
            streamingServer.start();
        }
        else if (serverTypeToSpawn.equalsIgnoreCase("login")) {
            System.out.println("Spawning login server.");
            LoginServer loginServer = new LoginServer(portNumberToUse);
            loginServer.start();
        }
        else {
            System.out.println("I don't know how this code was reached.");
            System.out.println("Killing program.");
            return;
        }
    }

    static String validateType(String serverType) {
        if (!(serverType.equalsIgnoreCase("login") || serverType.equalsIgnoreCase("streaming"))) {
            System.out.println("Incorrect server type provided.");
            System.out.println("Resorting to default server type.");
            return "streaming";
        }
         return serverType.toLowerCase();
    }

    static int validatePort(String portNumberString) {
        int portNumber = 0;
        try {
            portNumber = Integer.parseInt(portNumberString);
        }
        catch (NumberFormatException nfe) {
            //invalid integer.
            System.out.println("Invalid port number provided - could not be converted to integer.");
            return -1;
        }

        if (portNumber < 49152) { //check port too high
            System.out.println("Port must be greater than 49152.");
            System.out.println("Please restart with a new port number.");
            return -1;
        }
        else if (portNumber > 65535) { //check port too low
            System.out.println("Port must be less than 65535.");
            System.out.println("Please restart with a new port number.");
            return -1;
        }

        //if necessary, this can be changed to create on any available port using new ServerSocket(0).
        //the port of this could then be extracted and used instead of needing one to be provided.
        boolean socketOpened = false;
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(portNumber);
            socketOpened = true;
        }
        catch (IOException e) {
            System.out.println("Port is unavailable.");
            System.out.println("Attempting to find another port.");
        }
        finally {
            if (socket != null) {
                try {
                    socket.close();
                }
                catch (IOException e) {
                    System.out.println("Unable to close socket after testing port number.");
                }
            }
        }

        if (socket != null && !socket.isClosed()) { //if the socket was unable to close, we have problems. Kill program.
            return -1;
        }

        if (socketOpened) { //if we managed to open the socket successfully, we can return the port number.
            return portNumber;
        }

        //if we reach here, the port provided was in use. Get any port and use that.
        try {
            socket = new ServerSocket(0);
            socketOpened = true;
            portNumber = socket.getLocalPort();
        }
        catch (IOException e) {
            System.out.println("Port is unavailable.");
            System.out.println("Attempting to find another port.");
        }
        finally {
            if (socket != null) {
                try {
                    socket.close();
                }
                catch (IOException e) {
                    System.out.println("Unable to close socket after testing port number.");
                }
            }
        }

        if (socket != null && !socket.isClosed()) { //if the socket was unable to close, we have problems. Kill program.
            return -1;
        }

        if (socketOpened) { //if we managed to open the socket successfully, we can return the port number.
            return portNumber;
        }

        //if we reach this return statement, we have been unsuccessful in finding a viable port. Kill program.
        return -1;
    }
}
