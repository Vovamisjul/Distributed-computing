using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace TasksBase
{
    public abstract class TaskProcesser
    {
        protected readonly Action<ClientMessage> sender;
        protected readonly Action success;

        public TaskProcesser(Action<ClientMessage> sender, Action success)
        {
            this.sender = sender;
            this.success = success;
        }

        public abstract void OnNewMessages(IList<ClientMessage> messages);

        public abstract void StartProcessing(string copyId);

        public abstract string GetId();
    }
}
