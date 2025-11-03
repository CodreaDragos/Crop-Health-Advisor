<template>
  <div class="dashboard-container">
    <Navigation 
      :active-view="currentView" 
      @nav-click="handleNavClick"
      @logout="$emit('logout')"
    />
    
    <div class="main-content">
      <header class="top-header">
        <h1>{{ getPageTitle() }}</h1>
      </header>
      
      <div class="content-area">
        <!-- Dashboard View: Just Map -->
        <div v-if="currentView === 'dashboard'" class="dashboard-view">
          <div class="map-section">
                <MapComponent 
                  v-if="currentView === 'dashboard'"
                  @location-selected="handleMapClick"
                  @save-location-clicked="showLocationForm = true"
                  :saved-locations="savedLocations"
                  ref="mapComponent"
                  key="map-dashboard"
                />
          </div>
          
          <div class="map-info" v-if="!markerPlaced">
            <p class="info-text">📌 Faceți click pe hartă pentru a plasa un pin</p>
            <p class="info-text-sub">Apoi apăsați "Salvează Locația" pentru a o salva</p>
          </div>
          <div v-else class="map-info marker-placed-info">
            <p class="info-text">✅ Pin plasat! Apăsați "Salvează Locația" pentru a continua.</p>
          </div>
        </div>
        
        <!-- Locations View: List of saved locations with reports -->
        <div v-else-if="currentView === 'locations'" class="locations-view">
                 <LocationsList 
                   :locations="savedLocations"
                   :loading-report="loadingReportId"
                   :location-reports="locationReports"
                   @location-selected="handleLocationSelect"
                   @refresh="loadSavedLocations"
                   @generate-report="handleGenerateReport"
                   @view-full-report="handleViewFullReport"
                   @view-all-reports="handleViewAllReports"
                   @location-deleted="handleLocationDeleted"
                 />
        </div>
        
        <!-- History View: Past reports -->
        <div v-else-if="currentView === 'history'" class="history-view">
                 <HistoryPanel 
                   :selected-location-id="selectedLocationId"
                   @location-selected="selectedLocationId = $event"
                   @view-full-report="handleViewFullReport"
                   @report-deleted="handleReportDeleted"
                 />
        </div>
        
        <!-- Profile View -->
        <div v-else-if="currentView === 'profile'" class="profile-view">
          <ProfilePanel />
        </div>
      </div>
    </div>
    
    <!-- Location Form Modal -->
    <div v-if="showLocationForm" class="modal-overlay" @click.self="showLocationForm = false">
      <div class="modal-content">
        <h3>Salvați noua locație</h3>
        <p class="modal-coords" v-if="selectedLocation.lat">
          📍 {{ selectedLocation.lat.toFixed(4) }}, {{ selectedLocation.lon.toFixed(4) }}
        </p>
        <input
          type="text"
          v-model="selectedLocation.name"
          placeholder="Nume Locație (ex: Ferma Est)"
          class="input-field"
          @keyup.enter="saveLocation"
        />
        <div class="modal-actions">
          <button @click="saveLocation" :disabled="isLoading" class="btn-primary">
            {{ isLoading ? 'Salvează...' : 'Salvare Locație' }}
          </button>
          <button @click="showLocationForm = false" class="btn-secondary">Anulează</button>
        </div>
      </div>
    </div>
    
    <!-- Full Report Modal -->
    <div v-if="currentFullReport" class="modal-overlay" @click.self="currentFullReport = null">
      <div class="modal-content report-modal">
        <h3>Raport Complet</h3>
        <ResultsPanel :report-data="currentFullReport" />
        <div class="modal-actions">
          <button @click="currentFullReport = null" class="btn-secondary">Închide</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue';
import Navigation from './Navigation.vue';
import MapComponent from './MapComponent.vue';
import LocationsList from './LocationsList.vue';
import HistoryPanel from './HistoryPanel.vue';
import ProfilePanel from './ProfilePanel.vue';
import ResultsPanel from './ResultsPanel.vue';
import { getUserLocations, createLocation, getLocationReports } from '../services/locationService';
import { getAuthHeader } from '../services/authService';
import axios from 'axios';

const API_URL = 'http://localhost:8081/api';

const emit = defineEmits(['logout']);

const currentView = ref('dashboard');
const savedLocations = ref([]);
const isLoading = ref(false);
const selectedLocation = ref({ lat: null, lon: null, name: "" });
const showLocationForm = ref(false);
const loadingReportId = ref(null);
const locationReports = ref({}); // { locationId: [reports] }
const currentFullReport = ref(null);
const selectedLocationId = ref(null);

const mapComponent = ref(null);

const handleNavClick = (view) => {
  currentView.value = view;
};

// Watch pentru a reinițializa harta când revenim la dashboard
// Nu mai încercăm să reatașăm listener-ul manual - onMapReady se ocupă de asta automat
watch(currentView, (newView, oldView) => {
  if (newView === 'dashboard' && oldView !== 'dashboard') {
    console.log('Navigating to dashboard - map will be reinitialized automatically');
    // Componenta MapComponent va fi remontată (datorită v-if)
    // și onMapReady va fi apelat automat, atașând listener-ul
  }
});

const getPageTitle = () => {
  const titles = {
    dashboard: 'Dashboard - Harta',
    locations: 'Locații Salvate',
    history: 'Istoric Rapoarte',
    profile: 'Profil Utilizator'
  };
  return titles[currentView.value] || 'Dashboard';
};

const markerPlaced = ref(false);

const handleMapClick = ({ lat, lon }) => {
  console.log('Dashboard received map click:', lat, lon); // Debug log
  markerPlaced.value = true;
  selectedLocation.value.lat = lat;
  selectedLocation.value.lon = lon;
  // Nu mai deschidem automat formularul - așteptăm click pe buton
};

const saveLocation = async () => {
  if (!selectedLocation.value.name) {
    alert("Vă rugăm introduceți un nume pentru locație.");
    return;
  }

  isLoading.value = true;
  try {
    // Get user ID - for now using stored value or default to 1
    const userId = localStorage.getItem('user_id') || 1;
    
    const payload = {
      name: selectedLocation.value.name,
      latitude: selectedLocation.value.lat,
      longitude: selectedLocation.value.lon,
      user: { id: userId },
    };

    const response = await axios.post(`${API_URL}/locations`, payload, {
      headers: getAuthHeader(),
    });

    showLocationForm.value = false;
    markerPlaced.value = false;
    selectedLocation.value = { lat: null, lon: null, name: "" };
    
    // Reload locations list
    await loadSavedLocations();
    
    alert(`Locația "${response.data.name}" a fost salvată cu succes!`);
    
    // NU generăm raport automat - utilizatorul va genera manual din "Locații Salvate"
  } catch (error) {
    console.error("Eroare la salvarea locației:", error);
    if (error.response && error.response.status === 401) {
      alert("Sesiunea a expirat sau nu sunteți autentificat. Vă rugăm faceți login din nou.");
    } else {
      alert("Eroare la salvarea locației. Verificați backend-ul.");
    }
  } finally {
    isLoading.value = false;
  }
};

const handleGenerateReport = async (location) => {
  loadingReportId.value = location.id;
  
  try {
    const response = await axios.get(`${API_URL}/reports`, {
      params: { locationId: location.id },
      headers: getAuthHeader(),
    });

    // Add report to locationReports
    if (!locationReports.value[location.id]) {
      locationReports.value[location.id] = [];
    }
    locationReports.value[location.id].unshift(response.data); // Add at beginning
    
    // Reload locations to refresh report count
    await loadSavedLocations();
    
    alert("Raport generat cu succes!");
  } catch (error) {
    console.error("Eroare la generarea raportului:", error);
    if (error.response && error.response.status === 401) {
      alert("Sesiunea a expirat sau nu sunteți autentificat. Vă rugăm faceți login din nou.");
    } else {
      alert("Eroare la generarea raportului.");
    }
  } finally {
    loadingReportId.value = null;
  }
};

const handleViewFullReport = (report) => {
  currentFullReport.value = report;
};

const handleViewAllReports = (location) => {
  selectedLocationId.value = location.id;
  currentView.value = 'history'; // Navigate to history view
};

const handleLocationSelect = (location) => {
  // Do nothing - locations will handle report generation themselves
};

const handleLocationDeleted = async (locationId) => {
    // Reîncarcă locațiile și rapoartele după ștergere
    await loadSavedLocations();
    await loadLocationReports();
    // Dacă locația ștearsă era selectată în history, resetează selecția
    if (selectedLocationId.value === locationId) {
      selectedLocationId.value = null;
    }
  };

const handleReportDeleted = async (reportId) => {
  // Reîncarcă rapoartele pentru locațiile curente
  await loadLocationReports();
};

const loadSavedLocations = async () => {
  try {
    const userId = localStorage.getItem('user_id') || 1;
    savedLocations.value = await getUserLocations(userId);
  } catch (error) {
    console.error('Error loading locations:', error);
  }
};

const loadLocationReports = async () => {
  // Load reports for each location
  for (const location of savedLocations.value) {
    try {
      const reports = await getLocationReports(location.id);
      locationReports.value[location.id] = reports;
    } catch (error) {
      console.error(`Error loading reports for location ${location.id}:`, error);
      locationReports.value[location.id] = [];
    }
  }
};

onMounted(async () => {
  await loadSavedLocations();
  await loadLocationReports();
});
</script>

<style scoped>
.dashboard-container {
  display: flex;
  min-height: 100vh;
  background: #f5f5f5;
}

.main-content {
  margin-left: 250px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.top-header {
  background: white;
  padding: 20px 30px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  border-bottom: 1px solid #e0e0e0;
}

.top-header h1 {
  margin: 0;
  color: #2c3e50;
  font-size: 1.8em;
}

.content-area {
  padding: 30px;
  flex: 1;
}

.dashboard-view {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.map-info {
  background: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  text-align: center;
}

.info-text {
  font-size: 1.2em;
  color: #2c3e50;
  margin-bottom: 10px;
}

.info-text-sub {
  font-size: 0.95em;
  color: #7f8c8d;
}

.map-section {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  min-height: 600px;
  height: calc(100vh - 200px);
  display: flex;
  flex-direction: column;
}

.results-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.loading-section {
  grid-column: 1 / -1;
  text-align: center;
  padding: 40px;
}

.spinner {
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 30px;
  border-radius: 8px;
  min-width: 400px;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 4px 20px rgba(0,0,0,0.3);
}

.report-modal {
  max-width: 800px;
  min-width: 600px;
}

.modal-coords {
  color: #7f8c8d;
  font-size: 0.9em;
  margin-bottom: 15px;
}

.input-field {
  width: 100%;
  padding: 12px;
  margin: 15px 0;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 1em;
}

.modal-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.btn-primary {
  padding: 10px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1em;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  transition: all 0.3s ease;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary {
  padding: 10px 20px;
  background: #f3f4f6;
  color: #374151;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1em;
  font-weight: 500;
  transition: all 0.3s ease;
}

.btn-secondary:hover {
  background: #e5e7eb;
  border-color: #d1d5db;
}
</style>

