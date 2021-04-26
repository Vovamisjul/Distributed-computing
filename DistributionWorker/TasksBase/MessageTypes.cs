using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace TasksBase
{
    public static class MessageTypes
    {
        public const string START = "START";
        public const string STOP = "STOP";
        public const string ENDED_TASK = "ENDED_TASK";
        public const string RECONNECT = "RECONNECT";
    }
}
