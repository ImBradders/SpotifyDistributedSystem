using System;
using System.Reflection;
using System.Threading;
using System.Windows.Forms;

namespace FrontEnd
{
    public partial class AccountCreation : Form
    {
        private Form _parent;
        private SharedDataSource _sharedDataSource;
        public AccountCreation(Form parent)
        {
            _parent = parent;
            _sharedDataSource = SharedDataSource.GetInstance();
            InitializeComponent();
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
                    SetControlPropertyThreadSafe(lblOutput, "Text", "Added successfully");
                    Thread.Sleep(2000);
                    _sharedDataSource.Updated -= InterfaceUpdated;
                    _parent.Visible = true;
                    CloseForm(this);
                    break;
                default:
                    SetControlPropertyThreadSafe(lblOutput, "Text", messages[1]);
                    break;
            }
        }

        private delegate void CloseFormDelegate(Form form);

        private static void CloseForm(Form form)
        {
            if (form.InvokeRequired)
            {
                form.Invoke(new CloseFormDelegate(CloseForm), form);
            }
            else
            {
                form.Close();
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

        private void AccountCreation_Load(object sender, EventArgs e)
        {
            
        }

        private void AccountCreation_FormClosed(object sender, FormClosedEventArgs e)
        {

        }
    }
}