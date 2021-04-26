using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using TasksBase;

namespace DistributionWorker.Tasks
{
    public class RunningTaskInfo
    {
        [JsonProperty(PropertyName = "copyId")]
        public string CopyId { get; set; }
        [JsonProperty(PropertyName = "taskInfo")]
        public TaskInfo TaskInfo { get; set; }
    }
}
