using System;

namespace CommunicationServer
{
    class BootstrapServer
    {
        static int Main(string[] args)
        {
            const int defaultPort = 57313;
            int portToListenOn = defaultPort;

            if (args.Length == 1)
            {
                if (!int.TryParse(args[0], out portToListenOn))
                {
                    portToListenOn = defaultPort;
                }
            }
            
            CommunicationServer communicationServer = new CommunicationServer(portToListenOn);
            return communicationServer.Run();
        }
    }
}
