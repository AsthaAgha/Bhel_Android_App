package com.example.bhelvisualizer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bhelvisualizer.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AdminLogin extends AppCompatActivity {

    private EditText adminLoginEmail, adminLoginPassword;
    private Button adminLoginButton;
    private TextView forgotPassButton;

    private DatabaseReference adminDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        adminDatabase = FirebaseDatabase.getInstance().getReference("User");
        forgotPassButton = findViewById(R.id.forgotPassButton);

        adminLoginEmail = findViewById(R.id.adminLoginEmail);
        adminLoginPassword = findViewById(R.id.adminLoginPassword);
        adminLoginButton = findViewById(R.id.adminLoginButton);

        adminLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginAdmin();
            }
        });
    }

    private void loginAdmin() {
        final String email = adminLoginEmail.getText().toString().trim();
        final String password = adminLoginPassword.getText().toString().trim();

        if (email.isEmpty()) {
            adminLoginEmail.setError("Email is required");
            adminLoginEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            adminLoginEmail.setError("Please provide a valid email");
            adminLoginEmail.requestFocus();
            return;
        }

        if (!email.endsWith("@bhel.in")) {
            Toast.makeText(AdminLogin.this, "Please enter official e-mail only", Toast.LENGTH_LONG).show();
            return;
        }

        if (password.isEmpty()) {
            adminLoginPassword.setError("Password is required");
            adminLoginPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            adminLoginPassword.setError("Minimum password length is 6 characters");
            adminLoginPassword.requestFocus();
            return;
        }

        // Log attempt to login
        Log.d("AdminLoginActivity", "Attempting to login with email: " + email);

        // Check if the admin exists in "User" database
        adminDatabase.orderByChild("Email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("AdminLoginActivity", "DataSnapshot received: " + dataSnapshot);

                if (dataSnapshot.exists()) {
                    for (DataSnapshot adminSnapshot : dataSnapshot.getChildren()) {
                        String dbPassword = adminSnapshot.child("Password").getValue(String.class);
                        String role = adminSnapshot.child("Role").getValue(String.class);

                        if (dbPassword != null && (dbPassword.equals(hashPassword(password))||dbPassword.equals(password))){
                            // Password matches, check role
                            if ("Admin".equals(role)) {
                                // Role is Admin, login successful
                                Log.d("AdminLoginActivity", "Login successful for admin: " + email);
                                Toast.makeText(AdminLogin.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                String adminName = adminSnapshot.child("Name").getValue(String.class);

                                // Example: Navigate to AdminPanel
                                SharedPreferencesUtil.setLoggedIn(AdminLogin.this, true, "Admin");
                                setUserDetails(adminSnapshot.getKey());
                            } else {
                                // User is not authorized as Admin
                                Log.d("AdminLoginActivity", "User is not authorized as Admin: " + email);
                                Toast.makeText(AdminLogin.this, "You are not authorized as Admin", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } else {
                            Toast.makeText(AdminLogin.this, "Invalid Password", Toast.LENGTH_SHORT).show();
                            forgotPassButton.setVisibility(View.VISIBLE);

                            return;
                        }
                    }
                } else {
                    // Admin not found in database
                    Log.d("AdminLoginActivity", "Admin not found for email: " + email);
                    Toast.makeText(AdminLogin.this, "Admin not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                Log.e("AdminLoginActivity", "Database Error: " + databaseError.getMessage());
                Toast.makeText(AdminLogin.this, "Database Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void setUserDetails(String userId) {
        DatabaseReference userRef = adminDatabase.child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    // Set user details to SharedPreferences or any other suitable storage
                    SharedPreferencesUtil.setUserPriority(AdminLogin.this, user.getPriority());
                    SharedPreferencesUtil.setUserName(AdminLogin.this, user.getName());
                    SharedPreferencesUtil.setUserEmail(AdminLogin.this, user.getEmail());
                    SharedPreferencesUtil.setUserRole(AdminLogin.this, user.getRole());

                    navigateToAdminPanel();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AdminLogin.this, "Failed to retrieve user details: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void navigateToAdminPanel(){
        Intent intent = new Intent(AdminLogin.this, AdminPanel.class);
        startActivity(intent);
        finish(); // Finish the current activity to prevent going back to login screen
    }
    private String hashPassword(String password) {
        try {
            // Create MessageDigest instance for MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            // Add password bytes to digest
            md.update(password.getBytes());
            // Get the hash's bytes
            byte[] bytes = md.digest();
            // This bytes[] has bytes in decimal format;
            // Convert it to hexadecimal format
            StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }
            // Get complete hashed password in hex format
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
