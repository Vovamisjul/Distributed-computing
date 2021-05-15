using System;
using System.Collections.Generic;
using System.Configuration;
using System.Data;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using System.Windows;

namespace DistributionWorker
{
    /// <summary>
    /// Логика взаимодействия для App.xaml
    /// </summary>
    public partial class App : Application
    {
        static Mutex mutex = new Mutex(true, "{9E2474EA-7F01-49B3-8F6D-B2B3863D68A8}");

        [STAThread]
        public static void Main()
        {
            if (mutex.WaitOne(TimeSpan.Zero, true))
            {
                var application = new App();
                application.InitializeComponent();
                application.Run();
                mutex.ReleaseMutex();
            }
            else
            {
                // send our Win32 message to make the currently running instance
                // jump on top of all the other windows
                NativeMethods.PostMessage(
                    (IntPtr)NativeMethods.HWND_BROADCAST,
                    NativeMethods.WM_SHOWME,
                    IntPtr.Zero,
                    IntPtr.Zero);
            }
        }
    }
}
