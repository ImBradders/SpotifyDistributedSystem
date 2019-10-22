public class CommunicationServerBootstrap {
    /**
     * The main method so that the program can run.
     *
     * @param args command line arguments that may be passed in
     */
    public static void main (String[] args) {
        final int DEFAULT_PORT = 57313;

        int portNumber;

        if (args.length > 0) {
            try {
                portNumber = Integer.parseInt(args[0]);
            }
            catch (Exception e) {
                System.out.println("Port entered was NaN - resorting to default port.");
                portNumber = DEFAULT_PORT;
            }
        }
        else {
            System.out.println("No port provided - resorting to default port.");
            portNumber = DEFAULT_PORT;
        }

        CommunicationServer server = new CommunicationServer(portNumber);
        server.Start();
    }
}
