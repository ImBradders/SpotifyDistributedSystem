namespace FrontEnd
{
    partial class Form1
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.txtToSend = new System.Windows.Forms.TextBox();
            this.lblReceived = new System.Windows.Forms.Label();
            this.btnSend = new System.Windows.Forms.Button();
            this.SuspendLayout();
            // 
            // txtToSend
            // 
            this.txtToSend.Location = new System.Drawing.Point(320, 227);
            this.txtToSend.Name = "txtToSend";
            this.txtToSend.Size = new System.Drawing.Size(287, 23);
            this.txtToSend.TabIndex = 0;
            // 
            // lblReceived
            // 
            this.lblReceived.Location = new System.Drawing.Point(384, 149);
            this.lblReceived.Name = "lblReceived";
            this.lblReceived.Size = new System.Drawing.Size(143, 37);
            this.lblReceived.TabIndex = 1;
            this.lblReceived.Text = "Nothing yet received";
            // 
            // btnSend
            // 
            this.btnSend.Location = new System.Drawing.Point(416, 275);
            this.btnSend.Name = "btnSend";
            this.btnSend.Size = new System.Drawing.Size(97, 36);
            this.btnSend.TabIndex = 2;
            this.btnSend.Text = "Send Message";
            this.btnSend.UseVisualStyleBackColor = true;
            this.btnSend.Click += new System.EventHandler(this.btnSend_Click);
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(7F, 15F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(933, 519);
            this.Controls.Add(this.btnSend);
            this.Controls.Add(this.lblReceived);
            this.Controls.Add(this.txtToSend);
            this.Name = "Form1";
            this.Text = "Form1";
            this.Load += new System.EventHandler(this.Form1_Load);
            this.ResumeLayout(false);
            this.PerformLayout();
        }

        #endregion

        private System.Windows.Forms.Button btnSend;
        private System.Windows.Forms.Label lblReceived;
        private System.Windows.Forms.TextBox txtToSend;
    }
}

