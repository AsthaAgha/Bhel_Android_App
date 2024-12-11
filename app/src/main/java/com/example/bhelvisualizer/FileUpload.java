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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class FileUpload extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;
    private Uri fileUri;

    private EditText fileNameInput;
    private EditText descriptionInput;
    private Spinner prioritySpinner;
    private Spinner monthSpinner;
    private Spinner yearSpinner;
    private Button uploadFileButton;
    private Button submitButton;
    private ProgressBar progressBar;
    private View MainLayout;

    private FirebaseStorage storage;
    private FirebaseFirestore firestore;

    private String fileType; // Added to store file type/extension

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_upload);

        fileNameInput = findViewById(R.id.fileNameInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        prioritySpinner = findViewById(R.id.prioritySpinner);
        monthSpinner = findViewById(R.id.monthSpinner);
        yearSpinner = findViewById(R.id.yearSpinner);
        uploadFileButton = findViewById(R.id.uploadFileButton);
        submitButton = findViewById(R.id.submitButton);
        progressBar = findViewById(R.id.progressBar);

        storage = FirebaseStorage.getInstance();
        firestore = FirebaseFirestore.getInstance();
        MainLayout = findViewById(R.id.mainLayout);

        uploadFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileUri != null) {
                    MainLayout.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.VISIBLE);

                    getFileTypeFromUri(fileUri); // Determine file type from URI
                    uploadFile();
                } else {
                    Toast.makeText(FileUpload.this, "No file selected", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            fileUri = data.getData();
            submitButton.setVisibility(View.VISIBLE);
        }
    }

    private void getFileTypeFromUri(Uri uri) {
        // Get file extension from URI
        String fileExtension = getContentResolver().getType(uri);
        if (fileExtension != null) {
            fileType = fileExtension.substring(fileExtension.lastIndexOf("/") + 1);
        }
    }

    private void uploadFile() {
        String fileName = fileNameInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String priority = prioritySpinner.getSelectedItem().toString();
        String month = monthSpinner.getSelectedItem().toString();
        int year = Integer.parseInt(yearSpinner.getSelectedItem().toString());
        String username = SharedPreferencesUtil.getUserName(this);

        StorageReference fileReference = storage.getReference().child("Files/" + fileName);

        // Show ProgressBar
        progressBar.setVisibility(View.VISIBLE);

        fileReference.putFile(fileUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String fileUrl = uri.toString();

                                Map<String, Object> fileData = new HashMap<>();
                                fileData.put("fileName", fileName);
                                fileData.put("description", description);
                                fileData.put("priority", priority);
                                fileData.put("month", month);
                                fileData.put("year", year);
                                fileData.put("username", username);
                                fileData.put("fileUrl", fileUrl);
                                fileData.put("fileType", fileType); // Store file type/extension
                                fileData.put("timestamp", FieldValue.serverTimestamp()); // Add timestamp

                                firestore.collection("Files")
                                        .document(fileName)
                                        .set(fileData)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(FileUpload.this, "File uploaded successfully", Toast.LENGTH_SHORT).show();
                                                // Hide ProgressBar
                                                progressBar.setVisibility(View.GONE);
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(FileUpload.this, "Failed to upload file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                // Hide ProgressBar on failure
                                                progressBar.setVisibility(View.GONE);
                                            }
                                        });

                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(FileUpload.this, "Failed to upload file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        // Hide ProgressBar on failure
                        progressBar.setVisibility(View.GONE);
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        // Calculate progress percentage
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        // Update ProgressBar with the calculated progress
                        progressBar.setProgress((int) progress);
                    }
                });
    }
}
