using System;
using System.Net;
using System.Net.Sockets;
using System.Text;

namespace FrontEnd
{
    public class NetworkReader
    {
        private NetworkManager _parent;
        private Socket _socket;
        private ServerType _serverType;
        private NetworkConnectionState _connectionState;
        private SharedDataSource _sharedDataSource;

        public NetworkReader(NetworkManager parent, Socket socket, ServerType serverType)
        {
            _parent = parent;
            _parent.ReaderAlive = true;
            _socket = socket;
            _serverType = serverType;
            _connectionState = NetworkConnectionState.Connected;
            _sharedDataSource = SharedDataSource.GetInstance();
        }

        public void Run()
        {
            int bytesReceived;
            byte[] buffer = new byte[200];
            
            //while we are still connected.
            while (_connectionState == NetworkConnectionState.Connected) 
            {
                if (!_sharedDataSource.SocketDied)
                {
                    try
                    {
                        bytesReceived = _socket.Receive(buffer);
                        string toProcess = Encoding.UTF8.GetString(buffer, 0, bytesReceived);
                        switch (_serverType)
                        {
                            case ServerType.Communication:
                                CommunicationProc(toProcess);
                                break;
                            case ServerType.Login:
                                LoginProc(toProcess);
                                break;
                            case ServerType.Streaming:
                                StreamingProc(toProcess);
                                break;
                            default:
                                //something has gone terribly wrong.
                                _parent.NextServer = null;
                                
                                break;
                        }
                    }
                    catch (SocketException socketException)
                    {
                        if (_connectionState != NetworkConnectionState.Disconnecting)
                        {
                            //If we quit out of the reader with the next server as null, the network manager will reconnect us to the communication server.
                            _parent.NextServer = null;
                        }
                    }
                }
                else
                {
                    _connectionState = NetworkConnectionState.Disconnecting;
                }
            }

            _parent.ReaderAlive = false;
        }

        private void CommunicationProc(string message)
        {
            string[] splitMessage = message.Split(':');
            switch (splitMessage[0])
            {
                case "TYPESTORED":
                    //handle this
                    break;
                case "ERROR":
                    //errors can be handled by the UI
                    _sharedDataSource.AddUserQueue(message);
                    break;
                case "IP":
                    if (splitMessage.Length == 4)
                    {
                        string ipAddress = splitMessage[1];
                        int portNumber;
                        portNumber = int.TryParse(splitMessage[3], out portNumber) ? portNumber : -1;

                        if (portNumber != -1)
                        {
                            IPAddress ipAddr = IPAddress.Parse(ipAddress);
                            _parent.NextServer = new IPEndPoint(ipAddr, portNumber);
                            _sharedDataSource.AddMessageStart("DISCONNECT");
                        }
                    }
                    else
                    {
                        //there are problems - handle this.
                    }
                    break;
                case "DISCONNECT":
                    
                    _connectionState = NetworkConnectionState.Disconnecting;
                    break;
                default:
                    //we probably sent a bad message - we should handle this.
                    break;
            }
        }

        private void LoginProc(string message)
        {
            string[] splitMessage = message.Split(':');
            switch (splitMessage[0])
            {
                case "ADDED":
                case "AUTH":
                case "ERROR":
                    _sharedDataSource.AddUserQueue(message);
                    break;
                case "DISCONNECT":
                    
                    _connectionState = NetworkConnectionState.Disconnecting;
                    break;
                default:
                    //we probably sent a bad message - we should handle this.
                    break;
            }
        }

        private void StreamingProc(string message)
        {
            string[] splitMessage = message.Split(':');
            switch (splitMessage[0])
            {
                case "ADDED":
                case "REMOVED":
                case "ERROR":
                    _sharedDataSource.AddUserQueue(message);
                    break;
                case "DISCONNECT":
                    
                    _connectionState = NetworkConnectionState.Disconnecting;
                    break;
                default:
                    //we probably sent a bad message - we should handle this.
                    break;
            }
        }
    }
}