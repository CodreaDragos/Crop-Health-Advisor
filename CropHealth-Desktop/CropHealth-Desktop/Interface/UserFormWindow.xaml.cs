using CropHealth_Desktop.Models;
using CropHealth_Desktop.Services;
using System;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;

namespace CropHealth_Desktop.Interface
{
    public partial class UserFormWindow : Window
    {
        private readonly BackendApiService _apiService;
        private readonly User _editUser;
        private readonly bool _isEditMode;

        public UserFormWindow(BackendApiService apiService, User userToEdit = null)
        {
            InitializeComponent();
            _apiService = apiService;
            _editUser = userToEdit;
            _isEditMode = userToEdit != null;

            if (_isEditMode)
            {
                TitleTextBlock.Text = "Editează Utilizator";
                Title = "Editează Utilizator";
                
                // Pre-populate fields
                UsernameTextBox.Text = _editUser.Username;
                EmailTextBox.Text = _editUser.email;
                PasswordBox.Password = ""; // Don't display existing password
                
                // Select role
                foreach (ComboBoxItem item in RoleComboBox.Items)
                {
                    if (item.Tag?.ToString() == _editUser.role.ToString())
                    {
                        RoleComboBox.SelectedItem = item;
                        break;
                    }
                }
            }
            else
            {
                // Set default role
                RoleComboBox.SelectedIndex = 0; // USER
            }
        }

        private async void SaveButton_Click(object sender, RoutedEventArgs e)
        {
            ErrorTextBlock.Visibility = Visibility.Collapsed;
            ErrorTextBlock.Text = "";

            // Validare
            if (string.IsNullOrWhiteSpace(UsernameTextBox.Text))
            {
                ShowError("Username-ul este obligatoriu!");
                return;
            }

            if (string.IsNullOrWhiteSpace(EmailTextBox.Text))
            {
                ShowError("Email-ul este obligatoriu!");
                return;
            }

            if (!_isEditMode && string.IsNullOrWhiteSpace(PasswordBox.Password))
            {
                ShowError("Parola este obligatorie pentru utilizatori noi!");
                return;
            }

            if (PasswordBox.Password.Length < 8 && !_isEditMode)
            {
                ShowError("Parola trebuie să aibă minim 8 caractere!");
                return;
            }

            // Check if role was selected
            if (RoleComboBox.SelectedItem is ComboBoxItem selectedItem)
            {
                var roleString = selectedItem.Tag?.ToString();
                if (string.IsNullOrEmpty(roleString))
                {
                    ShowError("Te rugăm selectează un rol!");
                    return;
                }

                try
                {
                    User user;
                    
                    if (_isEditMode)
                    {
                        // Update existing user
                        user = new User
                        {
                            Id = _editUser.Id,
                            Username = UsernameTextBox.Text.Trim(),
                            email = EmailTextBox.Text.Trim(),
                            role = roleString == "ADMIN" ? Role.ADMIN : Role.USER
                        };
                        
                        // If new password entered, update it
                        if (!string.IsNullOrWhiteSpace(PasswordBox.Password))
                        {
                            user.password = PasswordBox.Password;
                        }
                        else
                        {
                            // Keep old password (don't send in update)
                            user.password = _editUser.password;
                        }

                        await _apiService.UpdateUserAsync(user.Id, user);
                        MessageBox.Show("Utilizatorul a fost actualizat cu succes!", 
                            "Succes", MessageBoxButton.OK, MessageBoxImage.Information);
                    }
                    else
                    {
                        // Create new user
                        user = new User
                        {
                            Id = 0, // ID is 0 but won't be sent in JSON due to DefaultValueHandling.Ignore
                            Username = UsernameTextBox.Text.Trim(),
                            email = EmailTextBox.Text.Trim(),
                            password = PasswordBox.Password,
                            role = roleString == "ADMIN" ? Role.ADMIN : Role.USER
                        };

                        await _apiService.CreateUserAsync(user);
                        MessageBox.Show("Utilizatorul a fost creat cu succes!", 
                            "Succes", MessageBoxButton.OK, MessageBoxImage.Information);
                    }

                    DialogResult = true;
                    Close();
                }
                catch (Exception ex)
                {
                    ShowError($"Eroare la salvare: {ex.Message}");
                }
            }
        }

        private void CancelButton_Click(object sender, RoutedEventArgs e)
        {
            DialogResult = false;
            Close();
        }

        private void ShowError(string message)
        {
            ErrorTextBlock.Text = message;
            ErrorTextBlock.Visibility = Visibility.Visible;
        }
    }
}

