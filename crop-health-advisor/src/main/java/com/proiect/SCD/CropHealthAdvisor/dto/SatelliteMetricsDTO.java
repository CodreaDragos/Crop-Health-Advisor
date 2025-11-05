package com.proiect.SCD.CropHealthAdvisor.dto;

/**
 * Simplified DTO for metrics calculated from Sentinel Hub data
 */
public class SatelliteMetricsDTO {
    private double ndvi;
    private double evi;
    private double ndwi;
    private double temperature; // Land Surface Temperature (LST) - soil temperature in °C
    private double precipitation; // Estimated precipitation (mm) - derived from satellite indices
    private double soilMoisture; // Soil moisture (index derived from NDWI and other indices)
    private double cloudCover; // Cloud cover (%) - estimated from Sentinel-2 metadata
    private double evapotranspiration; // Estimated evapotranspiration (mm/day) - derived from NDVI and LST
    
    // Raw spectral data (optional, for advanced analysis)
    private Double redReflectance;
    private Double nirReflectance;
    private Double swirReflectance;
    
    public SatelliteMetricsDTO() {
    }
    
    public SatelliteMetricsDTO(double ndvi, double evi, double ndwi, double temperature, double precipitation) {
        this.ndvi = ndvi;
        this.evi = evi;
        this.ndwi = ndwi;
        this.temperature = temperature;
        this.precipitation = precipitation;
    }
    
    // Getters and Setters
    public double getNdvi() {
        return ndvi;
    }
    
    public void setNdvi(double ndvi) {
        this.ndvi = ndvi;
    }
    
    public double getEvi() {
        return evi;
    }
    
    public void setEvi(double evi) {
        this.evi = evi;
    }
    
    public double getNdwi() {
        return ndwi;
    }
    
    public void setNdwi(double ndwi) {
        this.ndwi = ndwi;
    }
    
    public double getTemperature() {
        return temperature;
    }
    
    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
    
    public double getPrecipitation() {
        return precipitation;
    }
    
    public void setPrecipitation(double precipitation) {
        this.precipitation = precipitation;
    }
    
    public Double getRedReflectance() {
        return redReflectance;
    }
    
    public void setRedReflectance(Double redReflectance) {
        this.redReflectance = redReflectance;
    }
    
    public Double getNirReflectance() {
        return nirReflectance;
    }
    
    public void setNirReflectance(Double nirReflectance) {
        this.nirReflectance = nirReflectance;
    }
    
    public Double getSwirReflectance() {
        return swirReflectance;
    }
    
    public void setSwirReflectance(Double swirReflectance) {
        this.swirReflectance = swirReflectance;
    }
    
    public double getSoilMoisture() {
        return soilMoisture;
    }
    
    public void setSoilMoisture(double soilMoisture) {
        this.soilMoisture = soilMoisture;
    }
    
    public double getCloudCover() {
        return cloudCover;
    }
    
    public void setCloudCover(double cloudCover) {
        this.cloudCover = cloudCover;
    }
    
    public double getEvapotranspiration() {
        return evapotranspiration;
    }
    
    public void setEvapotranspiration(double evapotranspiration) {
        this.evapotranspiration = evapotranspiration;
    }
}

