using CropHealth_Desktop.Services;
using CropHealth_Desktop.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Shapes;

namespace CropHealth_Desktop.Interface
{
    public partial class LoginWindow : Window
    {
        private readonly BackendApiService _apiService;

        public LoginWindow()
        {
            InitializeComponent();
            _apiService = new BackendApiService();
        }

        private async void LoginButton_Click(object sender, RoutedEventArgs e)
        {
            ErrorTextBlock.Text = "";
            ErrorTextBlock.Visibility = Visibility.Collapsed;
            string email = EmailTextBox.Text;
            string password = PasswordBoxControl.Password;

            if (string.IsNullOrWhiteSpace(email) || string.IsNullOrWhiteSpace(password))
            {
                ErrorTextBlock.Text = "Vă rugăm introduceți email-ul și parola.";
                ErrorTextBlock.Visibility = Visibility.Visible;
                return;
            }

            try
            {
                // Call login API
                string token = await _apiService.LoginAsync(email, password);

                if (!string.IsNullOrEmpty(token))
                {
                    // Get user info to verify role
                    try
                    {
                        var user = await _apiService.GetUserByEmailAsync(email);

                        // Debug logging
                        if (user != null)
                        {
                            System.Diagnostics.Debug.WriteLine($"User retrieved - Email: {user.email}, Role: {user.role}, Role Type: {user.role.GetType()}");
                        }

                        // Verify user has ADMIN role
                        bool isAdmin = user != null && (user.role == Role.ADMIN || user.role.ToString().Equals("ADMIN", StringComparison.OrdinalIgnoreCase));
                        
                        if (!isAdmin)
                        {
                            string roleInfo = user?.role != null ? user.role.ToString() : "null";
                            ErrorTextBlock.Text = $"Acces restricționat. Doar utilizatorii cu rol de ADMIN pot accesa aplicația desktop. (Rol actual: {roleInfo})";
                            ErrorTextBlock.Visibility = Visibility.Visible;
                            _apiService.SetAuthToken(null); // Clear token
                            return;
                        }

                        // User is ADMIN: Open main window
                        MainWindow mainWindow = new MainWindow(_apiService);
                        mainWindow.Show();
                        this.Close(); // Close login window
                    }
                    catch (Exception userEx)
                    {
                        // Error getting user info
                        ErrorTextBlock.Text = $"Eroare la verificarea permisiunilor: {userEx.Message}";
                        ErrorTextBlock.Visibility = Visibility.Visible;
                        _apiService.SetAuthToken(null); // Clear token
                    }
                }
            }
            catch (Exception ex)
            {
                ErrorTextBlock.Visibility = Visibility.Visible;
                if (ex.Message.Contains("401"))
                {
                    ErrorTextBlock.Text = "Credențiale incorecte sau utilizatorul nu există.";
                }
                else
                {
                    ErrorTextBlock.Text = $"Eroare la autentificare. Verificați credențialele. Detalii: {ex.Message}";
                }
            }
        }
    }
}
