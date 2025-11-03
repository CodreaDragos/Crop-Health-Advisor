package com.proiect.SCD.CropHealthAdvisor.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * DTO pentru datele primite de la Sentinel Hub Statistical API
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SatelliteDataDTO {
    
    @JsonProperty("data")
    private List<TimeSeriesData> data;
    
    @JsonProperty("aggregation")
    private AggregationInfo aggregation;
    
    // Getters and Setters
    public List<TimeSeriesData> getData() {
        return data;
    }
    
    public void setData(List<TimeSeriesData> data) {
        this.data = data;
    }
    
    public AggregationInfo getAggregation() {
        return aggregation;
    }
    
    public void setAggregation(AggregationInfo aggregation) {
        this.aggregation = aggregation;
    }
    
    /**
     * Clasă pentru datele time-series
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TimeSeriesData {
        @JsonProperty("interval")
        private Interval interval;
        
        @JsonProperty("outputs")
        private Map<String, OutputValue> outputs;
        
        public Interval getInterval() {
            return interval;
        }
        
        public void setInterval(Interval interval) {
            this.interval = interval;
        }
        
        public Map<String, OutputValue> getOutputs() {
            return outputs;
        }
        
        public void setOutputs(Map<String, OutputValue> outputs) {
            this.outputs = outputs;
        }
    }
    
    /**
     * Interval de timp
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Interval {
        @JsonProperty("from")
        private String from;
        
        @JsonProperty("to")
        private String to;
        
        public String getFrom() {
            return from;
        }
        
        public void setFrom(String from) {
            this.from = from;
        }
        
        public String getTo() {
            return to;
        }
        
        public void setTo(String to) {
            this.to = to;
        }
    }
    
    /**
     * Valori de output (NDVI, EVI, etc.)
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OutputValue {
        @JsonProperty("stats")
        private Statistics stats;
        
        @JsonProperty("bins")
        private List<BinValue> bins;
        
        public Statistics getStats() {
            return stats;
        }
        
        public void setStats(Statistics stats) {
            this.stats = stats;
        }
        
        public List<BinValue> getBins() {
            return bins;
        }
        
        public void setBins(List<BinValue> bins) {
            this.bins = bins;
        }
    }
    
    /**
     * Statistici agregate (media, min, max, stdDev)
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Statistics {
        @JsonProperty("min")
        private Double min;
        
        @JsonProperty("max")
        private Double max;
        
        @JsonProperty("mean")
        private Double mean;
        
        @JsonProperty("stdev")
        private Double stdev;
        
        @JsonProperty("sampleCount")
        private Integer sampleCount;
        
        public Double getMin() {
            return min;
        }
        
        public void setMin(Double min) {
            this.min = min;
        }
        
        public Double getMax() {
            return max;
        }
        
        public void setMax(Double max) {
            this.max = max;
        }
        
        public Double getMean() {
            return mean;
        }
        
        public void setMean(Double mean) {
            this.mean = mean;
        }
        
        public Double getStdev() {
            return stdev;
        }
        
        public void setStdev(Double stdev) {
            this.stdev = stdev;
        }
        
        public Integer getSampleCount() {
            return sampleCount;
        }
        
        public void setSampleCount(Integer sampleCount) {
            this.sampleCount = sampleCount;
        }
    }
    
    /**
     * Valori bin (histogramă)
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BinValue {
        @JsonProperty("lowEdge")
        private Double lowEdge;
        
        @JsonProperty("highEdge")
        private Double highEdge;
        
        @JsonProperty("count")
        private Integer count;
        
        public Double getLowEdge() {
            return lowEdge;
        }
        
        public void setLowEdge(Double lowEdge) {
            this.lowEdge = lowEdge;
        }
        
        public Double getHighEdge() {
            return highEdge;
        }
        
        public void setHighEdge(Double highEdge) {
            this.highEdge = highEdge;
        }
        
        public Integer getCount() {
            return count;
        }
        
        public void setCount(Integer count) {
            this.count = count;
        }
    }
    
    /**
     * Informații despre agregare
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AggregationInfo {
        @JsonProperty("evalscript")
        private String evalscript;
        
        @JsonProperty("timeRange")
        private TimeRange timeRange;
        
        public String getEvalscript() {
            return evalscript;
        }
        
        public void setEvalscript(String evalscript) {
            this.evalscript = evalscript;
        }
        
        public TimeRange getTimeRange() {
            return timeRange;
        }
        
        public void setTimeRange(TimeRange timeRange) {
            this.timeRange = timeRange;
        }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TimeRange {
        @JsonProperty("from")
        private String from;
        
        @JsonProperty("to")
        private String to;
        
        public String getFrom() {
            return from;
        }
        
        public void setFrom(String from) {
            this.from = from;
        }
        
        public String getTo() {
            return to;
        }
        
        public void setTo(String to) {
            this.to = to;
        }
    }
}

