using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace FrontEnd
{
    public partial class Form1 : Form
    {
        private IPAddress _ipAddress;
        private IPEndPoint _localEndPoint;
        private Socket _socket;
        private byte[] buffer;
        
        public Form1()
        {
            InitializeComponent();
            buffer = new byte[4194304];
        }
        
        private void Form1_Load(object sender, EventArgs e)
        {
            _ipAddress = IPAddress.Parse("127.0.0.1");
            _localEndPoint = new IPEndPoint(_ipAddress, 57313);
            _socket = new Socket(_ipAddress.AddressFamily, SocketType.Stream, ProtocolType.Tcp);

            try
            {
                _socket.Connect(_localEndPoint);
            }
            catch (Exception exception)
            {
                txtToSend.Text = "Unable to connect to server";
                btnSend.Enabled = false;
            }
        }

        private void btnSend_Click(object sender, EventArgs e)
        {
            if (txtToSend.Text != "")
            {
                int bytesToSend = Encoding.UTF8.GetBytes(txtToSend.Text, 0, txtToSend.Text.Length, buffer, 0);
                //then send the message
                _socket.Send(buffer, bytesToSend, SocketFlags.None);
                ClearBuffer();
                
                int bytesReceived = _socket.Receive(buffer);
                lblReceived.Text = Encoding.UTF8.GetString(buffer, 0, bytesReceived);
            }
        }

        private void ClearBuffer()
        {
            int countZeros = 0;
            for (int i = 0; i < 4194304; i++)
            {
                countZeros = buffer[i] == 0 ? + 1 : 0;
                if (countZeros == 5)
                {
                    break;
                }
                buffer[i] = 0;
            }
        }
    }
}
