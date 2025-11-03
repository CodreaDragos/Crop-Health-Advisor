<template>
  <div class="history-panel">
    <div class="history-header">
      <h2>📊 Istoric Rapoarte</h2>
      <button v-if="selectedLocationId" @click="clearSelection" class="back-btn">
        ← Înapoi la Toate Locațiile
      </button>
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
        <p class="total-reports">{{ reports.length }} raport(e) total(e)</p>
      </div>
      
      <div 
        v-for="report in reports" 
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

const emit = defineEmits(['location-selected', 'view-full-report', 'report-deleted']);

const deletingReportId = ref(null);

const handleDeleteReport = async (report) => {
  if (!confirm(`Ești sigur că vrei să ștergi raportul din ${formatDate(report.reportDate)}?`)) {
    return;
  }
  
  deletingReportId.value = report.id;
  
  try {
    await deleteReport(report.id);
    emit('report-deleted', report.id);
    // Reîncarcă rapoartele pentru locația curentă
    await loadReports();
    alert('Raportul a fost șters cu succes.');
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
  
  // Încearcă să parseze ca JSON
  try {
    const parsed = JSON.parse(interpretation);
    // Dacă e un obiect JSON cu câmpul "interpretation", extrage-l
    if (parsed && typeof parsed === 'object' && parsed.interpretation) {
      return parsed.interpretation;
    }
    // Dacă e un string JSON, returnează direct
    if (typeof parsed === 'string') {
      return parsed;
    }
  } catch (e) {
    // Nu e JSON valid, continuă cu procesarea string-ului
  }
  
  // Dacă conține JSON embedded, încearcă să extragă interpretarea folosind regex mai robust
  // Caută pattern: "interpretation": "..." sau "interpretation":"..."
  const jsonMatch = interpretation.match(/"interpretation"\s*:\s*"((?:[^"\\]|\\.)*)"/);
  if (jsonMatch && jsonMatch[1]) {
    return jsonMatch[1]
      .replace(/\\"/g, '"')
      .replace(/\\n/g, '\n')
      .replace(/\\t/g, '\t')
      .replace(/\\\\/g, '\\');
  }
  
  // Elimină prefixe/sufixe comune din mock responses
  let cleaned = interpretation
    .replace(/^.*?"interpretation"\s*:\s*"/, '') // Elimină până la "interpretation": "
    .replace(/"\s*,\s*"timestamp".*$/, '') // Elimină ", "timestamp": ... la sfârșit
    .replace(/^.*?mockAI.*?interpretation.*?:/, '') // Elimină mockAI și interpretation:
    .replace(/,\s*"timestamp".*$/, '') // Elimină timestamp la sfârșit
    .replace(/^[\s"{]*/, '') // Elimină spații, {, " la început
    .replace(/[\s"}]*$/, '') // Elimină spații, }, " la sfârșit
    .replace(/^Analiza\s+pentru\s+prompt\s*:\s*/i, '') // Elimină "Analiza pentru prompt:"
    .replace(/^Analiza\s+pentru\s+prompt\s*/i, '') // Elimină variante fără ":"
    .trim();
  
  return cleaned || interpretation; // Dacă totul e gol, returnează originalul
};

const selectLocation = (locationId) => {
  emit('location-selected', locationId);
};

const clearSelection = () => {
  emit('location-selected', null);
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
    // Sortează rapoartele după dată (cele mai noi primul)
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
</style>

