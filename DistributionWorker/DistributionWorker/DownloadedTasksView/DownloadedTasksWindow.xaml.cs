using DistributionWorker.TasksView;
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

namespace DistributionWorker.DownloadedTasksView
{
    /// <summary>
    /// Логика взаимодействия для DownloadedTasksWindow.xaml
    /// </summary>
    public partial class DownloadedTasksWindow : Window
    {
        public TaskModel Task { get; set; }

        public DownloadedTasksWindow()
        {
            InitializeComponent();
            DataContext = new DownloadedTasksViewModel();
        }

        private async void Window_Loaded(object sender, RoutedEventArgs e)
        {
            var taskCount = 0;
            try
            {
                taskCount = await ((DownloadedTasksViewModel)DataContext).Init();
            }
            catch
            {
                MessageBox.Show("Unable to retrieve tasks", "Error");
            }
            if (taskCount > 0)
            {
                tasksLst.Visibility = Visibility.Visible;
            }
            else
            {
                noTaskLbl.Visibility = Visibility.Visible;
            }
            loadingImg.Visibility = Visibility.Hidden;
        }
    }
}
