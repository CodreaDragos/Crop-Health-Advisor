using CropHealth_Desktop.Models;
using CropHealth_Desktop.Services;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;

namespace CropHealth_Desktop.Interface
{
    public partial class UserManagementWindow : Window
    {
        private readonly BackendApiService _apiService;
        private List<User> _allUsers;

        public UserManagementWindow(BackendApiService apiService)
        {
            InitializeComponent();
            _apiService = apiService;
            LoadUsers();
        }

        private async void LoadUsers()
        {
            try
            {
                StatusTextBlock.Text = "Se încarcă utilizatorii...";
                _allUsers = await _apiService.GetAllUsersAsync();
                ApplyFilter();
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Eroare la încărcarea utilizatorilor: {ex.Message}", 
                    "Eroare", MessageBoxButton.OK, MessageBoxImage.Error);
                StatusTextBlock.Text = "Eroare la încărcare";
                _allUsers = new List<User>();
            }
        }

        private void ApplyFilter()
        {
            if (_allUsers == null)
            {
                UsersDataGrid.ItemsSource = null;
                StatusTextBlock.Text = "Gata - 0 utilizator(i) găsiți";
                return;
            }

            var searchText = SearchTextBox?.Text?.ToLower().Trim() ?? "";
            List<User> filteredUsers;

            if (string.IsNullOrWhiteSpace(searchText))
            {
                filteredUsers = _allUsers;
            }
            else
            {
                filteredUsers = _allUsers.Where(u =>
                    (u.Username?.ToLower().Contains(searchText) ?? false) ||
                    (u.email?.ToLower().Contains(searchText) ?? false) ||
                    (u.role.ToString().ToLower().Contains(searchText)) ||
                    (u.Id.ToString().Contains(searchText))
                ).ToList();
            }

            UsersDataGrid.ItemsSource = filteredUsers;
            StatusTextBlock.Text = $"Gata - {filteredUsers.Count} din {_allUsers.Count} utilizator(i)";
        }

        private void SearchTextBox_TextChanged(object sender, TextChangedEventArgs e)
        {
            ApplyFilter();
        }

        private void BackButton_Click(object sender, RoutedEventArgs e)
        {
            Close();
        }

        private async void RefreshButton_Click(object sender, RoutedEventArgs e)
        {
            SearchTextBox.Text = "";
            LoadUsers();
        }

        private void AddUserButton_Click(object sender, RoutedEventArgs e)
        {
            var userForm = new UserFormWindow(_apiService, null);
            if (userForm.ShowDialog() == true)
            {
                LoadUsers(); // Reload list after add
            }
        }

        private void EditUser_Click(object sender, RoutedEventArgs e)
        {
            var button = sender as Button;
            if (button?.Tag is User user)
            {
                var userForm = new UserFormWindow(_apiService, user);
                if (userForm.ShowDialog() == true)
                {
                    LoadUsers(); // Reload list after edit
                }
            }
        }

        private async void DeleteUser_Click(object sender, RoutedEventArgs e)
        {
            var button = sender as Button;
            if (button?.Tag is User user)
            {
                var result = MessageBox.Show(
                    $"Ești sigur că vrei să ștergi utilizatorul '{user.Username}' ({user.email})?",
                    "Confirmare ștergere",
                    MessageBoxButton.YesNo,
                    MessageBoxImage.Warning);

                if (result == MessageBoxResult.Yes)
                {
                    try
                    {
                        StatusTextBlock.Text = "Se șterge utilizatorul...";
                        await _apiService.DeleteUserAsync(user.Id);
                        MessageBox.Show("Utilizatorul a fost șters cu succes!", 
                            "Succes", MessageBoxButton.OK, MessageBoxImage.Information);
                        LoadUsers(); // Reîncarcă lista
                    }
                    catch (Exception ex)
                    {
                        MessageBox.Show($"Eroare la ștergerea utilizatorului: {ex.Message}", 
                            "Eroare", MessageBoxButton.OK, MessageBoxImage.Error);
                        StatusTextBlock.Text = "Eroare la ștergere";
                    }
                }
            }
        }
    }
}

