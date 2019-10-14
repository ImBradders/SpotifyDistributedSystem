package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class ServerConnectionHandler.
 * This class is the thread that will run whenever a client connects to the server.
 *
 */

class ServerConnectionHandler implements Runnable{
	Socket soc = null;
	DataInputStream dataIn;
	DataOutputStream dataOut;

	/**
	 * This constructor allows for the socket to be passed in when creating the class so that it can communicate over
     * the network.
     *
	 * @param inSoc - takes the created socket to create input and output streams
	 */
	public ServerConnectionHandler (Socket inSoc) {
		soc = inSoc;
	}

    /**
     * This allows the class to implement runnable so that the class will run as a thread when called to do so.
     */
	public void run() {
		String receivedData;
		int dataLength;
		String sReceivedData;
		boolean isConnected;

		try {
		    //this is dumb but allows me to demonstrate 421
            boolean fileSystemAvailable = true;

			//attempt to get data streams
			dataIn = new DataInputStream(soc.getInputStream());
			dataOut = new DataOutputStream(soc.getOutputStream());

			//connected to server
			isConnected = true;
			dataOut.writeUTF(Converter.stringToASCII("220 smtp.bradleydavis.online Ready"));
			dataOut.flush();

			while(isConnected) {
				//get the sent data
                receivedData = dataIn.readUTF();
				sReceivedData = receivedData.toString();
				dataLength = sReceivedData.length();

				switch() {
				    //based on the state
					//choose entered command
                    case :
						break;

					default:
						//state not set properly or in bad state. Reset and terminate connection
						break;
					}
			}
		}
		catch(IOException ioe) {
		    //if not in quitting state, throw error
		    if(state != states.CQ){
                System.out.println("Error in data streams -> " + ioe.getMessage());
            }
            //otherwise, run to end
		}
		catch(ByteConversionError bce) {
            System.out.println(bce.getMessage());
		}
	}
}