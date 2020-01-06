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
            _sharedDataSource.GetUserQueue();
        }
    }
}