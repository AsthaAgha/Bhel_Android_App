package com.example.bhelvisualizer;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ChangePassword extends AppCompatActivity {

    private EditText etOldPassword, etNewPassword, etConfirmNewPassword;
    private Button btnChangePassword;
    private ProgressBar progressBar;
    private ImageView homeButton;
    private DatabaseReference userDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        etOldPassword = findViewById(R.id.etOldPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        homeButton=findViewById(R.id.homebackButton);
        progressBar = findViewById(R.id.progressBar);

        userDatabase = FirebaseDatabase.getInstance().getReference("User");
        homeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent;
                if(SharedPreferencesUtil.getLoggedInAs(ChangePassword.this).equalsIgnoreCase("Admin")) {
                   intent = new Intent(ChangePassword.this,AdminPanel.class);
                }
                else{
                    intent = new Intent(ChangePassword.this,MainActivity.class);
                }
                startActivity(intent);
            }
        });

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });
    }

    private void changePassword() {
        final String oldPassword = etOldPassword.getText().toString().trim();
        final String newPassword = etNewPassword.getText().toString().trim();
        final String confirmNewPassword = etConfirmNewPassword.getText().toString().trim();

        if (TextUtils.isEmpty(oldPassword)) {
            etOldPassword.setError("Old password is required");
            etOldPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(newPassword)) {
            etNewPassword.setError("New password is required");
            etNewPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmNewPassword)) {
            etConfirmNewPassword.setError("Confirm new password is required");
            etConfirmNewPassword.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmNewPassword)) {
            etConfirmNewPassword.setError("New password and confirm password do not match");
            etConfirmNewPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        String email = SharedPreferencesUtil.getUserEmail(this); // Fetch email from SharedPreferences

        userDatabase.orderByChild("Email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String dbPassword = userSnapshot.child("Password").getValue(String.class);
                        if (dbPassword != null && dbPassword.equals(hashPassword(oldPassword))) {
                            userSnapshot.getRef().child("Password").setValue(hashPassword(newPassword));
                            Toast.makeText(ChangePassword.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            finish(); // Close the activity after changing password
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(ChangePassword.this, "Old password is incorrect", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ChangePassword.this, "User not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ChangePassword.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
