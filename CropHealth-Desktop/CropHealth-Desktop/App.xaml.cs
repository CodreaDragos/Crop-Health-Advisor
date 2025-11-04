using CropHealth_Desktop.Interface;
using System.Configuration;
using System.Data;
using System.Windows;

namespace CropHealth_Desktop
{
    /// <summary>
    /// Interaction logic for App.xaml
    /// </summary>
    public partial class App : Application
    {
        protected override void OnStartup(StartupEventArgs e)
        {
            base.OnStartup(e);

            // Use full namespace for LoginWindow
            Interface.LoginWindow loginWindow = new Interface.LoginWindow();
            loginWindow.Show();
        }
    }

}
