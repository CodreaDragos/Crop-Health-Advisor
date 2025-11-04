// src/services/authService.js
import axios from 'axios';

const API_URL = 'http://localhost:8081/api/auth';

// Login call
export const login = async (email, password) => {
    try {
        const response = await axios.post(`${API_URL}/login`, { email, password });
        
        if (response.data.accessToken) {
            // Store token in localStorage
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
        if (error.response) {
            const errorMessage = error.response.data || error.response.statusText || 'Authentication error';
            throw new Error(typeof errorMessage === 'string' ? errorMessage : JSON.stringify(errorMessage));
        } else if (error.request) {
            throw new Error('Could not connect to server. Check if backend is running and CORS configuration.');
        } else {
            throw new Error(error.message || 'Unknown authentication error');
        }
    }
};

// Registration call
export const register = async (username, email, password) => {
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

// Logout
export const logout = () => {
    localStorage.removeItem('user_token');
    localStorage.removeItem('user_email'); // Optional but useful
};

// Get Authorization Header
export const getAuthHeader = () => {
    const token = localStorage.getItem('user_token');
    if (token) {
        return { Authorization: 'Bearer ' + token };
    } else {
        return {};
    }
};