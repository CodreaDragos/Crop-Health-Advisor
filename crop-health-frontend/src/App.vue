<template>
  <div id="app">
    <div v-if="isAuthenticated">
      <Dashboard @logout="handleLogout" />
    </div>
    <div v-else class="auth-container">
      <AuthPage @auth-success="handleLoginSuccess" />
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import Dashboard from "./components/Dashboard.vue";
import AuthPage from "./components/AuthPage.vue";
import { logout as apiLogout } from "./services/authService";

const isAuthenticated = ref(!!localStorage.getItem("user_token"));

const handleLoginSuccess = (token) => {
  isAuthenticated.value = true;
};

const handleLogout = () => {
  apiLogout();
  isAuthenticated.value = false;
};
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  width: 100%;
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
}

.auth-container {
  width: 100%;
  min-height: 100vh;
}
</style>
