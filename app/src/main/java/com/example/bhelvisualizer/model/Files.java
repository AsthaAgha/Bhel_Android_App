package com.example.bhelvisualizer.model;

import com.google.firebase.firestore.PropertyName;
import com.google.firebase.Timestamp;

public class Files {
    private String fileName;
    private String description;
    private String priority;
    private String month;
    private int year;
    private String username;
    private String fileUrl; // URI field for storing file URL
    private String fileType; // Field to store file type or extension

    private Timestamp timestamp;

    // Required empty constructor for Firestore
    public Files() {
    }

    public Files(String fileName, String description, String priority, String month, int year, String username, String fileUrl, String fileType,Timestamp timestamp) {
        this.fileName = fileName;
        this.description = description;
        this.priority = priority;
        this.month = month;
        this.year = year;
        this.username = username;
        this.fileUrl = fileUrl;
        this.fileType = fileType;
        this.timestamp = timestamp;
    }

    @PropertyName("FileName")
    public String getFileName() {
        return fileName;
    }

    @PropertyName("FileName")
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @PropertyName("Description")
    public String getDescription() {
        return description;
    }

    @PropertyName("Description")
    public void setDescription(String description) {
        this.description = description;
    }

    @PropertyName("Priority")
    public String getPriority() {
        return priority;
    }

    @PropertyName("Priority")
    public void setPriority(String priority) {
        this.priority = priority;
    }

    @PropertyName("Month")
    public String getMonth() {
        return month;
    }

    @PropertyName("Month")
    public void setMonth(String month) {
        this.month = month;
    }

    @PropertyName("Year")
    public int getYear() {
        return year;
    }

    @PropertyName("Year")
    public void setYear(int year) {
        this.year = year;
    }

    @PropertyName("Username")
    public String getUsername() {
        return username;
    }

    @PropertyName("Username")
    public void setUsername(String username) {
        this.username = username;
    }

    @PropertyName("FileUrl")
    public String getFileUrl() {
        return fileUrl;
    }

    @PropertyName("FileUrl")
    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
