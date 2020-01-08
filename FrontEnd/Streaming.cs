using System;
using System.Collections.Generic;
using System.IO;
using System.Media;
using System.Text;
using System.Threading;
using System.Windows.Forms;

/*
 * https://stackoverflow.com/questions/21976011/playing-byte-in-c-sharp
 * https://docs.microsoft.com/en-us/dotnet/api/system.io.memorystream.-ctor?view=netframework-4.8#System_IO_MemoryStream__ctor
 * https://docs.microsoft.com/en-us/dotnet/api/system.io.memorystream.write?view=netframework-4.8#System_IO_MemoryStream_Write_System_Byte___System_Int32_System_Int32_
 */

namespace FrontEnd
{
    public partial class Streaming : Form
    {
        private Form _parent;
        private SharedDataSource _sharedDataSource;
        private SoundPlayer _soundPlayer;
        private Thread _player;
        private LinkedList<MemoryStream> _memoryStreams;
        
        public Streaming(Form parent)
        {
            _parent = parent;
            _memoryStreams = new LinkedList<MemoryStream>();
            _memoryStreams.AddFirst(new MemoryStream());
            _player = new Thread(RunPlayer);
            _sharedDataSource = SharedDataSource.GetInstance();
            InitializeComponent();
            _sharedDataSource.Updated += InterfaceUpdated;
            btnPause.Enabled = false;
            btnPlay.Enabled = false;
        }

        private void Streaming_Load(object sender, EventArgs e)
        {
            _sharedDataSource.AddMessage("SONGLIST");
        }

        private void InterfaceUpdated()
        {
            string message = _sharedDataSource.GetUserQueue();

            string[] messages = message.Split(':');

            switch (messages[0])
            {
                case "SONGS":
                    lstSongs.Items.Add(messages[1]);
                    break;
                case "SONG":
                    byte[] buffer = new byte[2048];
                    int size = Encoding.UTF8.GetBytes(messages[1], 0, messages[1].Length, buffer, 0);
                    _memoryStreams.Last.Value.Write(buffer, 0, size);
                    if (!_player.IsAlive)
                    {
                        _player.Start();
                    }
                    break;
                case "EOF":
                    _memoryStreams.AddLast(new MemoryStream());
                    break;
            }
        }

        private void button1_Click(object sender, EventArgs e)
        {
            txtSearch.Text = "";
        }

        private void btnAdd_Click(object sender, EventArgs e)
        {
            if (txtSearch.Text.Contains(":"))
            {
                lblErrors.Text = "Error: Search must not contain a ':'.";
            }
            else
            {
                _sharedDataSource.AddMessage("SONG:"+txtSearch.Text);
            }

            txtSearch.Text = "";
        }

        private void btnPause_Click(object sender, EventArgs e)
        {
            //pause the song player
            btnPause.Enabled = false;
            btnPlay.Enabled = true;
        }

        private void btnPlay_Click(object sender, EventArgs e)
        {
            //play the song player
            btnPlay.Enabled = false;
            btnPause.Enabled = true;
        }

        private void RunPlayer()
        {
            while (_memoryStreams.Count > 0)
            {
                _soundPlayer = new SoundPlayer(_memoryStreams.First.Value);
                _soundPlayer.PlaySync();
                _memoryStreams.RemoveFirst();
            }
        }
    }
}