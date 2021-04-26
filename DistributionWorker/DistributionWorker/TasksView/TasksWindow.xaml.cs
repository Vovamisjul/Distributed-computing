using System;
using System.Collections.Generic;
using System.Linq;
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

namespace DistributionWorker.TasksView
{
    /// <summary>
    /// Логика взаимодействия для TasksWindow.xaml
    /// </summary>
    public partial class TasksWindow : Window
    {
        public TasksWindow()
        {
            InitializeComponent();
            DataContext = new TasksViewModel();
        }

        private async void Window_Loaded(object sender, RoutedEventArgs e)
        {
            try
            {
                await ((TasksViewModel)DataContext).Init();
            }
            catch
            {
                MessageBox.Show("Unable to retrieve tasks", "Error");
            }
            loadingImg.Visibility = Visibility.Hidden;
            tasksLst.Visibility = Visibility.Visible;
        }
    }
}
