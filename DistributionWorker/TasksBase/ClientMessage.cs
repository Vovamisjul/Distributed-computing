using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace TasksBase
{
    public class ClientMessage
    {
        [JsonProperty(PropertyName = "type")]
        public string Type { get; set; }
        [JsonProperty(PropertyName = "taskCopyId")]
        public string TaskCopyId { get; set; }
        [JsonProperty(PropertyName = "data")]
        public Dictionary<string, string> Data { get; set; } 
    }
}
