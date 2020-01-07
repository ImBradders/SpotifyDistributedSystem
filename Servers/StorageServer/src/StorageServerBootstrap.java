import java.io.IOException;
import java.net.ServerSocket;

/**
 * Bootstrap class containing the main method to launch the server.
 *
 * @author Bradley Davis
 */
public class StorageServerBootstrap {
    /**
     * The main method so that the program can run.
     *
     * @param args command line arguments that may be passed in.
     *             First is the port number to listen on
     */
    public static void main(String[] args) {
        int portNumberToUse;

        int len = args.length;
        len = len > 2 ? 2 : len;

        switch (len) {
            case 2:
                System.out.println("More than 1 argument provided - ignoring extraneous args.");
            case 1:
                portNumberToUse = validatePort(args[0]);
                break;
            default:
                portNumberToUse = validatePort("0");
                break;
        }

        if (portNumberToUse < 0){
            System.out.println("Unable to get a port - killing program.");
            return;
        }

        StorageServer storageServer = new StorageServer(portNumberToUse);
        storageServer.start();
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

        if (portNumber != 0) {
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
