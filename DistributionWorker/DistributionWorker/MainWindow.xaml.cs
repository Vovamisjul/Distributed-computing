using DistributionWorker.DownloadedTasksView;
using DistributionWorker.Exceptions;
using DistributionWorker.Tasks;
using DistributionWorker.TasksView;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;

namespace DistributionWorker
{
    /// <summary>
    /// Логика взаимодействия для MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
    {
        public MainWindow()
        {
            InitializeComponent();
        }

        private void Window_Initialized(object sender, EventArgs e)
        {
            TaskManager.LoadTasks();
        }

        private void AllTasksItem_Click(object sender, RoutedEventArgs e)
        {
            var taskWindow = new TasksWindow();
            taskWindow.Show();
        }

        private void MainWdw_Loaded(object sender, RoutedEventArgs e)
        {
            try
            {
                AuthController.Initialize();
            }
            catch (UnregisteredException ex)
            {
                var registerWindow = new RegisterWindow();
                registerWindow.ShowDialog();
            }
        }

        private void StartBtn_Click(object sender, RoutedEventArgs e)
        {
            var downloadedTaskWindow = new DownloadedTasksWindow();
            downloadedTaskWindow.ShowDialog();
            if (downloadedTaskWindow.Task != null)
            {
                TaskManager.SetCurrentTask(downloadedTaskWindow.Task.Id);
                TaskManager.CurrentTask.StartProcessing(downloadedTaskWindow.Task.CopyId);
            }
        }
    }
}
