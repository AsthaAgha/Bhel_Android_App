package com.example.bhelvisualizer;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bhelvisualizer.model.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CRUD extends AppCompatActivity {

    private EditText nameEditText, emailEditText, passwordEditText;
    private Spinner spinnerPriority, spinnerType;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crud);

        nameEditText = findViewById(R.id.editName);
        emailEditText = findViewById(R.id.editEmail);
        passwordEditText = findViewById(R.id.editPassword);
        spinnerPriority = findViewById(R.id.spinnerPriority);
        spinnerType = findViewById(R.id.spinnerType);
        Button createButton = findViewById(R.id.CreateAdmin);

        // Populate spinner for priority
        ArrayAdapter<CharSequence> priorityAdapter = ArrayAdapter.createFromResource(this,
                R.array.priority_array, android.R.layout.simple_spinner_item);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(priorityAdapter);

        // Populate spinner for user type
        ArrayAdapter<CharSequence> userTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.user_type_array, android.R.layout.simple_spinner_item);
        userTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(userTypeAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("User");

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUser();
            }
        });
    }

    private void createUser() {
        String Name = nameEditText.getText().toString().trim();
        String Email = emailEditText.getText().toString().trim();
        String Password = passwordEditText.getText().toString().trim();
        String priorityString = spinnerPriority.getSelectedItem().toString();
        String Role = spinnerType.getSelectedItem().toString();

        if (TextUtils.isEmpty(Name)) {
            nameEditText.setError("Name is required");
            nameEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(Email)) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
            emailEditText.setError("Please provide a valid email");
            emailEditText.requestFocus();
            return;
        }
        if (!Email.endsWith("@bhel.in")) {
            emailEditText.setError("Please provide official mail only");
            return;
        }

        if (TextUtils.isEmpty(Password)) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return;
        }

        if (Password.length() < 6) {
            passwordEditText.setError("Minimum password length should be 6 characters");
            passwordEditText.requestFocus();
            return;
        }

        int Priority = Integer.parseInt(priorityString);
        String hashedPassword = hashPassword(Password);

        // Use email as the unique identifier for the user
        String userId = Name;
        User user = new User(Name, Email, hashedPassword, Role, Priority);

        databaseReference.child(userId).child("Name").setValue(user.getName());
        databaseReference.child(userId).child("Email").setValue(user.getEmail());
        databaseReference.child(userId).child("Password").setValue(user.getPassword());
        databaseReference.child(userId).child("Role").setValue(user.getRole());
        databaseReference.child(userId).child("Priority").setValue(user.getPriority())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CRUD.this, "User added successfully", Toast.LENGTH_SHORT).show();
                    clearFields();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CRUD.this, "Failed to add user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            md.update(password.getBytes());

            byte[] bytes = md.digest();

            StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
    private void clearFields() {
        nameEditText.setText("");
        emailEditText.setText("");
        passwordEditText.setText("");
        spinnerPriority.setSelection(0); // Set the default selection for priority spinner
        spinnerType.setSelection(0); // Set the default selection for type spinner
    }
}
