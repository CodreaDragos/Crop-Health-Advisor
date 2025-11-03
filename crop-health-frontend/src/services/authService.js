// src/services/authService.js
import axios from 'axios';

const API_URL = 'http://localhost:8081/api/auth';

// 1. Apel de login
export const login = async (email, password) => {
    try {
        const response = await axios.post(`${API_URL}/login`, { email, password });
        
        // Daca login-ul reuseste, response.data contine { accessToken: "...", tokenType: "Bearer" }
        if (response.data.accessToken) {
            // Stocheaza token-ul in localStorage
            localStorage.setItem('user_token', response.data.accessToken);
            
            // Get user info to store user ID
            try {
                const userResponse = await axios.get(`http://localhost:8081/api/users/by-email`, {
                    params: { email },
                    headers: { Authorization: 'Bearer ' + response.data.accessToken }
                });
                if (userResponse.data && userResponse.data.id) {
                    localStorage.setItem('user_id', userResponse.data.id);
                }
            } catch (userError) {
                console.warn('Could not fetch user info:', userError);
            }
        }
        return response.data;

    } catch (error) {
        // Gestionează erori diferite: CORS, network, sau erori de la backend
        if (error.response) {
            // Eroare de la backend (ex: 401, 400, 500)
            const errorMessage = error.response.data || error.response.statusText || 'Eroare la autentificare';
            throw new Error(typeof errorMessage === 'string' ? errorMessage : JSON.stringify(errorMessage));
        } else if (error.request) {
            // Request-ul a fost făcut dar nu s-a primit răspuns (CORS, network error)
            throw new Error('Nu s-a putut conecta la server. Verifică dacă backend-ul rulează și configurația CORS.');
        } else {
            // Altă eroare
            throw new Error(error.message || 'Eroare necunoscută la autentificare');
        }
    }
};

// 2. Apel de inregistrare
export const register = async (username, email, password) => {
    // Backend-ul asteapta obiectul User complet, inclusiv username
    const response = await axios.post(`${API_URL}/register`, { 
        username, email, password 
    });
    
    // Save user ID if available in response
    if (response.data && response.data.id) {
        localStorage.setItem('user_id', response.data.id);
        localStorage.setItem('user_email', email);
    }
    
    return response.data;
};

// 3. Delogare
export const logout = () => {
    localStorage.removeItem('user_token');
    localStorage.removeItem('user_email'); // Optional, dar util
};

// 4. Obtine Header-ul de Autorizare
export const getAuthHeader = () => {
    const token = localStorage.getItem('user_token');
    if (token) {
        // Returneaza formatul necesar: { Authorization: "Bearer [TOKEN]" }
        return { Authorization: 'Bearer ' + token };
    } else {
        return {};
    }
};