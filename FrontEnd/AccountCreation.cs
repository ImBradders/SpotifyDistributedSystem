using System;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace FrontEnd
{
    public partial class AccountCreation : Form
    {
        private SharedDataSource _sharedDataSource;
        public AccountCreation()
        {
            InitializeComponent();
            _sharedDataSource = SharedDataSource.GetInstance();
            _sharedDataSource.Updated += InterfaceUpdated;
        }

        private void btnCreateAccount_Click(object sender, EventArgs e)
        {
            if (txtPassword.Text == "" || txtPasswordReEnter.Text == "")
            {
                MessageBox.Show("Password must not be empty.");
            }
            else if (txtPassword.Text.Contains(":"))
            {
                MessageBox.Show("Password must not contain colons.");
                txtPassword.Text = "";
                txtPasswordReEnter.Text = "";
            }
            else if (txtPassword.Text == txtPasswordReEnter.Text)
            {
                _sharedDataSource.AddMessage("CREATE:"+txtUsername.Text+":"+txtPassword.Text);
            }
            else
            {
                //there was an error that I did not think of
                MessageBox.Show("Error");
            }
        }

        private void InterfaceUpdated()
        {
            string message = _sharedDataSource.GetUserQueue();
            string[] messages = message.Split(':');

            switch (messages[0])
            {
                case "ADDED":
                    lblOutput.Text = "Added successfully";
                    Thread.Sleep(5000);
                    _sharedDataSource.Updated -= InterfaceUpdated;
                    Login login = new Login();
                    login.Show();
                    this.Close();
                    break;
                default:
                    lblOutput.Text = messages[1];
                    break;
            }
        }
    }
}