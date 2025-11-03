<template>
  <div class="profile-panel">
    <h2>Profil Utilizator</h2>
    
    <div class="profile-content">
      <div class="profile-info">
        <div class="info-item">
          <label>Email:</label>
          <span>{{ userEmail || 'N/A' }}</span>
        </div>
        <div class="info-item">
          <label>User ID:</label>
          <span>{{ userId || 'N/A' }}</span>
        </div>
        <div class="info-item">
          <label>Status:</label>
          <span class="status-active">● Activ</span>
        </div>
      </div>
      
      <div class="profile-stats">
        <div class="stat-card">
          <div class="stat-value">{{ totalLocations }}</div>
          <div class="stat-label">Locații Salvate</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ totalReports }}</div>
          <div class="stat-label">Rapoarte Generate</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { getUserLocations } from '../services/locationService';
import { getLocationReports } from '../services/locationService';

const userEmail = ref(localStorage.getItem('user_email') || null);
const userId = ref(localStorage.getItem('user_id') || null);
const totalLocations = ref(0);
const totalReports = ref(0);

onMounted(async () => {
  try {
    const id = userId.value || 1;
    const locations = await getUserLocations(id);
    totalLocations.value = locations.length;
    
    // Count total reports
    let total = 0;
    for (const location of locations) {
      if (location.id) {
        try {
          const reports = await getLocationReports(location.id);
          total += reports.length;
        } catch (e) {
          // Ignore errors for individual locations
        }
      }
    }
    totalReports.value = total;
  } catch (error) {
    console.error('Error loading profile stats:', error);
  }
});
</script>

<style scoped>
.profile-panel {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

h2 {
  margin: 0 0 20px 0;
  color: #2c3e50;
  padding-bottom: 15px;
  border-bottom: 2px solid #e0e0e0;
}

.profile-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
}

.profile-info {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 5px;
}

.info-item label {
  font-weight: bold;
  color: #7f8c8d;
}

.info-item span {
  color: #2c3e50;
}

.status-active {
  color: #2ecc71 !important;
  font-weight: bold;
}

.profile-stats {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.stat-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 25px;
  border-radius: 8px;
  text-align: center;
}

.stat-value {
  font-size: 2.5em;
  font-weight: bold;
  margin-bottom: 10px;
}

.stat-label {
  font-size: 1em;
  opacity: 0.9;
}
</style>

