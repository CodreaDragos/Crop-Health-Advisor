using System.Text;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using CropHealth_Desktop.Services;
using CropHealth_Desktop.Interface;

namespace CropHealth_Desktop
{
    public partial class MainWindow : Window
    {
        private readonly BackendApiService _apiService;

        public MainWindow(BackendApiService apiService)
        {
            InitializeComponent();
            _apiService = apiService;
        }
        
        public MainWindow()
        {
            InitializeComponent();
        }

        private void ManageUsersButton_Click(object sender, RoutedEventArgs e)
        {
            if (_apiService == null)
            {
                MessageBox.Show("Eroare: Serviciul API nu este inițializat.", 
                    "Eroare", MessageBoxButton.OK, MessageBoxImage.Error);
                return;
            }

            var userManagementWindow = new UserManagementWindow(_apiService);
            this.Hide(); // Ascunde dashboard-ul
            userManagementWindow.ShowDialog();
            this.Show(); // Restore dashboard after management window closes
        }

        private void ManageLocationsButton_Click(object sender, RoutedEventArgs e)
        {
            if (_apiService == null)
            {
                MessageBox.Show("Eroare: Serviciul API nu este inițializat.", 
                    "Eroare", MessageBoxButton.OK, MessageBoxImage.Error);
                return;
            }

            var locationManagementWindow = new LocationManagementWindow(_apiService);
            this.Hide(); // Ascunde dashboard-ul
            locationManagementWindow.ShowDialog();
            this.Show(); // Restore dashboard after management window closes
        }

        private void ManageReportsButton_Click(object sender, RoutedEventArgs e)
        {
            if (_apiService == null)
            {
                MessageBox.Show("Eroare: Serviciul API nu este inițializat.", 
                    "Eroare", MessageBoxButton.OK, MessageBoxImage.Error);
                return;
            }

            var reportManagementWindow = new ReportManagementWindow(_apiService);
            this.Hide(); // Ascunde dashboard-ul
            reportManagementWindow.ShowDialog();
            this.Show(); // Restore dashboard after management window closes
        }
    }
}