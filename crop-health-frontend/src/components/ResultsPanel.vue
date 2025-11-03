<template>
  <div class="results-panel">
    <h2>📊 Rezultate Analiză</h2>
    
    <div class="metrics-section">
      <h3>Metrici</h3>
      
      <!-- NDVI Indicator -->
      <div class="metric-item">
        <div class="metric-header">
          <span class="metric-name">NDVI (Normalized Difference Vegetation Index)</span>
          <span class="metric-value">{{ formatValue(reportData.ndviValue) }}</span>
        </div>
        <div class="progress-bar-container">
          <div 
            class="progress-bar ndvi-bar" 
            :style="{ width: getNDVIPercentage() + '%' }"
          ></div>
        </div>
        <div class="metric-info">
          <span class="metric-range">Interval: -1.0 (nedelicios) → 1.0 (foarte sănătos)</span>
        </div>
      </div>
      
      <!-- Temperature Indicator -->
      <div class="metric-item">
        <div class="metric-header">
          <span class="metric-name">🌡️ Temperatură Sol (LST)</span>
          <span class="metric-value">{{ formatValue(reportData.temperatureValue) }}°C</span>
        </div>
        <div class="progress-bar-container">
          <div 
            class="progress-bar temp-bar" 
            :style="{ width: getTemperaturePercentage() + '%' }"
          ></div>
        </div>
        <div class="metric-info">
          <span class="metric-range">Temperatura suprafeței solului (LST) - normal: 5°C → 50°C</span>
        </div>
      </div>
      
      <!-- Precipitation Indicator -->
      <div class="metric-item">
        <div class="metric-header">
          <span class="metric-name">🌧️ Precipitații (Estimat)</span>
          <span class="metric-value">{{ formatValue(reportData.precipitationValue) }}mm</span>
        </div>
        <div class="progress-bar-container">
          <div 
            class="progress-bar precip-bar" 
            :style="{ width: getPrecipitationPercentage() + '%' }"
          ></div>
        </div>
        <div class="metric-info">
          <span class="metric-range">Estimat din indici satelitari (NDWI) - 0mm → 100mm</span>
        </div>
      </div>
      
      <!-- EVI Indicator -->
      <div class="metric-item" v-if="reportData.eviValue !== undefined">
        <div class="metric-header">
          <span class="metric-name">EVI (Enhanced Vegetation Index)</span>
          <span class="metric-value">{{ formatValue(reportData.eviValue) }}</span>
        </div>
        <div class="progress-bar-container">
          <div 
            class="progress-bar evi-bar" 
            :style="{ width: getEVIPercentage() + '%' }"
          ></div>
        </div>
        <div class="metric-info">
          <span class="metric-range">Interval: -1.0 → 1.0 (mai robust decât NDVI)</span>
        </div>
      </div>
      
      <!-- NDWI Indicator -->
      <div class="metric-item" v-if="reportData.ndwiValue !== undefined">
        <div class="metric-header">
          <span class="metric-name">NDWI (Normalized Difference Water Index)</span>
          <span class="metric-value">{{ formatValue(reportData.ndwiValue) }}</span>
        </div>
        <div class="progress-bar-container">
          <div 
            class="progress-bar ndwi-bar" 
            :style="{ width: getNDWIPercentage() + '%' }"
          ></div>
        </div>
        <div class="metric-info">
          <span class="metric-range">Interval: -1.0 (uscat) → 1.0 (umed)</span>
        </div>
      </div>
      
      <!-- Soil Moisture Indicator -->
      <div class="metric-item" v-if="reportData.soilMoisture !== undefined">
        <div class="metric-header">
          <span class="metric-name">💧 Umiditate Solului</span>
          <span class="metric-value">{{ formatValue(reportData.soilMoisture) }}%</span>
        </div>
        <div class="progress-bar-container">
          <div 
            class="progress-bar moisture-bar" 
            :style="{ width: Math.min(100, Math.max(0, reportData.soilMoisture || 0)) + '%' }"
          ></div>
        </div>
        <div class="metric-info">
          <span class="metric-range">Interval: 0% (foarte uscat) → 100% (foarte umed)</span>
        </div>
      </div>
      
      <!-- Evapotranspiration Indicator -->
      <div class="metric-item" v-if="reportData.evapotranspiration !== undefined">
        <div class="metric-header">
          <span class="metric-name">💨 Evapotranspirație</span>
          <span class="metric-value">{{ formatValue(reportData.evapotranspiration) }} mm/zi</span>
        </div>
        <div class="progress-bar-container">
          <div 
            class="progress-bar et-bar" 
            :style="{ width: Math.min(100, (reportData.evapotranspiration || 0) * 10) + '%' }"
          ></div>
        </div>
        <div class="metric-info">
          <span class="metric-range">Interval: 0 mm/zi → 10 mm/zi (estimare bazată pe NDVI și LST)</span>
        </div>
      </div>
      
      <!-- Cloud Cover Indicator -->
      <div class="metric-item" v-if="reportData.cloudCover !== undefined">
        <div class="metric-header">
          <span class="metric-name">☁️ Acoperire Nori</span>
          <span class="metric-value">{{ formatValue(reportData.cloudCover) }}%</span>
        </div>
        <div class="progress-bar-container">
          <div 
            class="progress-bar cloud-bar" 
            :style="{ width: Math.min(100, Math.max(0, reportData.cloudCover || 0)) + '%' }"
          ></div>
        </div>
        <div class="metric-info">
          <span class="metric-range">Interval: 0% (senin) → 100% (complet înnorat)</span>
        </div>
      </div>
    </div>
    
    <!-- AI Interpretation -->
    <div v-if="reportData.aiInterpretation" class="ai-section">
      <h3>🤖 Interpretare AI</h3>
      <div class="ai-content">
        <p>{{ cleanAIInterpretation(reportData.aiInterpretation) }}</p>
      </div>
    </div>
    
    <!-- NDVI Map with Satellite Overlay (if location data is available) -->
    <div v-if="reportData.location || (reportData.latitude && reportData.longitude)" class="ndvi-map-section">
      <h3>🗺️ Hartă NDVI cu Suprapunere Satelitară</h3>
      
      <!-- Opacity Control -->
      <div class="map-controls">
        <label class="opacity-control">
          <span>Opacitate NDVI:</span>
          <input 
            type="range" 
            min="0" 
            max="100" 
            v-model.number="ndviOpacity" 
            @input="updateNDVIOpacity"
            class="opacity-slider"
          />
          <span>{{ ndviOpacity }}%</span>
        </label>
        <label class="layer-toggle">
          <input type="checkbox" v-model="showSatelliteBase" @change="updateMapLayers" />
          <span>🛰️ Imagine Satelitară</span>
        </label>
      </div>
      
      <!-- Interactive Leaflet Map -->
      <div class="ndvi-map-container">
        <LMap 
          ref="ndviMap" 
          :zoom="zoom" 
          :center="mapCenter" 
          @ready="onMapReady"
          :options="{ zoomControl: true }"
        >
          <!-- Base Layer: Satellite or OpenStreetMap -->
          <LTileLayer 
            v-if="showSatelliteBase"
            :url="satelliteBaseUrl" 
            :attribution="satelliteAttribution"
            layer-type="base"
            name="Satellite"
          />
          <LTileLayer 
            v-else
            :url="osmUrl" 
            :attribution="osmAttribution"
            layer-type="base"
            name="OpenStreetMap"
          />
          
          <!-- NDVI Overlay Layer -->
          <LImageOverlay
            v-if="ndviImageUrl"
            :url="ndviImageUrl"
            :bounds="ndviBounds"
            :opacity="ndviOpacity / 100"
            layer-type="overlay"
            name="NDVI"
          />
          
          <!-- Location Marker -->
          <LMarker :lat-lng="mapCenter">
            <LPopup>
              <strong>📍 Locație Analizată</strong><br/>
              {{ reportData.location?.name || 'Locație fără nume' }}
            </LPopup>
          </LMarker>
        </LMap>
      </div>
      
      <p class="map-note">
        💡 Ajustează opacitatea pentru a vedea imaginea satelitară și distribuția vegetației (NDVI).
        Verde = vegetație sănătoasă, Galben = moderată, Portocaliu = slabă, Roșu = sol gol/apă.
      </p>
    </div>
    
    <!-- Report Date -->
    <div class="report-meta">
      <span>📅 Generat la: {{ formatDate(reportData.reportDate) }}</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue';
import { LMap, LTileLayer, LImageOverlay, LMarker, LPopup } from "@vue-leaflet/vue-leaflet";
import "leaflet/dist/leaflet.css";
import { getNDVIImageUrl, getNDVIImageBlob } from '../services/locationService';
import { getAuthHeader } from '../services/authService';

const props = defineProps({
  reportData: {
    type: Object,
    required: true
  }
});

const ndviImageUrl = ref(null);
const imageLoadError = ref(false);
const ndviMap = ref(null);
const ndviOpacity = ref(70); // Default opacity 70%
const showSatelliteBase = ref(true); // Default to satellite view
const zoom = ref(14);
const mapCenter = ref([46.0, 23.0]); // Default center, will be updated

// Map tile URLs
const osmUrl = 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';
const osmAttribution = '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors';
const satelliteBaseUrl = 'https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}';
const satelliteAttribution = '&copy; <a href="https://www.esri.com/">Esri</a>';

// NDVI image bounds - will be calculated based on location and image size
const ndviBounds = ref([[0, 0], [0, 0]]);

// Calculate NDVI bounds based on location and image size
const calculateNDVIBounds = (lat, lon, imageWidth = 512, imageHeight = 512) => {
  // Approximate bounds - assuming image covers about 5km x 5km area
  const latDelta = 0.0225; // ~2.5km north/south
  const lonDelta = 0.035; // ~2.5km east/west (adjusts for latitude)
  
  return [
    [lat - latDelta, lon - lonDelta], // Southwest corner
    [lat + latDelta, lon + lonDelta]   // Northeast corner
  ];
};

// Update NDVI opacity
const updateNDVIOpacity = () => {
  // Opacity is controlled by v-model binding
};

// Update map layers
const updateMapLayers = () => {
  // Layer update is controlled by v-model binding
};

// Map ready handler
const onMapReady = () => {
  console.log('NDVI Map ready');
};

// Încarcă imaginea NDVI dacă există date de locație
onMounted(async () => {
  // Verifică dacă există date de locație în raport
  const location = props.reportData.location;
  const lat = location?.latitude || props.reportData.latitude;
  const lon = location?.longitude || props.reportData.longitude;
  
  console.log('ResultsPanel mounted - reportData:', props.reportData);
  console.log('Location object:', location);
  console.log('Lat/Lon:', lat, lon);
  
  if (lat && lon) {
    // Set map center to location
    mapCenter.value = [lat, lon];
    
    // Calculate NDVI bounds
    ndviBounds.value = calculateNDVIBounds(lat, lon);
    
    try {
      console.log('Loading NDVI image for location:', lat, lon);
      
      // Pentru a include header-ul de autentificare, folosim blob
      const blobUrl = await getNDVIImageBlob(lat, lon, 512, 512);
      ndviImageUrl.value = blobUrl;
      console.log('NDVI image loaded successfully');
      
      // Wait for map to initialize
      await nextTick();
      if (ndviMap.value) {
        // Fit map to NDVI bounds
        const map = ndviMap.value.leafletObject;
        if (map) {
          map.fitBounds(ndviBounds.value);
        }
      }
    } catch (error) {
      console.warn('Could not load NDVI image:', error);
      imageLoadError.value = true;
    }
  } else {
    console.warn('No location data available for NDVI image. Report data:', props.reportData);
  }
});

const handleImageError = () => {
  imageLoadError.value = true;
  console.warn('NDVI image failed to load');
};

const formatValue = (value) => {
  if (value === null || value === undefined) return 'N/A';
  return typeof value === 'number' ? value.toFixed(2) : value;
};

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

// NDVI ranges from -1 to 1, normalize to 0-100%
const getNDVIPercentage = () => {
  const ndvi = props.reportData?.ndviValue;
  if (ndvi === null || ndvi === undefined) return 0;
  // Normalize -1 to 1 range to 0-100%
  return ((ndvi + 1) / 2) * 100;
};

// Temperature (LST): normalize for soil temperature range 5°C-50°C
const getTemperaturePercentage = () => {
  const temp = props.reportData?.temperatureValue;
  if (temp === null || temp === undefined) return 0;
  // Normalize assuming range 5°C to 50°C (LST range)
  const normalized = ((temp - 5) / 45) * 100;
  return Math.max(0, Math.min(100, normalized));
};

// Precipitation: 0-100mm range
const getPrecipitationPercentage = () => {
  const precip = props.reportData?.precipitationValue;
  if (precip === null || precip === undefined) return 0;
  // Normalize 0-100mm to 0-100%
  return Math.min(100, (precip / 100) * 100);
};

// NDWI: ranges from -1 to 1, normalize to 0-100%
const getNDWIPercentage = () => {
  const ndwi = props.reportData?.ndwiValue;
  if (ndwi === null || ndwi === undefined) return 0;
  // Normalize -1 to 1 range to 0-100%
  return ((ndwi + 1) / 2) * 100;
};

// EVI: ranges from -1 to 1, normalize to 0-100%
const getEVIPercentage = () => {
  const evi = props.reportData?.eviValue;
  if (evi === null || evi === undefined) return 0;
  // Normalize -1 to 1 range to 0-100%
  return ((evi + 1) / 2) * 100;
};

// Curăță textul AI de metadata JSON
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
</script>

<style scoped>
.results-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

h2 {
  margin: 0 0 20px 0;
  color: #2c3e50;
  padding-bottom: 15px;
  border-bottom: 2px solid #e0e0e0;
}

h3 {
  margin: 0 0 15px 0;
  color: #34495e;
  font-size: 1.2em;
}

.metrics-section {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
}

.metric-item {
  margin-bottom: 25px;
}

.metric-item:last-child {
  margin-bottom: 0;
}

.metric-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.metric-name {
  font-weight: 600;
  color: #2c3e50;
}

.metric-value {
  font-weight: bold;
  font-size: 1.1em;
  color: #3498db;
}

.progress-bar-container {
  width: 100%;
  height: 25px;
  background: #e0e0e0;
  border-radius: 12px;
  overflow: hidden;
  position: relative;
}

.progress-bar {
  height: 100%;
  border-radius: 12px;
  transition: width 0.5s ease;
  position: relative;
}

.ndvi-bar {
  background: linear-gradient(90deg, #e74c3c 0%, #f39c12 50%, #2ecc71 100%);
}

/* NDVI Map Section Styles */
.ndvi-map-section {
  margin-top: 30px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
}

.ndvi-map-section h3 {
  margin: 0 0 15px 0;
  color: #2c3e50;
  font-size: 1.2em;
}

.map-controls {
  display: flex;
  gap: 20px;
  margin-bottom: 15px;
  padding: 10px;
  background: white;
  border-radius: 6px;
  border: 1px solid #ddd;
  flex-wrap: wrap;
  align-items: center;
}

.opacity-control {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 0.9em;
}

.opacity-slider {
  width: 150px;
  height: 6px;
  -webkit-appearance: none;
  appearance: none;
  background: #ddd;
  border-radius: 3px;
  outline: none;
}

.opacity-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 18px;
  height: 18px;
  background: #3498db;
  border-radius: 50%;
  cursor: pointer;
}

.opacity-slider::-moz-range-thumb {
  width: 18px;
  height: 18px;
  background: #3498db;
  border-radius: 50%;
  cursor: pointer;
  border: none;
}

.layer-toggle {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.9em;
  cursor: pointer;
}

.layer-toggle input[type="checkbox"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
}

.ndvi-map-container {
  width: 100%;
  height: 500px;
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid #ddd;
  background: #e0e0e0;
}

.map-note {
  margin-top: 10px;
  font-size: 0.85em;
  color: #7f8c8d;
  font-style: italic;
}

.temp-bar {
  background: linear-gradient(90deg, #3498db 0%, #f39c12 50%, #e74c3c 100%);
}

.precip-bar {
  background: linear-gradient(90deg, #ecf0f1 0%, #3498db 50%, #2980b9 100%);
}

.additional-metrics-section {
  margin-top: 25px;
  padding-top: 20px;
  border-top: 2px solid #e0e0e0;
}

.additional-metrics-section h4 {
  margin: 0 0 15px 0;
  color: #34495e;
  font-size: 1.1em;
  font-weight: 600;
}

.additional-metric-item {
  margin-bottom: 15px;
}

.metric-header-small {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.metric-name-small {
  font-weight: 600;
  color: #2c3e50;
  font-size: 0.9em;
}

.metric-value-small {
  font-weight: bold;
  font-size: 1em;
  color: #3498db;
}

.progress-bar-container-small {
  width: 100%;
  height: 18px;
  background: #e0e0e0;
  border-radius: 9px;
  overflow: hidden;
  position: relative;
}

.progress-bar-container-small .progress-bar {
  height: 100%;
  border-radius: 9px;
  transition: width 0.5s ease;
}

.moisture-bar {
  background: linear-gradient(90deg, #e74c3c 0%, #f39c12 50%, #2ecc71 100%);
}

.et-bar {
  background: linear-gradient(90deg, #3498db 0%, #2980b9 100%);
}

.cloud-bar {
  background: linear-gradient(90deg, #ecf0f1 0%, #95a5a6 50%, #7f8c8d 100%);
}

.ndwi-bar {
  background: linear-gradient(90deg, #e74c3c 0%, #3498db 50%, #2ecc71 100%);
}

.evi-bar {
  background: linear-gradient(90deg, #9b59b6 0%, #8e44ad 50%, #3498db 100%);
}

.metric-info {
  margin-top: 5px;
  font-size: 0.85em;
  color: #7f8c8d;
}

.ai-section {
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
  padding: 20px;
  border-radius: 8px;
  border-left: 4px solid #4caf50;
}

.ai-content {
  background: white;
  padding: 15px;
  border-radius: 5px;
  line-height: 1.8;
  color: #2e7d32;
}

.ndvi-map-section {
  margin-top: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
}

.ndvi-map-section h3 {
  margin: 0 0 15px 0;
  color: #34495e;
  font-size: 1.2em;
}

.ndvi-image-container {
  width: 100%;
  text-align: center;
  margin-bottom: 10px;
}

.ndvi-image {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  border: 2px solid #ddd;
}

.image-loading {
  padding: 40px;
  color: #7f8c8d;
  font-style: italic;
}

.map-note {
  margin: 10px 0 0 0;
  font-size: 0.85em;
  color: #7f8c8d;
  font-style: italic;
  text-align: center;
}

.report-meta {
  text-align: center;
  padding: 10px;
  color: #7f8c8d;
  font-size: 0.9em;
  border-top: 1px solid #e0e0e0;
  margin-top: 10px;
}
</style>

