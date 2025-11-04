package com.proiect.SCD.CropHealthAdvisor.dto;

import com.proiect.SCD.CropHealthAdvisor.models.Reports;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for report response that includes all satellite metrics.
 * Allows displaying all data without modifying the Reports model.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponseDTO {
    private Long id;
    private Double ndviValue;
    private Double eviValue;
    private Double ndwiValue;
    private Double temperatureValue; // LST
    private Double precipitationValue;
    private Double soilMoisture;
    private Double cloudCover;
    private Double evapotranspiration;
    private String aiInterpretation;
    private LocalDateTime reportDate;
    private Object location;
    
    /**
     * Creates a DTO from Reports and SatelliteMetricsDTO.
     */
    public static ReportResponseDTO from(Reports report, SatelliteMetricsDTO metrics) {
        ReportResponseDTO dto = new ReportResponseDTO();
        dto.setId(report.getId());
        dto.setNdviValue(report.getNdviValue());
        dto.setEviValue(metrics.getEvi());
        dto.setNdwiValue(metrics.getNdwi());
        dto.setTemperatureValue(report.getTemperatureValue());
        dto.setPrecipitationValue(report.getPrecipitationValue());
        dto.setSoilMoisture(metrics.getSoilMoisture());
        dto.setCloudCover(metrics.getCloudCover());
        dto.setEvapotranspiration(metrics.getEvapotranspiration());
        dto.setAiInterpretation(report.getAiInterpretation());
        dto.setReportDate(report.getReportDate());
        dto.setLocation(report.getLocation());
        return dto;
    }
}


