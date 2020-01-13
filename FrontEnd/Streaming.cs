using System;
using System.Collections.Generic;
using System.IO;
using System.Media;
using System.Reflection;
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

        public Streaming(Form parent)
        {
            _parent = parent;
            _sharedDataSource = SharedDataSource.GetInstance();
            _sharedDataSource.Updated += InterfaceUpdated;
            _sharedDataSource.SongReady += OnSongReady;
            InitializeComponent();
            btnPause.Enabled = false;
            btnPlay.Enabled = false;
            
        }

        private void Streaming_Load(object sender, EventArgs e)
        {
            
        }

        private void InterfaceUpdated()
        {
            string message = _sharedDataSource.GetUserQueue();

            string[] messages = message.Split(':');

            switch (messages[0])
            {
                case "SONGS":
                    AddListControlPropertyThreadSafe(lstSongs, messages[1]);
                    break;
                case "ERROR":
                    if (messages.Length > 1)
                    {
                        if (messages[1].StartsWith("No server of"))
                        {
                            SetControlPropertyThreadSafe(lblErrors, "Text", "Attempting to get streaming server.");
                            //wait for 4 seconds to ensure that a server has had time to spawn.
                            Thread.Sleep(4000);
                            _sharedDataSource.AddMessage("GETSERVER:STREAMING");
                        }
                        else
                        {
                            SetControlPropertyThreadSafe(lblErrors, "Text", messages[1]);
                        }
                    }
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

        #region ThreadSafety

        private delegate void SetControlPropertyThreadSafeDelegate(
                    Control control, 
                    string propertyName, 
                    object propertyValue);
        
                private static void SetControlPropertyThreadSafe(Control control, string propertyName, object propertyValue)
                {
                    if (control.InvokeRequired)
                    {
                        control.Invoke(new SetControlPropertyThreadSafeDelegate               
                                (SetControlPropertyThreadSafe), 
                            new object[] { control, propertyName, propertyValue });
                    }
                    else
                    {
                        control.GetType().InvokeMember(
                            propertyName, 
                            BindingFlags.SetProperty, 
                            null, 
                            control, 
                            new object[] { propertyValue });
                    }
                }
                
                private delegate void AddListControlPropertyThreadSafeDelegate(
                    ListBox control,
                    object propertyValue);
        
                private static void AddListControlPropertyThreadSafe(ListBox control, object propertyValue)
                {
                    if (control.InvokeRequired)
                    {
                        control.Invoke(new AddListControlPropertyThreadSafeDelegate               
                                (AddListControlPropertyThreadSafe), 
                            new object[] { control, propertyValue });
                    }
                    else
                    {
                        control.Items.Add(propertyValue.ToString());
                    }
                }

        #endregion

        private void OnSongReady()
        {
            while (_soundPlayer != null)
            {}

            MemoryStream memoryStream = _sharedDataSource.RemoveMemoryStream();
            memoryStream.Position = 0;
            _soundPlayer = new SoundPlayer(memoryStream);
            _soundPlayer.PlaySync();
            _soundPlayer.Dispose();
            _soundPlayer = null;
        }
        
        private void btnLoadSongs_Click(object sender, EventArgs e)
        {
            lstSongs.Items.Clear();
            _sharedDataSource.AddMessage("SONGLIST");
        }

        private void Streaming_FormClosed(object sender, FormClosedEventArgs e)
        {
            _sharedDataSource.Updated -= InterfaceUpdated;
            _sharedDataSource.SongReady -= OnSongReady;
            _parent.Visible = true;
        }

        private void lstSongs_VisibleChanged(object sender, EventArgs e)
        {
            
        }

        private void Streaming_FormClosing(object sender, FormClosingEventArgs e)
        {
            
        }

        private void lstSongs_SelectedIndexChanged(object sender, EventArgs e)
        {
            txtSearch.Text = lstSongs.SelectedItem.ToString();
        }
    }
}