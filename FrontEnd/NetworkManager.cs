﻿using System;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading;

/*
 * https://stackoverflow.com/questions/21976011/playing-byte-in-c-sharp
 * https://docs.microsoft.com/en-us/dotnet/api/system.io.memorystream.-ctor?view=netframework-4.8#System_IO_MemoryStream__ctor_System_Int32_
 * https://docs.microsoft.com/en-us/dotnet/api/system.io.memorystream.write?view=netframework-4.8#System_IO_MemoryStream_Write_System_Byte___System_Int32_System_Int32_
 */

namespace FrontEnd
{
    /// <summary>
    /// The network manager class will run on a separate thread to the rest of the system. It will send and retrieve messages from the server.
    /// </summary>
    public class NetworkManager
    {
        private Socket _socket;
        private SharedDataSource _sharedDataSource;
        private ServerType _currentServerType;
        public ServerType CurrentServerType
        {
            get => _currentServerType;
        }

        private IPEndPoint _nextServer;

        public IPEndPoint NextServer
        {
            set => _nextServer = value;
        }

        private ServerType _nextServerType;

        public ServerType NextServerType
        {
            set => _nextServerType = value;
        }

        /// <summary>
        /// This will start the network manager which will deal with all network communications.
        /// </summary>
        /// <param name="socket">The socket of the first server to be accessed. All other servers will be accessed by the network manager.</param>
        public NetworkManager(Socket socket)
        {
            _socket = socket;
            _sharedDataSource = SharedDataSource.GetInstance();
            _currentServerType = ServerType.None;
        }

        /// <summary>
        /// Runs the network manager.
        /// </summary>
        public void Run()
        {
            IPAddress ipAddress = null;
            int portNumber = -1;

            try
            {
                string[] lines = System.IO.File.ReadAllLines(@"..\..\CommunicationServerIP.txt");
                foreach (string line in lines)
                {
                    string[] splitLine = line.Split(':');

                    //remove the whitespace from the string.
                    foreach (string segment in splitLine)
                    {
                        Regex.Replace(segment, @"\s+", "");
                    }

                    if (splitLine.Length == 2)
                    {
                        if (splitLine[0] == "IP")
                        {
                            ipAddress = IPAddress.Parse(splitLine[1]);
                        }
                        else if (splitLine[0] == "PORT")
                        {
                            portNumber = int.TryParse(splitLine[1], out portNumber) ? portNumber : -1;
                        }
                    }
                }

                if (portNumber != -1 && ipAddress != null)
                {
                    IPEndPoint communicationServer = new IPEndPoint(ipAddress, portNumber);
                    _socket = new Socket(ipAddress.AddressFamily, SocketType.Stream, ProtocolType.Tcp); 
                    _socket.Connect(communicationServer);
                }
                else
                {
                    return;
                }
            }
            catch (SocketException socketException)
            {
                //unable to connect to communication server;
                return;
            }
            catch (Exception e)
            {
                //unable to load the communication server IP - therefore the client cannot start.
                return;
            }
            
            //if the code reaches here, we have successfully connected to the communication server
            _currentServerType = ServerType.Communication;
            doComServerStart();

            _sharedDataSource.SocketDied = false;

            NetworkReader reader = new NetworkReader(this, _socket, _currentServerType);
            Thread readerThread = new Thread(new ThreadStart(reader.Run));
            readerThread.Start();
            
            NetworkWriter writer = new NetworkWriter(this, _socket, _currentServerType);
            Thread writerThread = new Thread(new ThreadStart(writer.Run));
            writerThread.Start();

            while (readerThread.IsAlive && writerThread.IsAlive)
            {
                //hold while the threads are running
            }
        }

        public void doComServerStart()
        {
            byte[] buffer = new byte[100];
            int bytesToSend = Encoding.UTF8.GetBytes("CLIENT", 0, 6, buffer, 0);
            _socket.Send(buffer, bytesToSend, SocketFlags.None);
        }
    }
}