<template>
  <div class="map-container">
    <div class="map-controls">
      <label class="layer-toggle">
        <input type="checkbox" v-model="useSatellite" @change="updateLayer" />
        <span>🌍 Mod Satelit</span>
      </label>
      
      <!-- Save Location Button - Apare doar când e un pin plasat -->
      <div v-if="marker" class="save-location-container">
        <button @click="$emit('save-location-clicked')" class="save-location-btn">
          💾 Salvează Locația
        </button>
      </div>
    </div>
    <div class="map-wrapper">
      <LMap ref="map" :zoom="zoom" :center="center" @ready="onMapReady" :options="{ zoomControl: true }">
        <!-- Base Layer: OpenStreetMap or Satellite -->
        <LTileLayer 
          v-if="!useSatellite"
          :url="osmUrl" 
          :attribution="osmAttribution" 
        />
        
        <!-- Satellite Layer using Esri World Imagery -->
        <LTileLayer 
          v-if="useSatellite"
          :url="satelliteUrl" 
          :attribution="satelliteAttribution"
        />

        <!-- Click Marker -->
        <LMarker v-if="marker" :lat-lng="marker" />

        <!-- Saved Locations Markers -->
        <LMarker 
          v-for="location in savedLocations" 
          :key="location.id"
          :lat-lng="[location.latitude, location.longitude]"
        >
          <LPopup>
            <strong>{{ location.name }}</strong><br/>
            📍 {{ location.latitude.toFixed(4) }}, {{ location.longitude.toFixed(4) }}
          </LPopup>
        </LMarker>
      </LMap>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch, onActivated } from "vue";
import { LMap, LTileLayer, LMarker, LPopup } from "@vue-leaflet/vue-leaflet";
import "leaflet/dist/leaflet.css";

// IMPORTEAZĂ IMAGINILE NECESARE
import { Icon } from "leaflet";

// Reimportă căile implicite ale imaginilor Leaflet
import markerIconUrl from "leaflet/dist/images/marker-icon.png";
import markerIconRetinaUrl from "leaflet/dist/images/marker-icon-2x.png";
import markerShadowUrl from "leaflet/dist/images/marker-shadow.png";

// CORECTEAZĂ CĂILE IMPLICITE ALE LEAFLET
delete Icon.Default.prototype._getIconUrl;
Icon.Default.mergeOptions({
  iconRetinaUrl: markerIconRetinaUrl,
  iconUrl: markerIconUrl,
  shadowUrl: markerShadowUrl,
});

const props = defineProps({
  savedLocations: {
    type: Array,
    default: () => []
  }
});

// Definim variabilele reactive
const zoom = ref(10);
const center = ref([46.77, 23.62]);
const useSatellite = ref(false);
const map = ref(null);

// OpenStreetMap layer
const osmUrl = ref("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png");
const osmAttribution = ref(
  '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
);

// Satellite layer using Esri World Imagery (free alternative)
const satelliteUrl = ref("https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}");
const satelliteAttribution = ref(
  '&copy; <a href="https://www.esri.com/">Esri</a> &copy; <a href="https://www.maptiler.com/">MapTiler</a>'
);

const marker = ref(null);

// Definim evenimentul personalizat (Emits)
const emit = defineEmits(["location-selected", "save-location-clicked"]);

const updateLayer = () => {
  // Layer update is handled by v-if on LTileLayer components
};

// Metoda apelată la click pe hartă
const handleMapClick = (event) => {
  // Primeste evenimentul Leaflet
  if (event && event.latlng) {
    const { lat, lng } = event.latlng;
    marker.value = [lat, lng];
    emit("location-selected", { lat, lon: lng });
  }
};

// Funcție pentru a atașa event listener-ul pe hartă
// Folosim leafletMapInstance direct în loc de map.value.leafletObject
const attachClickListener = () => {
  // Încearcă să folosească instanța Leaflet directă dacă este disponibilă
  const leafletMap = leafletMapInstance.value || (map.value && map.value.leafletObject);
  
  if (!leafletMap) {
    console.log('Cannot attach click listener: leaflet map instance not available');
    return false;
  }
  
  // Verifică dacă harta este complet inițializată (are metode necesare)
  if (!leafletMap.on || typeof leafletMap.on !== 'function') {
    console.log('Cannot attach click listener: map not fully initialized');
    return false;
  }
  
  // Verifică dacă listener-ul există deja
  if (leafletMap.listens && typeof leafletMap.listens === 'function' && leafletMap.listens('click')) {
    console.log('Click listener already attached');
    return true;
  }
  
  // Elimină orice event listener existent pentru a evita duplicarea
  if (leafletMap.off && typeof leafletMap.off === 'function') {
    leafletMap.off('click');
  }
  
  // Adaugă noul event listener
  try {
    leafletMap.on('click', (e) => {
      const { lat, lng } = e.latlng;
      marker.value = [lat, lng];
      emit("location-selected", { lat, lon: lng });
      console.log('Map clicked at:', lat, lng); // Debug log
    });
    
    console.log('Map click listener attached successfully'); // Debug log
    return true;
  } catch (error) {
    console.error('Error attaching click listener:', error);
    return false;
  }
};

// Expune metoda pentru a putea fi apelată din exterior
defineExpose({
  reattachClickListener: attachClickListener,
  map: map
});

// Flag pentru a știi când harta este complet inițializată
const mapReady = ref(false);
const leafletMapInstance = ref(null); // Stochează instanța Leaflet directă

// Callback când harta e gata
const onMapReady = (mapObject) => {
  console.log('onMapReady called', mapObject);
  map.value = mapObject;
  
  // În Vue Leaflet, mapObject este wrapper-ul, iar mapObject.leafletObject este instanța Leaflet reală
  // Dar uneori onMapReady poate primi direct instanța Leaflet
  if (mapObject && mapObject.leafletObject) {
    leafletMapInstance.value = mapObject.leafletObject;
    mapReady.value = true;
    
    // Atașează event listener-ul direct pe instanța Leaflet
    setTimeout(() => {
      if (leafletMapInstance.value) {
        // Elimină orice listener existent
        leafletMapInstance.value.off('click');
        
        // Adaugă noul listener
        leafletMapInstance.value.on('click', (e) => {
          const { lat, lng } = e.latlng;
          marker.value = [lat, lng];
          emit("location-selected", { lat, lon: lng });
          console.log('Map clicked at:', lat, lng);
        });
        
        // Reinițializează dimensiunile
        if (leafletMapInstance.value.invalidateSize) {
          leafletMapInstance.value.invalidateSize();
        }
        
        console.log('Click listener attached successfully in onMapReady (direct Leaflet instance)');
      }
    }, 200);
  } else if (mapObject && mapObject.on) {
    // Dacă mapObject este direct instanța Leaflet (fără wrapper)
    leafletMapInstance.value = mapObject;
    mapReady.value = true;
    
    setTimeout(() => {
      if (leafletMapInstance.value) {
        leafletMapInstance.value.off('click');
        leafletMapInstance.value.on('click', (e) => {
          const { lat, lng } = e.latlng;
          marker.value = [lat, lng];
          emit("location-selected", { lat, lon: lng });
          console.log('Map clicked at:', lat, lng);
        });
        
        if (leafletMapInstance.value.invalidateSize) {
          leafletMapInstance.value.invalidateSize();
        }
        
        console.log('Click listener attached successfully in onMapReady (Leaflet instance directly)');
      }
    }, 200);
  } else {
    console.warn('onMapReady received unexpected object:', mapObject);
  }
};

// Initializează harta după ce componenta e montată
onMounted(async () => {
  await nextTick();
  
  // Nu mai încercăm să atașăm listener-ul aici - onMapReady se ocupă de asta
  // Acest fallback poate cauza probleme, deci îl eliminăm
});

// Când componenta devine activă din nou (dacă folosim keep-alive)
onActivated(() => {
  console.log('MapComponent activated - reattaching click listener');
  nextTick(() => {
    setTimeout(() => {
      // Folosește leafletMapInstance direct
      if (leafletMapInstance.value) {
        if (leafletMapInstance.value.invalidateSize) {
          leafletMapInstance.value.invalidateSize();
        }
        attachClickListener();
      }
    }, 200);
  });
});

// Watch pentru a reinițializa listener-ul când componenta devine vizibilă
watch(() => props.savedLocations, () => {
  // Când se actualizează locațiile salvate, asigură-te că listener-ul există
  nextTick(() => {
    setTimeout(() => {
      if (!attachClickListener()) {
        // Retry dacă nu a reușit prima dată
        setTimeout(() => attachClickListener(), 300);
      }
    }, 100);
  });
}, { deep: true });
</script>

<style scoped>
.map-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.map-controls {
  padding: 10px;
  background: white;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 15px;
}

.save-location-container {
  display: flex;
  align-items: center;
}

.save-location-btn {
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

.save-location-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.layer-toggle {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 0.95em;
  color: #2c3e50;
}

.layer-toggle input[type="checkbox"] {
  cursor: pointer;
}

.map-wrapper {
  flex: 1;
  min-height: 600px;
  height: 100%;
  width: 100%;
  position: relative;
  z-index: 0;
}

/* Asigură-te că Leaflet se afișează corect */
.map-wrapper :deep(.leaflet-container) {
  height: 100%;
  width: 100%;
  z-index: 0;
  cursor: pointer; /* Indicează că harta e clickabilă */
}

/* Asigură-te că interacțiunile Leaflet funcționează */
.map-wrapper :deep(.leaflet-interactive) {
  cursor: pointer;
}

.map-wrapper :deep(.leaflet-clickable) {
  cursor: pointer;
}
</style>
