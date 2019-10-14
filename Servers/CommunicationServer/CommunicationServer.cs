using System.Collections.Generic;
using System.Net.Sockets;

namespace CommunicationServer
{
    public class CommunicationServer
    {
        private int _clientListeningPortNumber = 0;
        private List<Server> _serverList;
        

        public CommunicationServer(int clientListeningPortNumber)
        {
            _clientListeningPortNumber = clientListeningPortNumber;
            _serverList = new List<Server>();
        }
        
        public int Run()
        {
            
            
            return 0;
        }

        private void LoadKnownServers()
        {
            string line = "";
            System.IO.StreamReader file = new System.IO.StreamReader("servers.txt");
            while ((line = file.ReadLine()) != null)
            {
                string[] serverDetails = line.Split(" ");
                _serverList.Add(new Server(serverDetails[0], int.Parse(serverDetails[1])));
            }
            file.Close();
        }
    }
}