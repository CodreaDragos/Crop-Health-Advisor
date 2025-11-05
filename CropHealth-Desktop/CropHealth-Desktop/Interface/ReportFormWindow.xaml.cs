using CropHealth_Desktop.Models;
using CropHealth_Desktop.Services;
using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;

namespace CropHealth_Desktop.Interface
{
    public partial class ReportFormWindow : Window
    {
        private readonly BackendApiService _apiService;
        private readonly Report _editReport;
        private readonly bool _isEditMode;
        private readonly List<Location> _allLocations;
        private readonly List<User> _allUsers;

        public ReportFormWindow(BackendApiService apiService, Report reportToEdit = null, 
            List<Location> allLocations = null, List<User> allUsers = null)
        {
            InitializeComponent();
            _apiService = apiService;
            _editReport = reportToEdit;
            _isEditMode = reportToEdit != null;
            _allLocations = allLocations ?? new List<Location>();
            _allUsers = allUsers ?? new List<User>();

            // Populate ComboBox with locations
            LocationComboBox.ItemsSource = _allLocations;

            if (_isEditMode)
            {
                TitleTextBlock.Text = "Editează Raport";
                Title = "Editează Raport";
                
                // Pre-populate fields
                NdviValueTextBox.Text = _editReport.ndviValue.ToString(CultureInfo.InvariantCulture);
                TemperatureValueTextBox.Text = _editReport.temperatureValue.ToString(CultureInfo.InvariantCulture);
                PrecipitationValueTextBox.Text = _editReport.precipitationValue.ToString(CultureInfo.InvariantCulture);
                AiInterpretationTextBox.Text = _editReport.aiInterpretation ?? "";
                
                // Select the date and time
                if (_editReport.reportDate != null && _editReport.reportDate != default(DateTime))
                {
                    ReportDateDatePicker.SelectedDate = _editReport.reportDate.Date;
                    ReportTimeTextBox.Text = _editReport.reportDate.ToString("HH:mm");
                }
                else
                {
                    ReportDateDatePicker.SelectedDate = DateTime.Now.Date;
                    ReportTimeTextBox.Text = DateTime.Now.ToString("HH:mm");
                }
                
                // Location is read-only in edit mode
                LocationComboBox.IsEnabled = false;
                if (_editReport.location != null && _editReport.location.id > 0)
                {
                    var selectedLocation = _allLocations.FirstOrDefault(l => l.id == _editReport.location.id);
                    if (selectedLocation != null)
                    {
                        LocationComboBox.SelectedItem = selectedLocation;
                        UpdateLocationInfo(selectedLocation);
                    }
                }
            }
            else
            {
                // Set current date and time
                ReportDateDatePicker.SelectedDate = DateTime.Now.Date;
                ReportTimeTextBox.Text = DateTime.Now.ToString("HH:mm");
                
                // Set first location as default if exists
                if (_allLocations.Count > 0)
                {
                    LocationComboBox.SelectedIndex = 0;
                    UpdateLocationInfo(_allLocations[0]);
                }
            }
        }

        private void LocationComboBox_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            if (LocationComboBox.SelectedItem is Location selectedLocation)
            {
                UpdateLocationInfo(selectedLocation);
            }
        }

        private void UpdateLocationInfo(Location location)
        {
            if (location != null)
            {
                LocationNameTextBlock.Text = $"Nume: {location.name}";
                LocationCoordinatesTextBlock.Text = $"Coordonate: {location.latitude:F4}, {location.longitude:F4}";
                
                if (location.user != null)
                {
                    LocationUserTextBlock.Text = $"Utilizator: {location.user.Username} ({location.user.email})";
                }
                else
                {
                    LocationUserTextBlock.Text = "Utilizator: N/A";
                }
                
                LocationInfoPanel.Visibility = Visibility.Visible;
            }
        }

        private async void SaveButton_Click(object sender, RoutedEventArgs e)
        {
            ErrorTextBlock.Visibility = Visibility.Collapsed;
            ErrorTextBlock.Text = "";

     
            if (LocationComboBox.SelectedItem == null)
            {
                ShowError("Te rugăm selectează o locație!");
                return;
            }

   
            if (string.IsNullOrWhiteSpace(NdviValueTextBox.Text))
            {
                ShowError("Valoarea NDVI este obligatorie!");
                return;
            }

            if (!double.TryParse(NdviValueTextBox.Text, NumberStyles.Float, CultureInfo.InvariantCulture, out double ndviValue))
            {
                ShowError("Valoarea NDVI trebuie să fie un număr valid!");
                return;
            }

            if (ndviValue < -1.0 || ndviValue > 1.0)
            {
                ShowError("Valoarea NDVI trebuie să fie între -1.0 și 1.0!");
                return;
            }

  
            if (string.IsNullOrWhiteSpace(TemperatureValueTextBox.Text))
            {
                ShowError("Temperatura este obligatorie!");
                return;
            }

            if (!double.TryParse(TemperatureValueTextBox.Text, NumberStyles.Float, CultureInfo.InvariantCulture, out double temperatureValue))
            {
                ShowError("Temperatura trebuie să fie un număr valid!");
                return;
            }

            if (temperatureValue < -50.0 || temperatureValue > 60.0)
            {
                ShowError("Temperatura trebuie să fie între -50°C și 60°C!");
                return;
            }

 
            if (string.IsNullOrWhiteSpace(PrecipitationValueTextBox.Text))
            {
                ShowError("Precipitațiile sunt obligatorii!");
                return;
            }

            if (!double.TryParse(PrecipitationValueTextBox.Text, NumberStyles.Float, CultureInfo.InvariantCulture, out double precipitationValue))
            {
                ShowError("Precipitațiile trebuie să fie un număr valid!");
                return;
            }

            if (precipitationValue < 0)
            {
                ShowError("Precipitațiile nu pot fi negative!");
                return;
            }

            // Validate date
            if (ReportDateDatePicker.SelectedDate == null)
            {
                ShowError("Data raportului este obligatorie!");
                return;
            }

 
            DateTime reportDate;
            if (!DateTime.TryParseExact(ReportTimeTextBox.Text, "HH:mm", CultureInfo.InvariantCulture, DateTimeStyles.None, out DateTime time))
            {
                ShowError("Ora trebuie să fie în format HH:mm (ex: 14:30)!");
                return;
            }

            reportDate = ReportDateDatePicker.SelectedDate.Value.Date.Add(time.TimeOfDay);

            try
            {
                var selectedLocation = LocationComboBox.SelectedItem as Location;
                Report report;
                
                if (_isEditMode)
                {
                    // Update existing report (location remains the same)
                    report = new Report
                    {
                        id = _editReport.id,
                        ndviValue = ndviValue,
                        temperatureValue = temperatureValue,
                        precipitationValue = precipitationValue,
                        aiInterpretation = AiInterpretationTextBox.Text.Trim(),
                        reportDate = reportDate,
                        location = new Location { id = _editReport.location.id } // Location cannot be changed
                    };

                    await _apiService.UpdateReportAsync(report);
                    MessageBox.Show("Raportul a fost actualizat cu succes!", 
                        "Succes", MessageBoxButton.OK, MessageBoxImage.Information);
                }
                else
                {
                    // Create new report
                    report = new Report
                    {
                        id = 0,
                        ndviValue = ndviValue,
                        temperatureValue = temperatureValue,
                        precipitationValue = precipitationValue,
                        aiInterpretation = AiInterpretationTextBox.Text.Trim(),
                        reportDate = reportDate,
                        location = new Location { id = selectedLocation.id }
                    };

                    await _apiService.CreateReportAsync(report);
                    MessageBox.Show("Raportul a fost creat cu succes!", 
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

