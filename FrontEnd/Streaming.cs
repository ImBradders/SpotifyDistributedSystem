using System;
using System.Media;
using System.Windows.Forms;

/*
 * https://stackoverflow.com/questions/21976011/playing-byte-in-c-sharp
 * https://docs.microsoft.com/en-us/dotnet/api/system.io.memorystream.-ctor?view=netframework-4.8#System_IO_MemoryStream__ctor_System_Int32_
 * https://docs.microsoft.com/en-us/dotnet/api/system.io.memorystream.write?view=netframework-4.8#System_IO_MemoryStream_Write_System_Byte___System_Int32_System_Int32_
 */

namespace FrontEnd
{
    public partial class Streaming : Form
    {
        private SharedDataSource _sharedDataSource;
        private SoundPlayer _soundPlayer;
        public Streaming()
        {
            InitializeComponent();
            _sharedDataSource = SharedDataSource.GetInstance();
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
    }
}