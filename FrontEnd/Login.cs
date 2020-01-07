using System;
using System.Threading;
using System.Windows.Forms;

namespace FrontEnd
{
    public partial class Login : Form
    {
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
                    btnLogin.Enabled = true;
                    lblMessage.Text = messages[1];
                    break;
                
                case "AUTH":
                    _sharedDataSource.Updated -= InterfaceUpdated;
                    //TODO navigate to next form.
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

        private void btnLogin_Click(object sender, EventArgs e)
        {
            btnLogin.Enabled = false;
            if (txtUsername.Text != "" && txtPassword.Text != "")
            {
                _sharedDataSource.AddMessage("LOGIN:" + txtUsername.Text + ":" + txtPassword.Text);
                lblError.Text = "Waiting for confirmation...";
            }
            else
            {
                lblError.Text = "Please ensure that you enter your username and password.";
            }
        }
    }
}