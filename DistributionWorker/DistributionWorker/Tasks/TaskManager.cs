using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Reflection;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using TasksBase;

namespace DistributionWorker.Tasks
{
    public static class TaskManager
    {
        public static IDictionary<string, TaskProcesser> LoadedTasks { get; } = new Dictionary<string, TaskProcesser>();

        public static TaskProcesser CurrentTask { get; set; }

        public static void LoadTasks()
        {
            var files = Directory.GetFiles("dll");
            foreach (var file in files)
            {
                var processer = LoadProcesser(file);
                if (processer != null)
                {
                    LoadedTasks.Add
                    (
                        processer.GetId(),
                        processer
                    );
                }
            }
        }

        public static async Task DownloadTask(string id)
        {
            Directory.CreateDirectory("dll");
            using (WebClient client = new WebClient())
            {
                await client.DownloadFileTaskAsync(new Uri(Properties.Settings.Default.ServerURL + "/tasks/" + id + ".dll"),
                                    "dll/" + id + ".dll");

                var processer = LoadProcesser("dll/" + id + ".dll");
                if (processer != null)
                {
                    LoadedTasks.Add
                    (
                        processer.GetId(),
                        processer
                    );
                }
            }
        }

        public static bool IsDownloaded(string taskId)
        {
            return LoadedTasks.ContainsKey(taskId);
        }

        public static TaskProcesser LoadProcesser(string fileName)
        {
            var dll = Assembly.LoadFile(Path.Combine(Directory.GetCurrentDirectory(), fileName));

            foreach (Type type in dll.GetExportedTypes())
            {
                if (type.IsSubclassOf(typeof(TaskProcesser)))
                {
                    var c = Activator.CreateInstance(type, new object[] 
                    {
                        new Action<ClientMessage>(SendClientMessage),
                        new Action(Success)
                    });
                    return c as TaskProcesser;
                }
            }
            return null;
        }

        public static TaskProcesser GetTaskProcesser(string taskId)
        {
            return LoadedTasks[taskId];
        }

        public static void SetCurrentTask(string taskId)
        {
            CurrentTask = GetTaskProcesser(taskId);
            ThreadPool.QueueUserWorkItem(PollClientMessages);

        }

        private static async void PollClientMessages(object state)
        {
            while (CurrentTask != null)
            {
                var response = await HttpSender.GetClientMessages();
                if (response.Data != null)
                {
                    CurrentTask.OnNewMessages(response.Data);
                }
            }
        }

        public static async void SendClientMessage(ClientMessage message)
        {
            var response = await HttpSender.SendClientMessage(message);
            if (response.Data != null)
            {
                CurrentTask?.OnNewMessages(response.Data);
            }
        }

        public static void Success()
        {

        }
    }
}
