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
    public partial class ReportManagementWindow : Window
    {
        private readonly BackendApiService _apiService;
        private List<Location> _allLocations;
        private List<User> _allUsers;
        private List<Report> _allReports;

        public ReportManagementWindow(BackendApiService apiService)
        {
            InitializeComponent();
            _apiService = apiService;
            LoadLocations();
            LoadUsers();
            LoadReports();
        }

        private async void LoadLocations()
        {
            try
            {
                _allLocations = await _apiService.GetAllLocationsAsync();
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Eroare la încărcarea locațiilor: {ex.Message}", 
                    "Eroare", MessageBoxButton.OK, MessageBoxImage.Error);
                _allLocations = new List<Location>();
            }
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

        private async void LoadReports()
        {
            try
            {
                StatusTextBlock.Text = "Se încarcă rapoartele...";
                _allReports = await _apiService.GetAllReportsAsync();
                ApplyFilter();
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Eroare la încărcarea rapoartelor: {ex.Message}", 
                    "Eroare", MessageBoxButton.OK, MessageBoxImage.Error);
                StatusTextBlock.Text = "Eroare la încărcare";
                _allReports = new List<Report>();
            }
        }

        private void ApplyFilter()
        {
            if (_allReports == null)
            {
                ReportsDataGrid.ItemsSource = null;
                StatusTextBlock.Text = "Gata - 0 raport(e) găsit(e)";
                return;
            }

            var searchText = SearchTextBox?.Text?.ToLower().Trim() ?? "";
            List<Report> filteredReports;

            if (string.IsNullOrWhiteSpace(searchText))
            {
                filteredReports = _allReports;
            }
            else
            {
                filteredReports = _allReports.Where(r =>
                    (r.id.ToString().Contains(searchText)) ||
                    (r.ndviValue.ToString().Contains(searchText)) ||
                    (r.temperatureValue.ToString().Contains(searchText)) ||
                    (r.precipitationValue.ToString().Contains(searchText)) ||
                    (r.aiInterpretation?.ToLower().Contains(searchText) ?? false) ||
                    (r.reportDate.ToString("dd.MM.yyyy HH:mm").ToLower().Contains(searchText)) ||
                    (r.location?.name?.ToLower().Contains(searchText) ?? false) ||
                    (r.location?.latitude.ToString().Contains(searchText) ?? false) ||
                    (r.location?.longitude.ToString().Contains(searchText) ?? false) ||
                    (r.location?.user?.Username?.ToLower().Contains(searchText) ?? false) ||
                    (r.location?.user?.email?.ToLower().Contains(searchText) ?? false)
                ).ToList();
            }

            ReportsDataGrid.ItemsSource = filteredReports;
            StatusTextBlock.Text = $"Gata - {filteredReports.Count} din {_allReports.Count} raport(e)";
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
            LoadLocations();
            LoadUsers();
            LoadReports();
        }

        private void AddReportButton_Click(object sender, RoutedEventArgs e)
        {
            if (_allLocations == null || _allLocations.Count == 0)
            {
                MessageBox.Show("Nu există locații disponibile. Trebuie să existe cel puțin o locație pentru a adăuga un raport.", 
                    "Eroare", MessageBoxButton.OK, MessageBoxImage.Warning);
                return;
            }

            var reportForm = new ReportFormWindow(_apiService, null, _allLocations, _allUsers);
            if (reportForm.ShowDialog() == true)
            {
                LoadReports(); // Reload list after add
            }
        }

        private void EditReport_Click(object sender, RoutedEventArgs e)
        {
            var button = sender as Button;
            if (button?.Tag is Report report)
            {
                if (_allLocations == null || _allLocations.Count == 0)
                {
                    MessageBox.Show("Nu există locații disponibile.", 
                        "Eroare", MessageBoxButton.OK, MessageBoxImage.Warning);
                    return;
                }

                var reportForm = new ReportFormWindow(_apiService, report, _allLocations, _allUsers);
                if (reportForm.ShowDialog() == true)
                {
                    LoadReports(); // Reîncarcă lista după editare
                }
            }
        }

        private async void DeleteReport_Click(object sender, RoutedEventArgs e)
        {
            var button = sender as Button;
            if (button?.Tag is Report report)
            {
                var result = MessageBox.Show(
                    $"Ești sigur că vrei să ștergi raportul (ID: {report.id})?",
                    "Confirmare ștergere",
                    MessageBoxButton.YesNo,
                    MessageBoxImage.Warning);

                if (result == MessageBoxResult.Yes)
                {
                    try
                    {
                        StatusTextBlock.Text = "Se șterge raportul...";
                        await _apiService.DeleteReportAsync(report.id);
                        MessageBox.Show("Raportul a fost șters cu succes!", 
                            "Succes", MessageBoxButton.OK, MessageBoxImage.Information);
                        LoadReports(); // Reîncarcă lista
                    }
                    catch (Exception ex)
                    {
                        MessageBox.Show($"Eroare la ștergerea raportului: {ex.Message}", 
                            "Eroare", MessageBoxButton.OK, MessageBoxImage.Error);
                        StatusTextBlock.Text = "Eroare la ștergere";
                    }
                }
            }
        }
    }
}


