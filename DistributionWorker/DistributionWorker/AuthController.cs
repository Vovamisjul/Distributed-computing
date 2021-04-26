using DistributionWorker.Exceptions;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Text;
using System.Text.Json;
using System.Threading.Tasks;

namespace DistributionWorker
{
    public static class AuthController
    {

        public static void Initialize()
        {
            if (Properties.Settings.Default.AccessToken == "")
            {
                Properties.Settings.Default.Reset();
                Properties.Settings.Default.Save();
                if (Properties.Settings.Default.DeviceID != "")
                {
                    throw new UnauthorizedException();
                }
                else
                {
                    throw new UnregisteredException();
                }
            }
        }

        public static async Task Register(string password)
        {
            var response = await HttpSender.Register(password);
            if (response.StatusCode != HttpStatusCode.Created)
            {
                throw new UnregisteredException();
            }
            var registration = response.Data;
            Properties.Settings.Default.DeviceID = registration.Id;
            Properties.Settings.Default.AccessToken = registration.AccessToken;
            Properties.Settings.Default.RefreshToken = registration.RefreshToken;
            Properties.Settings.Default.Save();
        }

        public static void SaveTokens(string access, string refresh)
        {
            Properties.Settings.Default.AccessToken = access;
            Properties.Settings.Default.RefreshToken = refresh;
            Properties.Settings.Default.Save();
        }
    }
}
