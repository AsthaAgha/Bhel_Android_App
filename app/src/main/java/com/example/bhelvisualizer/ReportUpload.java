package com.example.bhelvisualizer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bhelvisualizer.model.Report;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.Timestamp;

public class ReportUpload extends AppCompatActivity {

    private static final int PICK_CSV_REQUEST = 1;

    private Spinner prioritySpinner;
    private Button uploadCSVButton;
    private ProgressBar progressBar;
    private EditText descriptionInput;
    private Spinner chartTypeSpinner;
    private Button submitDetailsButton;
    private EditText reportNameInput;
    private EditText numberOfEntriesInput;
    private TextView pleaseUpload;

    private Uri csvFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_upload);
        FirebaseApp.initializeApp(this);

        progressBar = findViewById(R.id.progressBar);
        pleaseUpload = findViewById(R.id.pleaseUploadTextView);
        prioritySpinner = findViewById(R.id.prioritySpinner);
        uploadCSVButton = findViewById(R.id.uploadCSVButton);
        descriptionInput = findViewById(R.id.descriptionInput);
        chartTypeSpinner = findViewById(R.id.chartTypeSpinner);
        submitDetailsButton = findViewById(R.id.submitDetailsButton);
        reportNameInput = findViewById(R.id.reportNameInput);
        numberOfEntriesInput = findViewById(R.id.numberOfEntriesInput);

        uploadCSVButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("text/csv");
                startActivityForResult(intent, PICK_CSV_REQUEST);
            }
        });

        submitDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                uploadCSVButton.setVisibility(View.GONE);
                submitDetailsButton.setVisibility(View.GONE);
                    // Hide "Please Upload" text view if shown
                uploadReportDetails();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CSV_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            csvFileUri = data.getData();
            pleaseUpload.setVisibility(View.INVISIBLE);
            submitDetailsButton.setVisibility(View.VISIBLE);
        }
    }

    private void uploadReportDetails() {
        if (csvFileUri == null) {
            Toast.makeText(this, "Please upload a CSV file", Toast.LENGTH_SHORT).show();
            return;
        }

        String reportName = reportNameInput.getText().toString();
        int numberOfEntries = Integer.parseInt(numberOfEntriesInput.getText().toString());
        String priority = prioritySpinner.getSelectedItem().toString();
        String description = descriptionInput.getText().toString();
        String chartType = chartTypeSpinner.getSelectedItem().toString();

        if (description.split("\\s+").length > 50) {
            Toast.makeText(this, "Description must be 50 words or less", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference csvRef = storageRef.child("Reports/" + reportName + ".csv");

        UploadTask uploadTask = csvRef.putFile(csvFileUri);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            progressBar.setVisibility(View.VISIBLE);
            uploadCSVButton.setVisibility(View.GONE);
            submitDetailsButton.setVisibility(View.GONE);
            pleaseUpload.setVisibility(View.INVISIBLE); // Hide "Please Upload" text view

            csvRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String csvUrl = uri.toString();

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                Timestamp timestamp = Timestamp.now(); // Get the current timestamp
                Report report = new Report(reportName, numberOfEntries, priority, description, chartType, csvUrl, timestamp);

                // Save report with unique document name
                db.collection("Reports").document(reportName)
                        .set(report)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(ReportUpload.this, "Report submitted successfully", Toast.LENGTH_SHORT).show();
                            finish(); // Finish the activity on success
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(ReportUpload.this, "Error submitting report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        });
            }).addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                uploadCSVButton.setVisibility(View.VISIBLE);
                submitDetailsButton.setVisibility(View.VISIBLE);
                Toast.makeText(ReportUpload.this, "Error getting CSV download URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            });

        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            uploadCSVButton.setVisibility(View.VISIBLE);
            submitDetailsButton.setVisibility(View.VISIBLE);
            Toast.makeText(ReportUpload.this, "Error uploading CSV: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        });
    }
}