// src/services/locationService.js
import axios from 'axios';
import { getAuthHeader } from './authService';

const API_URL = 'http://localhost:8081/api';

// Helper function to get user ID from token
// Note: In production, you'd decode the JWT properly
// For now, we'll need to get it from backend or store it after login
export const getUserId = () => {
  // TODO: Decode JWT token to get user ID
  // For now, return stored user ID or null
  return localStorage.getItem('user_id') || null;
};

// Get all locations for the current user
export const getUserLocations = async (userId) => {
  try {
    const response = await axios.get(`${API_URL}/locations/user/${userId}`, {
      headers: getAuthHeader(),
    });
    return response.data;
  } catch (error) {
    console.error('Error fetching locations:', error);
    throw error.response?.data || error.message;
  }
};

// Create a new location
export const createLocation = async (locationData) => {
  try {
    const response = await axios.post(`${API_URL}/locations`, locationData, {
      headers: getAuthHeader(),
    });
    return response.data;
  } catch (error) {
    console.error('Error creating location:', error);
    throw error.response?.data || error.message;
  }
};

// Get reports for a specific location
export const getLocationReports = async (locationId) => {
  try {
    const response = await axios.get(`${API_URL}/reports/location/${locationId}`, {
      headers: getAuthHeader(),
    });
    return response.data;
  } catch (error) {
    // Return empty array if 404 (no reports found)
    if (error.response && error.response.status === 404) {
      return [];
    }
    console.error('Error fetching reports:', error);
    throw error.response?.data || error.message;
  }
};

// Get NDVI image for a location
export const getNDVIImageUrl = (lat, lon, width = 512, height = 512) => {
  const params = new URLSearchParams({
    lat: lat.toString(),
    lon: lon.toString(),
    width: width.toString(),
    height: height.toString()
  });
  return `${API_URL}/satellite/image/ndvi?${params.toString()}`;
};

// Helper to get image with auth header (for blob requests)
export const getNDVIImageBlob = async (lat, lon, width = 512, height = 512) => {
  try {
    const params = new URLSearchParams({
      lat: lat.toString(),
      lon: lon.toString(),
      width: width.toString(),
      height: height.toString()
    });
    const response = await axios.get(`${API_URL}/satellite/image/ndvi?${params.toString()}`, {
      headers: getAuthHeader(),
      responseType: 'blob'
    });
    return URL.createObjectURL(response.data);
  } catch (error) {
    console.error('Error fetching NDVI image:', error);
    throw error.response?.data || error.message;
  }
};

// Delete a location (cascades to reports)
export const deleteLocation = async (locationId) => {
  try {
    await axios.delete(`${API_URL}/locations/${locationId}`, {
      headers: getAuthHeader(),
    });
    return true;
  } catch (error) {
    console.error('Error deleting location:', error);
    throw error.response?.data || error.message;
  }
};

// Delete a report
export const deleteReport = async (reportId) => {
  try {
    await axios.delete(`${API_URL}/reports/${reportId}`, {
      headers: getAuthHeader(),
    });
    return true;
  } catch (error) {
    console.error('Error deleting report:', error);
    throw error.response?.data || error.message;
  }
};

