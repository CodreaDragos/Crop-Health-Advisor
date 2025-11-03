<template>
  <nav class="sidebar">
    <div class="sidebar-header">
      <h2>🌾 Crop Health</h2>
    </div>
    
    <ul class="nav-menu">
      <li>
        <button 
          @click="$emit('nav-click', 'dashboard')" 
          :class="{ active: activeView === 'dashboard' }"
          class="nav-item"
        >
          🗺️ Harta
        </button>
      </li>
      <li>
        <button 
          @click="$emit('nav-click', 'locations')" 
          :class="{ active: activeView === 'locations' }"
          class="nav-item"
        >
          📍 Locații Salvate
        </button>
      </li>
      <li>
        <button 
          @click="$emit('nav-click', 'history')" 
          :class="{ active: activeView === 'history' }"
          class="nav-item"
        >
          📊 Istoric
        </button>
      </li>
      <li>
        <button 
          @click="$emit('nav-click', 'profile')" 
          :class="{ active: activeView === 'profile' }"
          class="nav-item"
        >
          👤 Profil
        </button>
      </li>
    </ul>
    
    <div class="sidebar-footer">
      <button @click="$emit('logout')" class="logout-btn">
        🚪 Logout
      </button>
    </div>
  </nav>
</template>

<script setup>
defineProps({
  activeView: {
    type: String,
    default: 'dashboard'
  }
});

defineEmits(['nav-click', 'logout']);
</script>

<style scoped>
.sidebar {
  width: 250px;
  height: 100vh;
  background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  flex-direction: column;
  position: fixed;
  left: 0;
  top: 0;
  box-shadow: 2px 0 10px rgba(0,0,0,0.1);
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}

.sidebar-header h2 {
  margin: 0;
  font-size: 1.5em;
}

.nav-menu {
  list-style: none;
  padding: 20px 0;
  margin: 0;
  flex: 1;
}

.nav-menu li {
  margin: 0;
}

.nav-item {
  width: 100%;
  padding: 15px 20px;
  background: transparent;
  border: none;
  color: white;
  text-align: left;
  cursor: pointer;
  font-size: 1em;
  transition: all 0.3s ease;
  border-left: 3px solid transparent;
}

.nav-item:hover {
  background: rgba(255,255,255,0.1);
  border-left-color: #3498db;
}

.nav-item.active {
  background: rgba(255,255,255,0.2);
  border-left-color: #ffffff;
  font-weight: bold;
}

.sidebar-footer {
  padding: 20px;
  border-top: 1px solid rgba(255,255,255,0.1);
}

.logout-btn {
  width: 100%;
  padding: 12px;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 8px;
  cursor: pointer;
  font-size: 1em;
  transition: all 0.3s ease;
  font-weight: 500;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.5);
}
</style>

