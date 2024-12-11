package com.example.bhelvisualizer.model;

import com.google.firebase.Timestamp;

public class Report {
    private String name;
    private int numberOfEntries;
    private String priority;
    private String description;
    private String chartType;
    private String csvUrl;
    private Timestamp timestamp;
    public Report() {
        // Default constructor required for calls to DataSnapshot.getValue(Report.class)
    }

    public Report(String name, int numberOfEntries, String priority, String description, String chartType, String csvUrl,Timestamp timestamp) {
        this.name = name;
        this.numberOfEntries = numberOfEntries;
        this.priority = priority;
        this.description = description;
        this.chartType = chartType;
        this.csvUrl = csvUrl;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumberOfEntries() {
        return numberOfEntries;
    }

    public void setNumberOfEntries(int numberOfEntries) {
        this.numberOfEntries = numberOfEntries;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getChartType() {
        return chartType;
    }

    public void setChartType(String chartType) {
        this.chartType = chartType;
    }

    public String getCsvUrl() {
        return csvUrl;
    }

    public void setCsvUrl(String csvUrl) {
        this.csvUrl = csvUrl;
    }
    public String getCsvFileName() {
        return csvUrl != null ? csvUrl.substring(csvUrl.lastIndexOf('/') + 1) : null;
    }
    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}