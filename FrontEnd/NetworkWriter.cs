using System;
using System.Net.Sockets;
using System.Text;

namespace FrontEnd
{
    public class NetworkWriter
    {
        private NetworkManager _parent;
        private Socket _socket;
        private ServerType _serverType;
        private NetworkConnectionState _connectionState;
        private SharedDataSource _sharedDataSource;
        
        public NetworkWriter(NetworkManager parent, Socket socket, ServerType serverType)
        {
            _parent = parent;
            _socket = socket;
            _serverType = serverType;
            _connectionState = NetworkConnectionState.Connected;
            _sharedDataSource = SharedDataSource.GetInstance();
        }
        
        public void Run()
        {
            while (_connectionState == NetworkConnectionState.Connected)
            {
                string messageToSend;
                if (!_sharedDataSource.SocketDied)
                {
                    try
                    {
                        messageToSend = _sharedDataSource.GetMessage();
                        if (messageToSend != null)
                        {
                            string[] parameters = messageToSend.Split(':');
                            switch (parameters[0])
                            {
                                case "DISCONNECT":
                                    _connectionState = NetworkConnectionState.Disconnecting;
                                    break;
                                case "GETSERVER":
                                    ServerType nextServer;
                                    nextServer = Enum.TryParse(parameters[1], true, out nextServer) ? nextServer : ServerType.None;
                                    _parent.NextServerType = nextServer;
                                    break;
                            }
                            byte[] buffer = new byte[messageToSend.Length];
                            int bytesToSend = Encoding.UTF8.GetBytes(messageToSend, 0, messageToSend.Length, buffer, 0);
                            _socket.Send(buffer, bytesToSend, SocketFlags.None);
                        }
                    }
                    catch (SocketException socketException)
                    {
                        if (_connectionState != NetworkConnectionState.Disconnecting)
                        {
                            //the socket died so we need to die too.
                            _sharedDataSource.SocketDied = true;
                            _connectionState = NetworkConnectionState.Disconnecting;
                        }
                    }
                }
                else
                {
                    _connectionState = NetworkConnectionState.Disconnecting;
                }
            }

            while (_serverType == _sharedDataSource.CurrentServerType)
            {
                //hold until we have changed server.
            }
        }
    }
}