using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Cryptography;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using TasksBase;

namespace SHA512Task
{
    public class SHA512TaskProcesser : TaskProcesser
    {
        private const string id = "4b5678b1-1b68-4462-8077-443d23a62464";
        private readonly SHA512 sha512 = SHA512.Create();
        private string copyId;

        private const char START_CHAR = (char)33;
        private const char END_CHAR = (char)126;

        public SHA512TaskProcesser(Action<ClientMessage> sender, Action success) : base(sender, success)
        {

        }

        public override void OnNewMessages(IList<ClientMessage> messages)
        {
            foreach (var message in messages)
            {
                switch (message.Type)
                {
                    case MessageTypes.START:
                        ProcessSHA(message.Data);
                        break;
                    case MessageTypes.STOP:
                        success();
                        break;
                }
            }
        }

        public override void StartProcessing(string copyId)
        {
            this.copyId = copyId;
            sender(new ClientMessage
            {
                Type = MessageTypes.START,
                TaskCopyId = copyId
            });
        }

        public void ProcessSHA(IDictionary<string, string> data)
        {
            var required = data["required"];
            var start = data["start"];
            var end = data["end"];
            var currStr = start;
            bool isEnd = false;
            var results = new List<string>();

            while (!isEnd)
            {
                var bytes = Encoding.UTF8.GetBytes(currStr);
                var hashBytes = sha512.ComputeHash(bytes);
                var hash = BitConverter.ToString(hashBytes).Replace("-", "");
                if (required.Equals(hash, StringComparison.OrdinalIgnoreCase))
                {
                    results.Add(currStr);
                }
                currStr = GetNextString(currStr);
                if (currStr == end)
                {
                    isEnd = true;
                }
            }

            var outData = new Dictionary<string, string>();
            string found = results.Count > 0 ? "true" : "false";
            outData["found"] = found;
            if (results.Count > 0)
            {
                outData["answerCount"] = results.Count.ToString();
                for (int i = 0; i < results.Count; i++)
                {
                    outData["answer" + i] = results[i];
                }
            }

            sender(new ClientMessage
            {
                Type = MessageTypes.ENDED_TASK,
                TaskCopyId = copyId,
                Data = outData
            });

        }

        private string GetNextString(string current)
        {
            return IncreasePos(new StringBuilder(current), current.Length - 1).ToString();
        }

        private StringBuilder IncreasePos(StringBuilder str, int pos)
        {
            if (pos >= 0)
            {
                if (str[pos] >= END_CHAR)
                {
                    str[pos] = START_CHAR;
                    IncreasePos(str, pos - 1);
                    return str;
                }
                else
                {
                    str[pos] = (char)(str[pos] + 1);
                    return str;
                }
            }
            else
            {
                str.Insert(0, START_CHAR);
                return str;
            }
        }

        public override string GetId()
        {
            return id;
        }
    }
}
