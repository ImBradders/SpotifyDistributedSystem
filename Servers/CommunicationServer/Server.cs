namespace CommunicationServer
{
    public class Server
    {
        private string _ipAddress;
        private int _listeningPort;

        public Server(string ipAddress, int listeningPort)
        {
            _ipAddress = ipAddress;
            _listeningPort = listeningPort;
        }
        
        public string IPAddress
        {
            get => _ipAddress;
            set => _ipAddress = value;
        }

        public int ListeningPort
        {
            get => _listeningPort;
            set => _listeningPort = value;
        }
    }
}