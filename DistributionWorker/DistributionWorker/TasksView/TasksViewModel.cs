using DistributionWorker.Tasks;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Input;

namespace DistributionWorker.TasksView
{
    class TasksViewModel : INotifyPropertyChanged
    {
        public ObservableCollection<TaskModel> Tasks { get; set; }

        public TaskModel Selected { get; set; }

        public bool CanDownload { get; set; }

        public IAsyncCommand DownloadCommand { get; private set; }

        public ICommand SelectCommand { get; private set; }

        public event PropertyChangedEventHandler PropertyChanged;

        public TasksViewModel()
        {
            DownloadCommand = new AsyncCommand(DownloadDll, onError: e => MessageBox.Show("Unable to download task", "Error"));
            SelectCommand = new DelegateCommand(SelectTask);
            Tasks = new ObservableCollection<TaskModel>();
        }

        public async Task Init()
        {
            var response = await HttpSender.GetTaskInfos();
            var taskInfos = response.Data;
            foreach (var taskInfo in taskInfos)
            {
                bool downloaded = TaskManager.IsDownloaded(taskInfo.Id);
                Tasks.Add(new TaskModel
                {
                    Id = taskInfo.Id,
                    Name = taskInfo.Name + (downloaded ? " (Downloaded)" : ""),
                    Description = taskInfo.Description,
                    Downloaded = downloaded
                });

            }
        }

        private async Task DownloadDll(object obj)
        {
            if (Selected != null && !Selected.Downloaded)
            {
                await TaskManager.DownloadTask(Selected.Id);
                await HttpSender.AddPossibleTask(Selected.Id);
                Selected.Downloaded = true;
                Selected.Name += " (Downloaded)";
                CanDownload = !Selected.Downloaded;
                OnPropertyChanged("CanDownload");
                OnPropertyChanged("Downloaded");
                OnPropertyChanged("Name");

                MessageBox.Show("Successfully downloaded and loaded", "Info");
            }
        }

        private void SelectTask(object obj)
        {
            CanDownload = !Selected.Downloaded;
            OnPropertyChanged("CanDownload");
        }

        protected virtual void OnPropertyChanged(string propertyName)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }
    }
}
