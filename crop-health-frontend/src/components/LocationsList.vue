<template>
  <div class="locations-list">
    <div class="header-section">
      <h2>Locațiile Mele Salvate</h2>
      <button @click="$emit('refresh')" class="refresh-btn">🔄 Reîncarcă</button>
    </div>
    
    <!-- Search Bar -->
    <div v-if="!loading && locations.length > 0" class="search-section">
      <div class="search-container">
        <input
          type="text"
          v-model="searchQuery"
          placeholder="🔍 Caută locații după nume..."
          class="search-input"
        />
        <button 
          v-if="searchQuery" 
          @click="clearSearch" 
          class="clear-search-btn"
          title="Șterge căutarea"
        >
          ✕
        </button>
      </div>
      <p v-if="filteredLocations.length !== locations.length" class="search-results-count">
        {{ filteredLocations.length }} din {{ locations.length }} locații găsite
      </p>
    </div>
    
    <div v-if="loading" class="loading">Se încarcă...</div>
    
    <div v-else-if="locations.length === 0" class="empty-state">
      <p>Nu aveți locații salvate.</p>
      <p class="hint">Mergeți la Harta și faceți click pentru a adăuga o locație.</p>
    </div>
    
    <div v-else-if="filteredLocations.length === 0 && searchQuery" class="empty-state">
      <p>Nu s-au găsit locații care să se potrivească cu "{{ searchQuery }}".</p>
      <button @click="clearSearch" class="clear-search-action-btn">Șterge căutarea</button>
    </div>
    
    <div v-else class="locations-grid">
      <div 
        v-for="location in filteredLocations" 
        :key="location.id"
        class="location-card"
      >
        <h3>{{ location.name }}</h3>
        <p class="coordinates">
          📍 Lat: {{ location.latitude.toFixed(4) }}, 
          Lon: {{ location.longitude.toFixed(4) }}
        </p>
        
        <!-- Generate Report Button -->
        <button 
          @click.stop="$emit('generate-report', location)" 
          :disabled="loadingReport === location.id"
          class="generate-report-btn"
        >
          {{ loadingReport === location.id ? '⏳ Se generează...' : '📊 Generează Raport' }}
        </button>
        
        <!-- Latest Report for this location -->
        <div v-if="locationReports[location.id] && locationReports[location.id].length > 0" class="reports-section">
          <h4>Ultimul Raport:</h4>
          <div class="report-item">
            <div class="report-header">
              <span class="report-date">📅 {{ formatDate(locationReports[location.id][0].reportDate) }}</span>
            </div>
            <div class="report-metrics">
              <div class="metric-item">
                <span class="metric-label">NDVI</span>
                <span class="metric-value">{{ locationReports[location.id][0].ndviValue?.toFixed(2) || 'N/A' }}</span>
              </div>
              <div class="metric-item">
                <span class="metric-label">Temp</span>
                <span class="metric-value">{{ locationReports[location.id][0].temperatureValue?.toFixed(1) || 'N/A' }}°C</span>
              </div>
              <div class="metric-item">
                <span class="metric-label">Precip</span>
                <span class="metric-value">{{ locationReports[location.id][0].precipitationValue?.toFixed(1) || 'N/A' }}mm</span>
              </div>
            </div>
            <div v-if="locationReports[location.id][0].aiInterpretation" class="report-ai">
              {{ cleanAIInterpretation(locationReports[location.id][0].aiInterpretation).substring(0, 120) }}{{ cleanAIInterpretation(locationReports[location.id][0].aiInterpretation).length > 120 ? '...' : '' }}
            </div>
            <div class="report-actions">
              <button @click.stop="$emit('view-full-report', locationReports[location.id][0])" class="view-report-btn">
                Vezi Raport Complet
              </button>
              <button 
                v-if="locationReports[location.id].length > 1"
                @click.stop="$emit('view-all-reports', location)" 
                class="view-all-reports-btn"
              >
                📊 Rapoarte Mai Vechi ({{ locationReports[location.id].length - 1 }})
              </button>
            </div>
          </div>
        </div>
        
        <!-- No reports message -->
        <div v-else class="no-reports">
          <p>Nu există rapoarte generate pentru această locație.</p>
        </div>
        
        <!-- Delete Location Button -->
        <div class="location-actions">
          <button 
            @click.stop="handleDeleteLocation(location)" 
            :disabled="deletingLocationId === location.id"
            class="delete-location-btn"
          >
            {{ deletingLocationId === location.id ? '⏳ Se șterge...' : '🗑️ Șterge Locația' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { deleteLocation } from '../services/locationService';

const props = defineProps({
  locations: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  loadingReport: {
    type: Number,
    default: null
  },
  locationReports: {
    type: Object,
    default: () => ({})
  }
});

const emit = defineEmits(['location-selected', 'refresh', 'generate-report', 'view-full-report', 'view-all-reports', 'location-deleted']);

const deletingLocationId = ref(null);
const searchQuery = ref('');

// Filter locations based on search query
const filteredLocations = computed(() => {
  if (!searchQuery.value.trim()) {
    return props.locations;
  }
  const query = searchQuery.value.toLowerCase().trim();
  return props.locations.filter(location => 
    location.name.toLowerCase().includes(query)
  );
});

const clearSearch = () => {
  searchQuery.value = '';
};

const handleDeleteLocation = async (location) => {
  // Confirmation from the user
  const reportCount = props.locationReports[location.id]?.length || 0;
  const message = reportCount > 0
    ? `Ești sigur că vrei să ștergi locația "${location.name}"? \n\nAceasta va șterge și ${reportCount} raport(e) asociate.`
    : `Ești sigur că vrei să ștergi locația "${location.name}"?`;
  
  if (!confirm(message)) {
    return;
  }
  
  deletingLocationId.value = location.id;
  
  try {
    await deleteLocation(location.id);
    emit('location-deleted', location.id);
    alert(`Locația "${location.name}" a fost ștearsă cu succes${reportCount > 0 ? ` (inclusiv ${reportCount} raport(e))` : ''}.`);
    emit('refresh');
  } catch (error) {
    console.error('Error deleting location:', error);
    alert('Eroare la ștergerea locației. Vă rugăm încercați din nou.');
  } finally {
    deletingLocationId.value = null;
  }
};

const formatDate = (dateString) => {
  if (!dateString) return 'N/A';
  const date = new Date(dateString);
  return date.toLocaleDateString('ro-RO', { 
    year: 'numeric', 
    month: 'short', 
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
};

// Clean AI text from JSON metadata
const cleanAIInterpretation = (interpretation) => {
  if (!interpretation) return '';
  

  try {
    const parsed = JSON.parse(interpretation);

    if (parsed && typeof parsed === 'object' && parsed.interpretation) {
      return parsed.interpretation;
    }

    if (typeof parsed === 'string') {
      return parsed;
    }
  } catch (e) {

  }
  

// If it contains embedded JSON, try to extract the interpretation using more robust regex
// Look for pattern: "interpretation": "..." or "interpretation":"..."
  const jsonMatch = interpretation.match(/"interpretation"\s*:\s*"((?:[^"\\]|\\.)*)"/);
  if (jsonMatch && jsonMatch[1]) {
    return jsonMatch[1]
      .replace(/\\"/g, '"')
      .replace(/\\n/g, '\n')
      .replace(/\\t/g, '\t')
      .replace(/\\\\/g, '\\');
  }
  

// Remove common prefixes/suffixes from mock responses
  let cleaned = interpretation
    .replace(/^.*?"interpretation"\s*:\s*"/, '') 
    .replace(/"\s*,\s*"timestamp".*$/, '') 
    .replace(/^.*?mockAI.*?interpretation.*?:/, '')
    .replace(/,\s*"timestamp".*$/, '') 
    .replace(/^[\s"{]*/, '') 
    .replace(/[\s"}]*$/, '') 
    .replace(/^Analiza\s+pentru\s+prompt\s*:\s*/i, '') 
    .replace(/^Analiza\s+pentru\s+prompt\s*/i, '') 
    .trim();
  
  return cleaned || interpretation; 
};
</script>

<style scoped>
.locations-list {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #e0e0e0;
}

.header-section h2 {
  margin: 0;
  color: #2c3e50;
}

.refresh-btn {
  padding: 8px 15px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 0.9em;
}

.refresh-btn:hover {
  background: #2980b9;
}

.locations-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.location-card {
  background: #f8f9fa;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  padding: 20px;
  transition: all 0.3s ease;
}

.location-card:hover {
  border-color: #3498db;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.location-card h3 {
  margin: 0 0 10px 0;
  color: #2c3e50;
  font-size: 1.2em;
}

.coordinates {
  color: #7f8c8d;
  font-size: 0.9em;
  margin: 5px 0;
}

.reports-count {
  color: #27ae60;
  font-size: 0.9em;
  margin: 10px 0;
  font-weight: bold;
}

.generate-report-btn {
  margin-top: 15px;
  padding: 12px 15px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.95em;
  width: 100%;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  transition: all 0.3s ease;
}

.generate-report-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.generate-report-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.reports-section {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 2px solid #e5e7eb;
}

.reports-section h4 {
  margin: 0 0 15px 0;
  color: #2c3e50;
  font-size: 1em;
  font-weight: 600;
}

.report-item {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  border: 2px solid rgba(102, 126, 234, 0.2);
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.1);
}

.report-header {
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(102, 126, 234, 0.2);
}

.report-date {
  font-size: 0.9em;
  color: #667eea;
  font-weight: 600;
}

.report-metrics {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 12px;
}

.metric-item {
  background: white;
  border: 1px solid rgba(102, 126, 234, 0.2);
  border-radius: 8px;
  padding: 10px;
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.metric-label {
  font-size: 0.75em;
  color: #6b7280;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.metric-value {
  font-size: 1.1em;
  color: #667eea;
  font-weight: 700;
}

.report-ai {
  background: white;
  border-left: 3px solid #667eea;
  border-radius: 6px;
  padding: 12px;
  font-size: 0.9em;
  color: #374151;
  margin: 12px 0;
  line-height: 1.6;
  font-style: italic;
}

.report-actions {
  display: flex;
  gap: 10px;
  margin-top: 12px;
}

.view-report-btn {
  flex: 1;
  padding: 10px 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9em;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.view-report-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.view-all-reports-btn {
  flex: 1;
  padding: 10px 16px;
  background: white;
  color: #667eea;
  border: 2px solid #667eea;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9em;
  font-weight: 600;
  transition: all 0.3s ease;
}

.view-all-reports-btn:hover {
  background: rgba(102, 126, 234, 0.1);
  border-color: #764ba2;
  color: #764ba2;
}

.no-reports {
  margin-top: 20px;
  padding: 16px;
  text-align: center;
  background: #f9fafb;
  border-radius: 8px;
  color: #6b7280;
  font-size: 0.9em;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #7f8c8d;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #7f8c8d;
}

.hint {
  font-size: 0.9em;
  margin-top: 10px;
  font-style: italic;
}

.location-actions {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #e0e0e0;
}

.delete-location-btn {
  width: 100%;
  padding: 10px 15px;
  background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9em;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(231, 76, 60, 0.3);
}

.delete-location-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(231, 76, 60, 0.4);
  background: linear-gradient(135deg, #c0392b 0%, #a93226 100%);
}

.delete-location-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.search-section {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #e0e0e0;
}

.search-container {
  position: relative;
  display: flex;
  align-items: center;
  max-width: 500px;
}

.search-input {
  width: 100%;
  padding: 12px 40px 12px 16px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 1em;
  transition: all 0.3s ease;
  background: white;
}

.search-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.clear-search-btn {
  position: absolute;
  right: 8px;
  background: #e0e0e0;
  border: none;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
  font-size: 14px;
  transition: all 0.2s ease;
}

.clear-search-btn:hover {
  background: #c0c0c0;
  color: #333;
}

.search-results-count {
  margin: 8px 0 0 0;
  color: #667eea;
  font-size: 0.9em;
  font-weight: 600;
}

.clear-search-action-btn {
  margin-top: 12px;
  padding: 8px 16px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9em;
  font-weight: 600;
  transition: all 0.3s ease;
}

.clear-search-action-btn:hover {
  background: #764ba2;
  transform: translateY(-1px);
}
</style>

