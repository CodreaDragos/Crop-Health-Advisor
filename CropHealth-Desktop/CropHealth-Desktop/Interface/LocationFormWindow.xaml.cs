using CropHealth_Desktop.Models;
using CropHealth_Desktop.Services;
using System;
using System.Globalization;
using System.Linq;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;

namespace CropHealth_Desktop.Interface
{
    public partial class LocationFormWindow : Window
    {
        private readonly BackendApiService _apiService;
        private readonly Location _editLocation;
        private readonly bool _isEditMode;
        private readonly System.Collections.Generic.List<User> _allUsers;

        public LocationFormWindow(BackendApiService apiService, Location locationToEdit = null, System.Collections.Generic.List<User> allUsers = null)
        {
            InitializeComponent();
            _apiService = apiService;
            _editLocation = locationToEdit;
            _isEditMode = locationToEdit != null;
            _allUsers = allUsers ?? new System.Collections.Generic.List<User>();

            // Populate ComboBox with users
            UserComboBox.ItemsSource = _allUsers;

            if (_isEditMode)
            {
                TitleTextBlock.Text = "Editează Locație";
                Title = "Editează Locație";
                
                // Pre-populate fields
                NameTextBox.Text = _editLocation.name;
                LatitudeTextBox.Text = _editLocation.latitude.ToString(CultureInfo.InvariantCulture);
                LongitudeTextBox.Text = _editLocation.longitude.ToString(CultureInfo.InvariantCulture);
                
                // Select user
                if (_editLocation.user != null && _editLocation.user.Id > 0)
                {
                    var selectedUser = _allUsers.FirstOrDefault(u => u.Id == _editLocation.user.Id);
                    if (selectedUser != null)
                    {
                        UserComboBox.SelectedItem = selectedUser;
                    }
                }
            }
            else
            {
                // Set first user as default if exists
                if (_allUsers.Count > 0)
                {
                    UserComboBox.SelectedIndex = 0;
                }
            }
        }

        private async void SaveButton_Click(object sender, RoutedEventArgs e)
        {
            ErrorTextBlock.Visibility = Visibility.Collapsed;
            ErrorTextBlock.Text = "";

            // Validare
            if (string.IsNullOrWhiteSpace(NameTextBox.Text))
            {
                ShowError("Numele locației este obligatoriu!");
                return;
            }

            if (string.IsNullOrWhiteSpace(LatitudeTextBox.Text))
            {
                ShowError("Latitudinea este obligatorie!");
                return;
            }

            if (string.IsNullOrWhiteSpace(LongitudeTextBox.Text))
            {
                ShowError("Longitudinea este obligatorie!");
                return;
            }

            // Validare latitudine
            if (!double.TryParse(LatitudeTextBox.Text, NumberStyles.Float, CultureInfo.InvariantCulture, out double latitude))
            {
                ShowError("Latitudinea trebuie să fie un număr valid!");
                return;
            }

            if (latitude < -90.0 || latitude > 90.0)
            {
                ShowError("Latitudinea trebuie să fie între -90 și 90!");
                return;
            }

            // Validare longitudine
            if (!double.TryParse(LongitudeTextBox.Text, NumberStyles.Float, CultureInfo.InvariantCulture, out double longitude))
            {
                ShowError("Longitudinea trebuie să fie un număr valid!");
                return;
            }

            if (longitude < -180.0 || longitude > 180.0)
            {
                ShowError("Longitudinea trebuie să fie între -180 și 180!");
                return;
            }

            // Validare utilizator
            if (UserComboBox.SelectedItem == null)
            {
                ShowError("Te rugăm selectează un utilizator!");
                return;
            }

            try
            {
                var selectedUser = UserComboBox.SelectedItem as User;
                Location location;
                
                if (_isEditMode)
                {
                    // Update existing location
                    location = new Location
                    {
                        id = _editLocation.id,
                        name = NameTextBox.Text.Trim(),
                        latitude = latitude,
                        longitude = longitude,
                        user = new User { Id = selectedUser.Id }
                    };

                    await _apiService.UpdateLocationAsync(location);
                    MessageBox.Show("Locația a fost actualizată cu succes!", 
                        "Succes", MessageBoxButton.OK, MessageBoxImage.Information);
                }
                else
                {
                    // Create new location
                    location = new Location
                    {
                        id = 0,
                        name = NameTextBox.Text.Trim(),
                        latitude = latitude,
                        longitude = longitude,
                        user = new User { Id = selectedUser.Id }
                    };

                    await _apiService.CreateLocationAsync(location);
                    MessageBox.Show("Locația a fost creată cu succes!", 
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


