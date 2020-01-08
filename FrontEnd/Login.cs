using System;
using System.Reflection;
using System.Windows.Forms;

namespace FrontEnd
{
    public partial class Login : Form
    {
        private SharedDataSource _sharedDataSource;

        public Login()
        {
            _sharedDataSource = SharedDataSource.GetInstance();
            InitializeComponent();
            _sharedDataSource.Updated += InterfaceUpdated;
        }
        
        private void Login_Load(object sender, EventArgs e)
        {
            if (_sharedDataSource.CurrentServerType != ServerType.Login)
            {
                _sharedDataSource.ClientState = ClientState.Startup;

                _sharedDataSource.AddMessage("GETSERVER:LOGIN");
            }
        }
        
        private void InterfaceUpdated()
        {
            string message = _sharedDataSource.GetUserQueue();

            string[] messages = message.Split(':');

            switch (messages[0])
            {
                case "ERROR":
                    btnLogin.Enabled = true;
                    SetControlPropertyThreadSafe(lblMessage, "Text", messages[1]);
                    break;
                
                case "AUTH":
                    _sharedDataSource.Updated -= InterfaceUpdated;
                    _sharedDataSource.ClientState = ClientState.LoggedIn;
                    Streaming streaming = new Streaming(this);
                    streaming.Show();
                    this.Visible = false;
                    break;
            }
        }

        private delegate void SetControlPropertyThreadSafeDelegate(
            Control control, 
            string propertyName, 
            object propertyValue);

        public static void SetControlPropertyThreadSafe(Control control, string propertyName, object propertyValue)
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
        
        private void btnCreateAccount_Click(object sender, EventArgs e)
        {
            // remove the event handler subscription
            _sharedDataSource.Updated -= InterfaceUpdated;
            AccountCreation accountCreation = new AccountCreation(this);
            accountCreation.Show();
            this.Visible = false;
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

        private void Login_FormClosed(object sender, FormClosedEventArgs e)
        {

        }
    }
}