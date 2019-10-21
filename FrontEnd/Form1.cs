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
        
        public Form1()
        {
            InitializeComponent();
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
                byte[] dataToSend = Encoding.UTF8.GetBytes(txtToSend.Text);
                //first send the number of bytes to be received
                _socket.Send(BitConverter.GetBytes(dataToSend.Length));
                //then send the message
                _socket.Send(dataToSend);

                byte[] messageLength = new byte[1];
                _socket.Receive(messageLength);
                int length = messageLength[0];
                byte[] message = new byte[length];
                _socket.Receive(message);
                lblReceived.Text = Encoding.UTF8.GetString(message, 0, message.Length);
            }
        }
    }
}
