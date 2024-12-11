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

public class Login extends AppCompatActivity {

    private EditText loginEmail, loginPassword;
    private Button loginButton;
    private TextView adminBtn;
    private DatabaseReference userDatabase;
    private TextView forgotPassButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userDatabase = FirebaseDatabase.getInstance().getReference("User");
        forgotPassButton = findViewById(R.id.forgotPassButton);
        loginEmail = findViewById(R.id.loginEmail);
        loginPassword = findViewById(R.id.loginPassword);
        loginButton = findViewById(R.id.loginButton);
        adminBtn = findViewById(R.id.adminLoginPrompt);

        if (SharedPreferencesUtil.getLoggedInAs(this).equals("User") && SharedPreferencesUtil.isLoggedIn(this)) {
            navigateToMainActivity();
        } else if (SharedPreferencesUtil.getLoggedInAs(this).equals("Admin") && SharedPreferencesUtil.isLoggedIn(this)) {
            navigateToAdminActivity();
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        adminBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, AdminLogin.class);
                startActivity(intent);
            }
        });
    }

    private void loginUser() {
        final String email = loginEmail.getText().toString().trim();
        final String password = loginPassword.getText().toString().trim();

        if (email.isEmpty()) {
            loginEmail.setError("Email is required");
            loginEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            loginEmail.setError("Please provide a valid email");
            loginEmail.requestFocus();
            return;
        }

        if (!email.endsWith("@bhel.in")) {
            Toast.makeText(Login.this, "Please enter official e-mail only", Toast.LENGTH_LONG).show();
            return;
        }

        if (password.isEmpty()) {
            loginPassword.setError("Password is required");
            loginPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            loginPassword.setError("Minimum password length is 6 characters");
            loginPassword.requestFocus();
            return;
        }

        userDatabase.orderByChild("Email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("LoginActivity", "DataSnapshot received: " + dataSnapshot);

                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String dbPassword = userSnapshot.child("Password").getValue(String.class);
                        if (dbPassword != null&&(dbPassword.equals(password)|| dbPassword.equals(hashPassword(password)))) {
                            SharedPreferencesUtil.setLoggedIn(Login.this, true, "User");
                            setUserDetails(userSnapshot.getKey());
                        } else {
                            Toast.makeText(Login.this, "Invalid Password", Toast.LENGTH_SHORT).show();
                            forgotPassButton.setVisibility(View.VISIBLE);
                            return;
                        }
                    }
                } else {
                    Toast.makeText(Login.this, "User not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                Toast.makeText(Login.this, "Database Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setUserDetails(String userId) {
        DatabaseReference userRef = userDatabase.child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    // Set user details to SharedPreferences or any other suitable storage
                    SharedPreferencesUtil.setUserPriority(Login.this, user.getPriority());
                    SharedPreferencesUtil.setUserName(Login.this, user.getName());
                    SharedPreferencesUtil.setUserEmail(Login.this, user.getEmail());
                    SharedPreferencesUtil.setUserRole(Login.this, user.getRole());

                    navigateToMainActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Login.this, "Failed to retrieve user details: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(Login.this, MainActivity.class);
        startActivity(intent);
        finish(); // Finish the current activity to prevent going back to login screen
    }

    private void navigateToAdminActivity() {
        Intent intent = new Intent(Login.this, AdminPanel.class);
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
