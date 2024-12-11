package com.example.bhelvisualizer.model;

public class User {
    private String Name;
    private String Email;
    private String Password; // Note: In practice, this should not be stored as plaintext
    private String Role;
    private int Priority;

    public User() {
        // Default constructor required for Firebase
    }

    public User(String Name, String Email, String Password, String Role, int Priority) {
        this.Name = Name;
        this.Email = Email;
        this.Password = Password;
        this.Role = Role;
        this.Priority = Priority;
    }

    // Getters and Setters

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String Email) {
        this.Email = Email;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String Password) {
        this.Password = Password; // Note: In practice, this should not be stored as plaintext
    }

    public String getRole() {
        return Role;
    }

    public void setRole(String Role) {
        this.Role = Role;
    }

    public int getPriority() {
        return Priority;
    }

    public void setPriority(int Priority) {
        this.Priority = Priority;
    }
}
