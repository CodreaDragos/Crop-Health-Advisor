<template>
  <div class="history-panel">
    <div class="history-header">
      <h2>📊 Istoric Rapoarte</h2>
      <button v-if="selectedLocationId" @click="clearSelection" class="back-btn">
        ← Înapoi la Toate Locațiile
      </button>
      <button @click="handleRefresh" class="refresh-btn">🔄 Reîncarcă</button>
    </div>
    
    <!-- Location Selector -->
    <div v-if="!selectedLocationId && locations.length > 0" class="locations-selector">
      <h3>Selectează o locație pentru a vedea rapoartele:</h3>
      <div class="locations-grid">
        <div 
          v-for="location in locations" 
          :key="location.id"
          @click="selectLocation(location.id)"
          class="location-selector-card"
        >
          <h4>{{ location.name }}</h4>
          <p class="location-coords">
            📍 {{ location.latitude.toFixed(4) }}, {{ location.longitude.toFixed(4) }}
          </p>
          <p class="reports-count" v-if="locationReportCounts[location.id]">
            {{ locationReportCounts[location.id] }} raport(e)
          </p>
        </div>
      </div>
    </div>
    
    <!-- Reports List -->
    <div v-if="selectedLocationId && reports.length > 0" class="reports-list">
      <div class="location-info">
        <h3>{{ selectedLocationName }}</h3>
        <p class="total-reports">{{ filteredReports.length }} din {{ reports.length }} raport(e) afișate</p>
      </div>
      
      <!-- Filter Panel -->
      <div class="filter-panel">
        <div class="filter-header">
          <div class="filter-header-left">
            <h4>🔍 Filtrează Rapoartele</h4>
            <button @click="toggleFilters" class="toggle-filters-btn">
              {{ filtersExpanded ? '▼ Ascunde' : '▶ Afișează Filtrele' }}
            </button>
          </div>
          <button @click="resetFilters" class="reset-filters-btn">Resetează Filtrele</button>
        </div>
        
        <div v-show="filtersExpanded" class="filters-grid">
          <!-- NDVI Filter -->
          <div class="filter-item">
            <label class="filter-label">
              NDVI: {{ Math.min(filters.ndvi.min, filters.ndvi.max).toFixed(2) }} - {{ Math.max(filters.ndvi.min, filters.ndvi.max).toFixed(2) }}
            </label>
            <div class="range-inputs">
              <div class="range-input-group">
                <span class="range-label">Min: {{ filters.ndvi.min.toFixed(2) }}</span>
                <input 
                  type="range" 
                  v-model.number="filters.ndvi.min" 
                  min="-1" 
                  max="1" 
                  step="0.01"
                  class="range-slider"
                />
              </div>
              <div class="range-input-group">
                <span class="range-label">Max: {{ filters.ndvi.max.toFixed(2) }}</span>
                <input 
                  type="range" 
                  v-model.number="filters.ndvi.max" 
                  min="-1" 
                  max="1" 
                  step="0.01"
                  class="range-slider"
                />
              </div>
            </div>
          </div>
          
          <!-- Temperature Filter -->
          <div class="filter-item">
            <label class="filter-label">
              Temperatură (°C): {{ Math.min(filters.temperature.min, filters.temperature.max) }}°C - {{ Math.max(filters.temperature.min, filters.temperature.max) }}°C
            </label>
            <div class="range-inputs">
              <div class="range-input-group">
                <span class="range-label">Min: {{ filters.temperature.min }}°C</span>
                <input 
                  type="range" 
                  v-model.number="filters.temperature.min" 
                  min="-50" 
                  max="60" 
                  step="1"
                  class="range-slider"
                />
              </div>
              <div class="range-input-group">
                <span class="range-label">Max: {{ filters.temperature.max }}°C</span>
                <input 
                  type="range" 
                  v-model.number="filters.temperature.max" 
                  min="-50" 
                  max="60" 
                  step="1"
                  class="range-slider"
                />
              </div>
            </div>
          </div>
          
          <!-- Precipitation Filter -->
          <div class="filter-item">
            <label class="filter-label">
              Precipitații (mm): {{ Math.min(filters.precipitation.min, filters.precipitation.max).toFixed(1) }}mm - {{ Math.max(filters.precipitation.min, filters.precipitation.max).toFixed(1) }}mm
            </label>
            <div class="range-inputs">
              <div class="range-input-group">
                <span class="range-label">Min: {{ filters.precipitation.min.toFixed(1) }}mm</span>
                <input 
                  type="range" 
                  v-model.number="filters.precipitation.min" 
                  min="0" 
                  max="200" 
                  step="0.1"
                  class="range-slider"
                />
              </div>
              <div class="range-input-group">
                <span class="range-label">Max: {{ filters.precipitation.max.toFixed(1) }}mm</span>
                <input 
                  type="range" 
                  v-model.number="filters.precipitation.max" 
                  min="0" 
                  max="200" 
                  step="0.1"
                  class="range-slider"
                />
              </div>
            </div>
          </div>
          
          <!-- EVI Filter -->
          <div class="filter-item">
            <label class="filter-label">
              EVI: {{ Math.min(filters.evi.min, filters.evi.max).toFixed(2) }} - {{ Math.max(filters.evi.min, filters.evi.max).toFixed(2) }}
            </label>
            <div class="range-inputs">
              <div class="range-input-group">
                <span class="range-label">Min: {{ filters.evi.min.toFixed(2) }}</span>
                <input 
                  type="range" 
                  v-model.number="filters.evi.min" 
                  min="-1" 
                  max="1" 
                  step="0.01"
                  class="range-slider"
                />
              </div>
              <div class="range-input-group">
                <span class="range-label">Max: {{ filters.evi.max.toFixed(2) }}</span>
                <input 
                  type="range" 
                  v-model.number="filters.evi.max" 
                  min="-1" 
                  max="1" 
                  step="0.01"
                  class="range-slider"
                />
              </div>
            </div>
          </div>
          
          <!-- NDWI Filter -->
          <div class="filter-item">
            <label class="filter-label">
              NDWI: {{ Math.min(filters.ndwi.min, filters.ndwi.max).toFixed(2) }} - {{ Math.max(filters.ndwi.min, filters.ndwi.max).toFixed(2) }}
            </label>
            <div class="range-inputs">
              <div class="range-input-group">
                <span class="range-label">Min: {{ filters.ndwi.min.toFixed(2) }}</span>
                <input 
                  type="range" 
                  v-model.number="filters.ndwi.min" 
                  min="-1" 
                  max="1" 
                  step="0.01"
                  class="range-slider"
                />
              </div>
              <div class="range-input-group">
                <span class="range-label">Max: {{ filters.ndwi.max.toFixed(2) }}</span>
                <input 
                  type="range" 
                  v-model.number="filters.ndwi.max" 
                  min="-1" 
                  max="1" 
                  step="0.01"
                  class="range-slider"
                />
              </div>
            </div>
          </div>
          
          <!-- Soil Moisture Filter -->
          <div class="filter-item">
            <label class="filter-label">
              Umiditate Sol (%): {{ Math.min(filters.soilMoisture.min, filters.soilMoisture.max).toFixed(1) }}% - {{ Math.max(filters.soilMoisture.min, filters.soilMoisture.max).toFixed(1) }}%
            </label>
            <div class="range-inputs">
              <div class="range-input-group">
                <span class="range-label">Min: {{ filters.soilMoisture.min.toFixed(1) }}%</span>
                <input 
                  type="range" 
                  v-model.number="filters.soilMoisture.min" 
                  min="0" 
                  max="100" 
                  step="0.1"
                  class="range-slider"
                />
              </div>
              <div class="range-input-group">
                <span class="range-label">Max: {{ filters.soilMoisture.max.toFixed(1) }}%</span>
                <input 
                  type="range" 
                  v-model.number="filters.soilMoisture.max" 
                  min="0" 
                  max="100" 
                  step="0.1"
                  class="range-slider"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div 
        v-for="report in filteredReports" 
        :key="report.id"
        class="report-card"
      >
        <div class="report-header">
          <span class="report-date">
            📅 {{ formatDate(report.reportDate) }}
          </span>
        </div>
        <div class="report-content">
          <div class="metrics">
            <div class="metric">
              <span class="metric-label">NDVI</span>
              <span class="metric-value">{{ report.ndviValue?.toFixed(3) || 'N/A' }}</span>
            </div>
            <div class="metric">
              <span class="metric-label">Temperatură</span>
              <span class="metric-value">{{ report.temperatureValue?.toFixed(1) || 'N/A' }}°C</span>
            </div>
            <div class="metric">
              <span class="metric-label">Precipitații</span>
              <span class="metric-value">{{ report.precipitationValue?.toFixed(1) || 'N/A' }}mm</span>
            </div>
          </div>
          <div v-if="report.aiInterpretation" class="interpretation">
            <p>{{ cleanAIInterpretation(report.aiInterpretation) }}</p>
          </div>
          <div class="report-actions">
            <button @click="$emit('view-full-report', report)" class="view-full-report-btn">
              Vezi Raport Complet
            </button>
            <button 
              @click="handleDeleteReport(report)" 
              :disabled="deletingReportId === report.id"
              class="delete-report-btn"
            >
              {{ deletingReportId === report.id ? '⏳ Se șterge...' : '🗑️ Șterge' }}
            </button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Empty States -->
    <div v-if="selectedLocationId && reports.length === 0" class="empty-state">
      <p>Nu există rapoarte pentru această locație.</p>
    </div>
    
    <div v-if="selectedLocationId && reports.length > 0 && filteredReports.length === 0" class="empty-state">
      <p>Nu s-au găsit rapoarte care să corespundă filtrilor selectate.</p>
      <button @click="resetFilters" class="reset-filters-action-btn">Resetează Filtrele</button>
    </div>
    
    <div v-if="!selectedLocationId && locations.length === 0" class="empty-state">
      <p>Nu aveți locații salvate.</p>
      <p class="hint">Mergeți la "Locații Salvate" pentru a adăuga o locație.</p>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed, onMounted } from 'vue';
import { getLocationReports, deleteReport } from '../services/locationService';
import { getUserLocations } from '../services/locationService';

const props = defineProps({
  selectedLocationId: {
    type: Number,
    default: null
  }
});

const emit = defineEmits(['refresh', 'location-selected', 'view-full-report', 'report-deleted']);

const deletingReportId = ref(null);
const filtersExpanded = ref(false); // Start collapsed

const toggleFilters = () => {
  filtersExpanded.value = !filtersExpanded.value;
};

const handleDeleteReport = async (report) => {
  if (!confirm(`Ești sigur că vrei să ștergi raportul din ${formatDate(report.reportDate)}?`)) {
    return;
  }
  
  deletingReportId.value = report.id;
  
  try {
    await deleteReport(report.id);
    emit('report-deleted', report.id);
    // Reload reports for current location
    await loadReports();
    alert('Raportul a fost șters cu succes.');
    emit('refresh');
  } catch (error) {
    console.error('Error deleting report:', error);
    alert('Eroare la ștergerea raportului. Vă rugăm încercați din nou.');
  } finally {
    deletingReportId.value = null;
  }
};

const reports = ref([]);
const locations = ref([]);
const locationReportCounts = ref({});

// Filter state
const filters = ref({
  ndvi: { min: -1.0, max: 1.0 },
  temperature: { min: -50, max: 60 },
  precipitation: { min: 0, max: 200 },
  evi: { min: -1.0, max: 1.0 },
  ndwi: { min: -1.0, max: 1.0 },
  soilMoisture: { min: 0, max: 100 }
});

// Filtered reports based on filter criteria
const filteredReports = computed(() => {
  return reports.value.filter(report => {
    // NDVI filter (ensure min <= max)
    const ndvi = report.ndviValue;
    if (ndvi !== null && ndvi !== undefined) {
      const ndviMin = Math.min(filters.value.ndvi.min, filters.value.ndvi.max);
      const ndviMax = Math.max(filters.value.ndvi.min, filters.value.ndvi.max);
      if (ndvi < ndviMin || ndvi > ndviMax) {
        return false;
      }
    }
    
    // Temperature filter (ensure min <= max)
    const temp = report.temperatureValue;
    if (temp !== null && temp !== undefined) {
      const tempMin = Math.min(filters.value.temperature.min, filters.value.temperature.max);
      const tempMax = Math.max(filters.value.temperature.min, filters.value.temperature.max);
      if (temp < tempMin || temp > tempMax) {
        return false;
      }
    }
    
    // Precipitation filter (ensure min <= max)
    const precip = report.precipitationValue;
    if (precip !== null && precip !== undefined) {
      const precipMin = Math.min(filters.value.precipitation.min, filters.value.precipitation.max);
      const precipMax = Math.max(filters.value.precipitation.min, filters.value.precipitation.max);
      if (precip < precipMin || precip > precipMax) {
        return false;
      }
    }
    
    // EVI filter (ensure min <= max)
    const evi = report.eviValue;
    if (evi !== null && evi !== undefined) {
      const eviMin = Math.min(filters.value.evi.min, filters.value.evi.max);
      const eviMax = Math.max(filters.value.evi.min, filters.value.evi.max);
      if (evi < eviMin || evi > eviMax) {
        return false;
      }
    }
    
    // NDWI filter (ensure min <= max)
    const ndwi = report.ndwiValue;
    if (ndwi !== null && ndwi !== undefined) {
      const ndwiMin = Math.min(filters.value.ndwi.min, filters.value.ndwi.max);
      const ndwiMax = Math.max(filters.value.ndwi.min, filters.value.ndwi.max);
      if (ndwi < ndwiMin || ndwi > ndwiMax) {
        return false;
      }
    }
    
    // Soil Moisture filter (ensure min <= max)
    const soilMoisture = report.soilMoisture;
    if (soilMoisture !== null && soilMoisture !== undefined) {
      const soilMin = Math.min(filters.value.soilMoisture.min, filters.value.soilMoisture.max);
      const soilMax = Math.max(filters.value.soilMoisture.min, filters.value.soilMoisture.max);
      if (soilMoisture < soilMin || soilMoisture > soilMax) {
        return false;
      }
    }
    
    return true;
  });
});

const resetFilters = () => {
  filters.value = {
    ndvi: { min: -1.0, max: 1.0 },
    temperature: { min: -50, max: 60 },
    precipitation: { min: 0, max: 200 },
    evi: { min: -1.0, max: 1.0 },
    ndwi: { min: -1.0, max: 1.0 },
    soilMoisture: { min: 0, max: 100 }
  };
};

const selectedLocationName = computed(() => {
  if (!props.selectedLocationId) return '';
  const location = locations.value.find(l => l.id === props.selectedLocationId);
  return location ? location.name : '';
});

const formatDate = (dateString) => {
  if (!dateString) return 'N/A';
  const date = new Date(dateString);
  return date.toLocaleDateString('ro-RO', { 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
};

const cleanAIInterpretation = (interpretation) => {
  if (!interpretation) return '';
  
  // Try to parse as JSON
  try {
    const parsed = JSON.parse(interpretation);
    // If it's a JSON object with "interpretation" field, extract it
    if (parsed && typeof parsed === 'object' && parsed.interpretation) {
      return parsed.interpretation;
    }
    // If it's a JSON string, return directly
    if (typeof parsed === 'string') {
      return parsed;
    }
  } catch (e) {
    // Not valid JSON, continue with string processing
  }
  
  // If contains embedded JSON, try to extract interpretation using more robust regex
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

const selectLocation = (locationId) => {
  emit('location-selected', locationId);
};

const clearSelection = () => {
  emit('location-selected', null);
};

const handleRefresh = async () => {
  // Reload locations and reports
  await loadLocations();
  if (props.selectedLocationId) {
    await loadReports();
  }
  emit('refresh');
};

const loadLocations = async () => {
  try {
    const userId = localStorage.getItem('user_id') || 1;
    locations.value = await getUserLocations(userId);
    
    // Load report counts for each location
    for (const location of locations.value) {
      try {
        const locationReports = await getLocationReports(location.id);
        locationReportCounts.value[location.id] = locationReports.length;
      } catch (error) {
        locationReportCounts.value[location.id] = 0;
      }
    }
  } catch (error) {
    console.error('Error loading locations:', error);
  }
};

const loadReports = async () => {
  if (!props.selectedLocationId) {
    reports.value = [];
    return;
  }
  
  try {
    reports.value = await getLocationReports(props.selectedLocationId);
    // Sort reports by date (newest first)
    reports.value.sort((a, b) => {
      const dateA = new Date(a.reportDate);
      const dateB = new Date(b.reportDate);
      return dateB - dateA; // Descending order (newest first)
    });
  } catch (error) {
    console.error('Error loading reports:', error);
    reports.value = [];
  }
};

watch(() => props.selectedLocationId, async (newId) => {
  if (newId) {
    try {
      reports.value = await getLocationReports(newId);
      // Sort by date, newest first
      reports.value.sort((a, b) => new Date(b.reportDate) - new Date(a.reportDate));
    } catch (error) {
      console.error('Error loading reports:', error);
      reports.value = [];
    }
  } else {
    reports.value = [];
  }
}, { immediate: true });

onMounted(() => {
  loadLocations();
});
</script>

<style scoped>
.history-panel {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid #e5e7eb;
}

h2 {
  margin: 0;
  color: #2c3e50;
  font-size: 1.8em;
  font-weight: 700;
}

.back-btn {
  padding: 8px 16px;
  background: white;
  color: #667eea;
  border: 2px solid #667eea;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9em;
  font-weight: 600;
  transition: all 0.3s ease;
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

.back-btn:hover {
  background: rgba(102, 126, 234, 0.1);
  border-color: #764ba2;
  color: #764ba2;
}

.locations-selector {
  margin-bottom: 24px;
}

.locations-selector h3 {
  color: #374151;
  font-size: 1.1em;
  margin-bottom: 16px;
  font-weight: 600;
}

.locations-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 16px;
}

.location-selector-card {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  border: 2px solid rgba(102, 126, 234, 0.2);
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.location-selector-card:hover {
  border-color: #667eea;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.location-selector-card h4 {
  margin: 0 0 8px 0;
  color: #2c3e50;
  font-size: 1.1em;
  font-weight: 600;
}

.location-coords {
  color: #6b7280;
  font-size: 0.85em;
  margin: 4px 0;
}

.reports-count {
  margin-top: 8px;
  color: #667eea;
  font-size: 0.9em;
  font-weight: 600;
}

.location-info {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 2px solid #e5e7eb;
}

.location-info h3 {
  margin: 0 0 8px 0;
  color: #2c3e50;
  font-size: 1.3em;
  font-weight: 700;
}

.total-reports {
  margin: 0;
  color: #667eea;
  font-size: 0.95em;
  font-weight: 600;
}

.reports-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.report-card {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  border: 2px solid rgba(102, 126, 234, 0.2);
  border-radius: 12px;
  padding: 20px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.1);
}

.report-card:hover {
  border-color: #667eea;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.2);
  transform: translateY(-2px);
}

.report-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(102, 126, 234, 0.2);
}

.report-date {
  color: #667eea;
  font-size: 0.95em;
  font-weight: 600;
}

.metrics {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}

.metric {
  background: white;
  border: 1px solid rgba(102, 126, 234, 0.2);
  border-radius: 10px;
  padding: 12px;
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.metric-label {
  font-size: 0.75em;
  color: #6b7280;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.metric-value {
  font-size: 1.3em;
  font-weight: 700;
  color: #667eea;
}

.interpretation {
  background: white;
  border-left: 4px solid #667eea;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.interpretation p {
  margin: 0;
  color: #374151;
  line-height: 1.8;
  font-size: 0.95em;
}

.report-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.view-full-report-btn {
  flex: 1;
  padding: 12px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.95em;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.view-full-report-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.delete-report-btn {
  flex: 0 0 auto;
  padding: 12px 20px;
  background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.95em;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(231, 76, 60, 0.3);
}

.delete-report-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(231, 76, 60, 0.4);
  background: linear-gradient(135deg, #c0392b 0%, #a93226 100%);
}

.delete-report-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #6b7280;
}

.empty-state p {
  font-size: 1.1em;
  margin-bottom: 8px;
}

.hint {
  font-size: 0.9em;
  margin-top: 10px;
  font-style: italic;
  color: #9ca3af;
}

.filter-panel {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  border: 2px solid rgba(102, 126, 234, 0.2);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid rgba(102, 126, 234, 0.2);
}

.filter-header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.filter-header h4 {
  margin: 0;
  color: #2c3e50;
  font-size: 1.1em;
  font-weight: 600;
}

.toggle-filters-btn {
  padding: 6px 12px;
  background: white;
  color: #667eea;
  border: 2px solid #667eea;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.85em;
  font-weight: 600;
  transition: all 0.3s ease;
}

.toggle-filters-btn:hover {
  background: rgba(102, 126, 234, 0.1);
  border-color: #764ba2;
  color: #764ba2;
}

.reset-filters-btn {
  padding: 8px 16px;
  background: white;
  color: #667eea;
  border: 2px solid #667eea;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.85em;
  font-weight: 600;
  transition: all 0.3s ease;
}

.reset-filters-btn:hover {
  background: rgba(102, 126, 234, 0.1);
  border-color: #764ba2;
  color: #764ba2;
}

.filters-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.filter-item {
  background: white;
  border: 1px solid rgba(102, 126, 234, 0.2);
  border-radius: 8px;
  padding: 16px;
}

.filter-label {
  display: block;
  margin-bottom: 12px;
  color: #374151;
  font-size: 0.9em;
  font-weight: 600;
}

.range-inputs {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.range-input-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.range-label {
  font-size: 0.8em;
  color: #667eea;
  font-weight: 600;
}

.range-slider {
  width: 100%;
  height: 6px;
  border-radius: 3px;
  background: #e0e0e0;
  outline: none;
  -webkit-appearance: none;
  appearance: none;
}

.range-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #667eea;
  cursor: pointer;
  transition: all 0.2s ease;
}

.range-slider::-webkit-slider-thumb:hover {
  background: #764ba2;
  transform: scale(1.1);
}

.range-slider::-moz-range-thumb {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #667eea;
  cursor: pointer;
  border: none;
  transition: all 0.2s ease;
}

.range-slider::-moz-range-thumb:hover {
  background: #764ba2;
  transform: scale(1.1);
}

.reset-filters-action-btn {
  margin-top: 12px;
  padding: 10px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.95em;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.reset-filters-action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}
</style>

