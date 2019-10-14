package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.SocketException;

/**
 * This is the server class - it looks for any incoming connections and creates a thread to handle them
 *
 * @author Bradley Davis
 */
public class Server {

    /**
     * The main class so that the program can run.
     *
     * @param args command line arguments that may be passed in
     */
	public static void main (String[] args) {
		Server s = new Server();
	}

    /**
     * This is the constructor for the server. It listens on a defined port and then creates threads when a client
     * attempts to connect.
     */
	public Server(){
        int portNumber = 0;
        String fromUser = "";

        while (portNumber < 1023 || portNumber > 65535) {
            System.out.println("Please enter the port number.");
            try {
                fromUser = GetInput();
                if (fromUser != "") {
                    portNumber = Integer.parseInt(fromUser);
                }
            }
            catch(NumberFormatException nfe) {
                System.out.println("Please ensure that a number is entered.");
            }
        }

        try{
            ServerSocket serverSoc = new ServerSocket(portNumber);

            while(true){
                System.out.println("Waiting for client.");

                Socket soc = serverSoc.accept();

                ServerConnectionHandler sch = new ServerConnectionHandler(soc);
                Thread schThread = new Thread(sch);
                schThread.start();
            }
        }
        catch(SocketException se){
            System.out.println("Exception in socket: " + se.getMessage());
        }
        catch(IOException ioe){
            System.out.println("IO exception: " + ioe.getMessage());
        }
    }

    /**
     * This method gets input from the user and gives them errors if their input cannot be read or is in a bad fromat.
     *
     * @return a string if the user has managed to enter the data correctly
     */
	private static String GetInput() {
		String fromUser = "";
		try {
			BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
			fromUser = stdIn.readLine();
		}
		catch(IOException ioe) {
			System.out.println("IOException: " + ioe.getMessage());
		}
		return fromUser;
	}
}







