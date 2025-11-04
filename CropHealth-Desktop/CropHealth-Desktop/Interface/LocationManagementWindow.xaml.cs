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
    public partial class LocationManagementWindow : Window
    {
        private readonly BackendApiService _apiService;
        private List<User> _allUsers;
        private List<Location> _allLocations;

        public LocationManagementWindow(BackendApiService apiService)
        {
            InitializeComponent();
            _apiService = apiService;
            LoadUsers();
            LoadLocations();
        }

        private async void LoadUsers()
        {
            try
            {
                _allUsers = await _apiService.GetAllUsersAsync();
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Eroare la încărcarea utilizatorilor: {ex.Message}", 
                    "Eroare", MessageBoxButton.OK, MessageBoxImage.Error);
                _allUsers = new List<User>();
            }
        }

        private async void LoadLocations()
        {
            try
            {
                StatusTextBlock.Text = "Se încarcă locațiile...";
                _allLocations = await _apiService.GetAllLocationsAsync();
                ApplyFilter();
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Eroare la încărcarea locațiilor: {ex.Message}", 
                    "Eroare", MessageBoxButton.OK, MessageBoxImage.Error);
                StatusTextBlock.Text = "Eroare la încărcare";
                _allLocations = new List<Location>();
            }
        }

        private void ApplyFilter()
        {
            if (_allLocations == null)
            {
                LocationsDataGrid.ItemsSource = null;
                StatusTextBlock.Text = "Gata - 0 locație(i) găsită(e)";
                return;
            }

            var searchText = SearchTextBox?.Text?.ToLower().Trim() ?? "";
            List<Location> filteredLocations;

            if (string.IsNullOrWhiteSpace(searchText))
            {
                filteredLocations = _allLocations;
            }
            else
            {
                filteredLocations = _allLocations.Where(l =>
                    (l.name?.ToLower().Contains(searchText) ?? false) ||
                    (l.id.ToString().Contains(searchText)) ||
                    (l.latitude.ToString().Contains(searchText)) ||
                    (l.longitude.ToString().Contains(searchText)) ||
                    (l.user?.Username?.ToLower().Contains(searchText) ?? false) ||
                    (l.user?.email?.ToLower().Contains(searchText) ?? false)
                ).ToList();
            }

            LocationsDataGrid.ItemsSource = filteredLocations;
            StatusTextBlock.Text = $"Gata - {filteredLocations.Count} din {_allLocations.Count} locație(i)";
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
            LoadLocations();
        }

        private void AddLocationButton_Click(object sender, RoutedEventArgs e)
        {
            if (_allUsers == null || _allUsers.Count == 0)
            {
                MessageBox.Show("Nu există utilizatori disponibili. Trebuie să existe cel puțin un utilizator pentru a adăuga o locație.", 
                    "Eroare", MessageBoxButton.OK, MessageBoxImage.Warning);
                return;
            }

            var locationForm = new LocationFormWindow(_apiService, null, _allUsers);
            if (locationForm.ShowDialog() == true)
            {
                LoadLocations(); // Reload list after add
            }
        }

        private void EditLocation_Click(object sender, RoutedEventArgs e)
        {
            var button = sender as Button;
            if (button?.Tag is Location location)
            {
                if (_allUsers == null || _allUsers.Count == 0)
                {
                    MessageBox.Show("Nu există utilizatori disponibili.", 
                        "Eroare", MessageBoxButton.OK, MessageBoxImage.Warning);
                    return;
                }

                var locationForm = new LocationFormWindow(_apiService, location, _allUsers);
                if (locationForm.ShowDialog() == true)
                {
                    LoadLocations(); // Reîncarcă lista după editare
                }
            }
        }

        private async void DeleteLocation_Click(object sender, RoutedEventArgs e)
        {
            var button = sender as Button;
            if (button?.Tag is Location location)
            {
                var result = MessageBox.Show(
                    $"Ești sigur că vrei să ștergi locația '{location.name}' (ID: {location.id})?",
                    "Confirmare ștergere",
                    MessageBoxButton.YesNo,
                    MessageBoxImage.Warning);

                if (result == MessageBoxResult.Yes)
                {
                    try
                    {
                        StatusTextBlock.Text = "Se șterge locația...";
                        await _apiService.DeleteLocationAsync(location.id);
                        MessageBox.Show("Locația a fost ștearsă cu succes!", 
                            "Succes", MessageBoxButton.OK, MessageBoxImage.Information);
                        LoadLocations(); // Reîncarcă lista
                    }
                    catch (Exception ex)
                    {
                        MessageBox.Show($"Eroare la ștergerea locației: {ex.Message}", 
                            "Eroare", MessageBoxButton.OK, MessageBoxImage.Error);
                        StatusTextBlock.Text = "Eroare la ștergere";
                    }
                }
            }
        }
    }
}


