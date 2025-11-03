<template>
  <div class="auth-page">
    <div class="auth-wrapper">
      <div class="auth-card">
        <div class="auth-header">
          <h1 class="auth-title">🌾 Crop Health Advisor</h1>
          <h2 class="auth-subtitle">{{ isLogin ? 'Bun venit înapoi!' : 'Creează cont nou' }}</h2>
        </div>

        <form @submit.prevent="handleSubmit" class="auth-form">
          <div v-if="!isLogin" class="form-group">
            <label for="username">Nume utilizator</label>
            <input 
              id="username"
              type="text" 
              v-model="username" 
              placeholder="Introduceți numele de utilizator"
              required 
              class="form-input"
            />
          </div>

          <div class="form-group">
            <label for="email">Email</label>
            <input 
              id="email"
              type="email" 
              v-model="email" 
              placeholder="exemplu@email.com"
              required 
              class="form-input"
            />
          </div>

          <div class="form-group">
            <label for="password">Parolă</label>
            <input 
              id="password"
              type="password" 
              v-model="password" 
              placeholder="••••••••"
              required 
              class="form-input"
            />
          </div>

          <button 
            type="submit" 
            :disabled="isLoading" 
            class="submit-btn"
          >
            <span v-if="isLoading" class="spinner-small"></span>
            <span>{{ isLogin ? 'Autentificare' : 'Înregistrare' }}</span>
          </button>

          <div v-if="message" :class="['message', isError ? 'message-error' : 'message-success']">
            {{ message }}
          </div>
        </form>

        <div class="auth-footer">
          <button @click="isLogin = !isLogin" class="toggle-btn">
            {{ isLogin ? 'Nu ai cont? ' : 'Ai deja cont? ' }}
            <span class="toggle-link">{{ isLogin ? 'Creează unul acum' : 'Conectează-te' }}</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { login, register } from '../services/authService';

const emit = defineEmits(['auth-success']);

const isLogin = ref(true);
const email = ref('');
const password = ref('');
const username = ref('');
const message = ref('');
const isError = ref(false);
const isLoading = ref(false);

const handleSubmit = async () => {
    isLoading.value = true;
    message.value = '';

    try {
        if (isLogin.value) {
            const response = await login(email.value, password.value);
            message.value = 'Login reușit! Redirecționare...';
            isError.value = false;
            
            localStorage.setItem('user_email', email.value);
            emit('auth-success', response.accessToken); 
            
        } else {
            const response = await register(username.value, email.value, password.value);
            message.value = response || 'Înregistrare reușită. Vă puteți loga acum.';
            isError.value = false;
            isLogin.value = true;
        }
    } catch (err) {
        isError.value = true;
        // Gestionează atât Error objects cât și string-uri
        message.value = err instanceof Error ? err.message : (err || 'A apărut o eroare necunoscută.');
        console.error('Auth error:', err);
    } finally {
        isLoading.value = false;
    }
};
</script>

<style scoped>
.auth-page {
  width: 100%;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
  overflow: hidden;
}

.auth-page::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 50%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 80%, rgba(255, 255, 255, 0.1) 0%, transparent 50%);
  pointer-events: none;
}

.auth-wrapper {
  width: 100%;
  max-width: 450px;
  position: relative;
  z-index: 1;
}

.auth-card {
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(10px);
  border-radius: 24px;
  box-shadow: 
    0 20px 60px rgba(0, 0, 0, 0.3),
    0 0 0 1px rgba(255, 255, 255, 0.1) inset;
  padding: 48px 40px;
  width: 100%;
  animation: slideUp 0.5s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.auth-header {
  text-align: center;
  margin-bottom: 36px;
}

.auth-title {
  font-size: 2.5em;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 8px;
  letter-spacing: -0.5px;
}

.auth-subtitle {
  font-size: 1.2em;
  color: #6b7280;
  font-weight: 500;
  margin: 0;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-size: 0.9em;
  font-weight: 600;
  color: #374151;
  letter-spacing: 0.3px;
}

.form-input {
  width: 100%;
  padding: 14px 16px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  font-size: 1em;
  transition: all 0.3s ease;
  background: #ffffff;
  color: #111827;
  font-family: inherit;
}

.form-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

.form-input::placeholder {
  color: #9ca3af;
}

.submit-btn {
  width: 100%;
  padding: 14px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 1.05em;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-top: 8px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.submit-btn:active:not(:disabled) {
  transform: translateY(0);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.spinner-small {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.message {
  padding: 12px 16px;
  border-radius: 10px;
  font-size: 0.9em;
  text-align: center;
  font-weight: 500;
}

.message-error {
  background: #fee2e2;
  color: #dc2626;
  border: 1px solid #fecaca;
}

.message-success {
  background: #d1fae5;
  color: #059669;
  border: 1px solid #a7f3d0;
}

.auth-footer {
  margin-top: 28px;
  text-align: center;
  padding-top: 24px;
  border-top: 1px solid #e5e7eb;
}

.toggle-btn {
  background: none;
  border: none;
  color: #6b7280;
  font-size: 0.95em;
  cursor: pointer;
  padding: 8px;
  transition: color 0.2s ease;
  font-family: inherit;
}

.toggle-btn:hover {
  color: #667eea;
}

.toggle-link {
  color: #667eea;
  font-weight: 600;
  text-decoration: underline;
  text-underline-offset: 3px;
}

.toggle-link:hover {
  color: #764ba2;
}

@media (max-width: 480px) {
  .auth-card {
    padding: 36px 28px;
    border-radius: 20px;
  }

  .auth-title {
    font-size: 2em;
  }

  .auth-subtitle {
    font-size: 1.05em;
  }
}
</style>
