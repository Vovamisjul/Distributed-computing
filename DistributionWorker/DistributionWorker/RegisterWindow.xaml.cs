using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Shapes;

namespace DistributionWorker
{
    /// <summary>
    /// Логика взаимодействия для RegisterWindow.xaml
    /// </summary>
    public partial class RegisterWindow : Window
    {
        static readonly HttpClient client = new HttpClient();

        public RegisterWindow()
        {
            InitializeComponent();
        }

        private async void RegisterBtn_Click(object sender, RoutedEventArgs e)
        {
            if (PasswordFld.Password != RepeatPasswordFld.Password)
            {
                MessageBox.Show("Passwords do not match", "Error");
                return;
            }
            if (PasswordFld.Password == "")
            {
                MessageBox.Show("Password could not be empty", "Error");
                return;
            }

            try
            {
                await AuthController.Register(PasswordFld.Password);
                Close();
            }
            catch (Exception ex)
            {
                MessageBox.Show("Unexpeccted error occured: " + ex.Message, "Error");
            }
        }
    }
}
