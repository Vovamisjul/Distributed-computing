using DistributionWorker.Tasks;
using DistributionWorker.TasksView;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Input;

namespace DistributionWorker.DownloadedTasksView
{
    class DownloadedTasksViewModel : INotifyPropertyChanged
    {
        public event PropertyChangedEventHandler PropertyChanged;

        public TaskModel Selected { get; set; }

        public ICommand SelectCommand { get; private set; }

        public ObservableCollection<TaskModel> Tasks { get; set; }

        public DownloadedTasksViewModel()
        {
            Tasks = new ObservableCollection<TaskModel>();
            SelectCommand = new DelegateCommand(SelectTask);
        }

        public async Task<int> Init()
        {
            var loadedTasks = TaskManager.LoadedTasks;
            var runningTasks = await HttpSender.GetRunningTasks();
            foreach (var task in runningTasks.Data)
            {
                if (loadedTasks.ContainsKey(task.TaskInfo.Id))
                {
                    Tasks.Add(new TaskModel
                    {
                        Id = task.TaskInfo.Id,
                        CopyId = task.CopyId,
                        Name = task.TaskInfo.Name,
                        Description = task.TaskInfo.Description,
                        Comment = task.Comment,
                    });
                }
            }
            return Tasks.Count;
        }

        public void OnPropertyChanged(string propertyName)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }

        private void SelectTask(object obj)
        {
            var window = obj as DownloadedTasksWindow;
            window.Task = Selected;
            window.Close();
        }
    }
}
