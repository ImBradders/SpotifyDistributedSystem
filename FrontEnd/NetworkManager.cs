﻿using System;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading;

namespace FrontEnd
{
    /// <summary>
    /// The network manager class will run on a separate thread to the rest of the system. It will send and retrieve messages from the server.
    /// </summary>
    public class NetworkManager
    {
        private Socket _socket;
        private SharedDataSource _sharedDataSource;
        private ServerType _nextServerType;

        public ServerType NextServerType
        {
            get => _nextServerType;
            set => _nextServerType = value;
        }

        private IPEndPoint _nextServer;

        public IPEndPoint NextServer
        {
            set => _nextServer = value;
        }
        
        public IPEndPoint CommServerDetails { get; set; }

        public bool ReaderAlive { get; set; }
        public bool WriterAlive { get; set; }

        /// <summary>
        /// This will start the network manager which will deal with all network communications.
        /// </summary>
        /// <param name="socket">The socket of the first server to be accessed. All other servers will be accessed by the network manager.</param>
        public NetworkManager()
        {
            _sharedDataSource = SharedDataSource.GetInstance();
            _sharedDataSource.CurrentServerType = ServerType.None;
        }

        /// <summary>
        /// Runs the network manager.
        /// </summary>
        public void Run()
        {
            IPEndPoint serverDetails = LoadCommServerDetails();

            if (serverDetails == null)
            {
                return;
            }
            
            _socket = new Socket(serverDetails.AddressFamily, SocketType.Stream, ProtocolType.Tcp);
            _socket.Connect(serverDetails);
            
            //if the code reaches here, we have successfully connected to the communication server
            _sharedDataSource.CurrentServerType = ServerType.Communication;
            CommServerDetails = serverDetails;
            DoComServerStart();

            _sharedDataSource.SocketDied = false;
            
            while (_sharedDataSource.ClientState != ClientState.Quitting) //while the application is not quitting, keep running
            {
                NetworkReader reader = new NetworkReader(this, _socket, _sharedDataSource.CurrentServerType);
                Thread readerThread = new Thread(new ThreadStart(reader.Run));
                readerThread.Start();

                NetworkWriter writer = new NetworkWriter(this, _socket, _sharedDataSource.CurrentServerType);
                Thread writerThread = new Thread(new ThreadStart(writer.Run));
                writerThread.Start();

                //hold while the threads are running
                while (ReaderAlive && WriterAlive)
                {}

                if (_socket.Connected)
                {
                    _socket.Disconnect(false);
                }

                if (_sharedDataSource.ClientState != ClientState.Quitting)
                {
                    if (_nextServer == null) // we need to go back to the communication server and try again.
                    {
                        serverDetails = LoadCommServerDetails();
                        if (serverDetails == null) //if we cannot load the server details, we may as well give up
                        {
                            break;
                        }
                        _socket = new Socket(serverDetails.AddressFamily, SocketType.Stream, ProtocolType.Tcp);
                        _socket.Connect(serverDetails);
                
                        //if the code reaches here, we have successfully connected to the communication server
                        _sharedDataSource.CurrentServerType = ServerType.Communication;
                        CommServerDetails = serverDetails;
                        DoComServerStart();
    
                        _sharedDataSource.SocketDied = false;
                        
                        RestoreState(_sharedDataSource.ClientState); //restore to the state we were at before we dropped the network connection.
                    }
                    else
                    {
                        _socket = new Socket(_nextServer.AddressFamily, SocketType.Stream, ProtocolType.Tcp);
                        _socket.Connect(_nextServer);
                        _sharedDataSource.CurrentServerType = _nextServerType;
                        _sharedDataSource.SocketDied = false;
                    }
                }
            }
        }

        private IPEndPoint LoadCommServerDetails()
        {
            IPEndPoint communicationServer;
            IPAddress ipAddress = null;
            int portNumber = -1;

            try
            {
                string[] lines = System.IO.File.ReadAllLines(@"..\..\CommunicationServerIP.txt");
                foreach (string line in lines)
                {
                    string[] splitLine = line.Split(':');

                    //remove the whitespace from the string.
                    for (int i = 0; i < splitLine.Length; i++)
                    {
                        splitLine[i] = Regex.Replace(splitLine[i], @"\s+", "");
                    }

                    if (splitLine.Length == 2)
                    {
                        if (splitLine[0].Equals("IP"))
                        {
                            ipAddress = IPAddress.Parse(splitLine[1]);
                        }
                        else if (splitLine[0].Equals("PORT"))
                        {
                            portNumber = int.TryParse(splitLine[1], out portNumber) ? portNumber : -1;
                        }
                    }
                }

                if (portNumber != -1 && ipAddress != null)
                {
                    communicationServer = new IPEndPoint(ipAddress, portNumber);
                }
                else
                {
                    return null;
                }
            }
            catch (SocketException socketException)
            {
                //unable to connect to communication server;
                return null;
            }
            catch (Exception e)
            {
                //unable to load the communication server IP - therefore the client cannot start.
                return null;
            }

            return communicationServer;
        }

        private void DoComServerStart()
        {
            byte[] buffer = new byte[100];
            int bytesToSend = Encoding.UTF8.GetBytes("CLIENT", 0, 6, buffer, 0);
            _socket.Send(buffer, bytesToSend, SocketFlags.None);
        }

        /// <summary>
        /// Gets the network threads back to where they were should they disconnect.
        /// </summary>
        /// <param name="state">The state that the client was previously at.</param>
        private void RestoreState(ClientState state)
        {
            //ensure that we are starting from a clean slate.
            _sharedDataSource.EmptyMessageQueue();
            
            DoComServerStart();
            if (state == ClientState.Startup)
            {
                _sharedDataSource.AddMessage("GETSERVER:LOGIN");
            }
            else if (state == ClientState.LoggedIn)
            {
                _sharedDataSource.AddMessage("GETSERVER:STREAMING");
            }
        }
    }
}