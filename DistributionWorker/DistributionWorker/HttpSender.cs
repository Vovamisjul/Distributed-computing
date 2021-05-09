using DistributionWorker.Exceptions;
using DistributionWorker.Tasks;
using Newtonsoft.Json;
using RestSharp;
using RestSharp.Deserializers;
using System;
using System.Collections.Generic;
using System.Json;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Text;
using System.Text.Json;
using System.Text.Json.Serialization;
using System.Threading.Tasks;
using TasksBase;

namespace DistributionWorker
{
    public class HttpSender
    {
        static readonly IRestClient client;

        static HttpSender()
        {
            client = new RestClient(Properties.Settings.Default.ServerURL);
        }

        public static async Task<IRestResponse<Response>> Register(string password)
        {
            return await SendRequest<Response>("register", Method.POST, new
            {
                password
            });
        }

        public static async Task<IRestResponse<Response>> Login(string id, string password)
        {
            return await SendRequest<Response>("login", Method.POST, new
            {
                id,
                password
            });
        }

        public static async Task<IRestResponse<IList<TaskInfo>>> GetTaskInfos()
        {
            return await SendAuthRequest<IList<TaskInfo>>("tasks/list", Method.GET);
        }

        public static async Task<IRestResponse<object>> AddPossibleTask(string taskId)
        {
            return await SendAuthRequest<object>("settings/addPossibleTask", Method.PUT, new
            {
                taskId
            });
        }

        public static async Task<IRestResponse<IList<RunningTaskInfo>>> GetRunningTasks()
        {
            return await SendAuthRequest<IList<RunningTaskInfo>>("tasks/running", Method.GET);
        }

        public static async Task<IRestResponse<IList<ClientMessage>>> GetClientMessages()
        {
            return await SendAuthRequest<IList<ClientMessage>>("messages", Method.POST);
        }

        public static async Task<IRestResponse<RatingT>> GetRating()
        {
            return await SendAuthRequest<RatingT>("settings/rating", Method.GET);
        }

        public static async Task<IRestResponse<IList<ClientMessage>>> SendClientMessage(ClientMessage message)
        {
            return await SendAuthRequest<IList<ClientMessage>>("messages", Method.POST, new
            {
                type = message.Type,
                taskCopyId = message.TaskCopyId,
                data = message.Data
            });
        }
        

        private static async Task<IRestResponse<T>> SendRequest<T>(string url, Method method, object content = null)
        {
            var request = new RestRequest(url, method, DataFormat.Json);
            request.AddParameter("application/json", JsonConvert.SerializeObject(content), ParameterType.RequestBody);
            return await client.ExecuteAsync<T>(request);
        }

        private static async Task<IRestResponse<T>> SendAuthRequest<T>(string url, Method method, object content = null, bool retry = false)
        {
            var request = new RestRequest(url, method, DataFormat.Json);
            request.AddJsonBody(content, "application/json");
            request.AddHeader("Authorization", "Bearer " + Properties.Settings.Default.AccessToken);
            var response = await client.ExecuteAsync<T>(request);
            if (response.StatusCode == HttpStatusCode.Unauthorized)
            {
                if (retry)
                {
                    throw new UnauthorizedException();
                }
                var tokens = await SendRequest<Response>("refreshtoken", Method.POST, new
                {
                    refreshToken = Properties.Settings.Default.RefreshToken
                });
                if (tokens.StatusCode == HttpStatusCode.Unauthorized)
                {
                    throw new UnauthorizedException();
                }
                AuthController.SaveTokens(tokens.Data.AccessToken, tokens.Data.RefreshToken);
                return await SendAuthRequest<T>(url, method, content, true);
            }
            return response;
        }
    }

    public class Response
    {
        [JsonProperty(PropertyName = "id")]
        public string Id { get; set; }
        [JsonProperty(PropertyName = "accessToken")]
        public string AccessToken { get; set; }
        [JsonProperty(PropertyName = "refreshToken")]
        public string RefreshToken { get; set; }
    }

    public class RatingT
    {
        [JsonProperty(PropertyName = "rating")]
        public double Rating { get; set; }
    }
}
