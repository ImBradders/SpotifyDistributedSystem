using System;
using System.Threading;
using System.Windows.Forms;

namespace FrontEnd
{
    public partial class Login : Form
    {
        private string _username;
        private string _password;
        private SharedDataSource _sharedDataSource;

        public Login()
        {
            InitializeComponent();
            _sharedDataSource = SharedDataSource.GetInstance();
            _sharedDataSource.Updated += InterfaceUpdated;
        }
        
        private void Login_Load(object sender, EventArgs e)
        {
            _sharedDataSource.ClientState = ClientState.Startup;
            
            NetworkManager networkManager = new NetworkManager();
            Thread networkManagerThread = new Thread(new ThreadStart(networkManager.Run));
            networkManagerThread.Start();
            
            //this will get put out to the network to get us a login server.
            _sharedDataSource.AddMessage("GETSERVER:LOGIN");
        }
        
        private void InterfaceUpdated()
        {
            string message = _sharedDataSource.GetUserQueue();

            string[] messages = message.Split(':');

            switch (messages[0])
            {
                case "ERROR":

                    break;
                
                case "AUTH":

                    break;
            }
        }

        private void btnCreateAccount_Click(object sender, EventArgs e)
        {
            // remove the event handler subscription
            _sharedDataSource.Updated -= InterfaceUpdated;
            AccountCreation accountCreation = new AccountCreation();
            accountCreation.Show();
            this.Close();
        }
    }
}