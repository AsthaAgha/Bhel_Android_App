package com.example.bhelvisualizer;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.bhelvisualizer.adapter.FileAdapter;
import com.example.bhelvisualizer.model.Files;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ViewFilesFragment extends Fragment implements FileAdapter.OnItemClickListener {

    private Spinner yearSpinner;
    private Spinner monthSpinner;
    private int userPriority;
    private Button showAllButton;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private EditText searchEditText;

    private FirebaseFirestore firestore;
    private CollectionReference filesCollection;

    private FileAdapter fileAdapter;
    private List<Files> files;
    private List<Files> allFiles; // To store all files for filtering

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_files, container, false);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();
        filesCollection = firestore.collection("Files");

        // Initialize Views
        yearSpinner = view.findViewById(R.id.yearSpinner);
        monthSpinner = view.findViewById(R.id.monthSpinner);
        showAllButton = view.findViewById(R.id.showAllButton);
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        searchEditText = view.findViewById(R.id.searchEditText);
        userPriority = SharedPreferencesUtil.getUserPriority(requireContext());

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        files = new ArrayList<>();
        allFiles = new ArrayList<>(); // Initialize allFiles list
        fileAdapter = new FileAdapter(files, this);
        recyclerView.setAdapter(fileAdapter);

        // Setup Spinners
        setupYearSpinner();
        setupMonthSpinner();

        // Setup Search EditText
        setupSearchEditText();

        // Setup Buttons
        showAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                yearSpinner.setSelection(0);
                monthSpinner.setSelection(0);
                fetchAllFiles();
            }
        });

        // Load all files by default
        fetchAllFiles();

        // Setup SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                yearSpinner.setSelection(0);
                monthSpinner.setSelection(0);
                fetchAllFiles();
            }
        });

        return view;
    }

    private void setupSearchEditText() {
        searchEditText.setVisibility(View.VISIBLE);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterByUsername(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
        });
    }

    private void filterByUsername(String username) {
        List<Files> filteredFiles = new ArrayList<>();
        for (Files file : allFiles) {
            if (file.getUsername().toLowerCase().contains(username.toLowerCase())) {
                filteredFiles.add(file);
            }
        }
        fileAdapter.filterList(filteredFiles);
    }

    private void setupYearSpinner() {
        String[] yearArray = getResources().getStringArray(R.array.year_array);
        List<String> years = new ArrayList<>(Arrays.asList(yearArray));
        years.add(0, "All Year");

        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);

        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedYear = parent.getItemAtPosition(position).toString();
                String selectedMonth = monthSpinner.getSelectedItem().toString();
                filterFiles(selectedYear, selectedMonth);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setupMonthSpinner() {
        String[] monthArray = getResources().getStringArray(R.array.month_array);
        List<String> months = new ArrayList<>(Arrays.asList(monthArray));
        months.add(0, "All Month");

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdapter);

        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedMonth = parent.getItemAtPosition(position).toString();
                String selectedYear = yearSpinner.getSelectedItem().toString();
                filterFiles(selectedYear, selectedMonth);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void fetchAllFiles() {
        progressBar.setVisibility(View.VISIBLE);
        filesCollection.orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            files.clear();
                            allFiles.clear(); // Clear previous data
                            for (DocumentSnapshot document : task.getResult()) {
                                Files file = document.toObject(Files.class);
                                if (shouldDisplayFile(file)) {
                                    files.add(file);
                                    allFiles.add(file);
                                }
                            }
                            progressBar.setVisibility(View.GONE);
                            fileAdapter.notifyDataSetChanged();
                            swipeRefreshLayout.setRefreshing(false);
                        } else {
                            Toast.makeText(requireContext(), "Error fetching files: " + task.getException(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }
                });
    }

    private void filterFiles(String year, String month) {
        List<Files> filteredFiles = new ArrayList<>();
        for (Files file : allFiles) {
            boolean matchesYear = year.equals("All Year") || String.valueOf(file.getYear()).equals(year);
            boolean matchesMonth = month.equals("All Month") || file.getMonth().equals(month);
            if (matchesYear && matchesMonth) {
                filteredFiles.add(file);
            }
        }
        fileAdapter.filterList(filteredFiles);
    }

    @Override
    public void onItemClick(int position) {
        Files file = files.get(position); // Use files list to get position
        openOrDownloadFile(file);
    }

    private void openOrDownloadFile(Files file) {
        String fileName = file.getFileName();
        String fileUrl = file.getFileUrl();
        String fileType = file.getFileType(); // Retrieve fileType from Firestore

        // Ensure the file name matches the exact name including extension
        if (!fileName.toLowerCase().endsWith("." + fileType.toLowerCase())) {
            fileName += "." + fileType;
        }

        // Check if file exists in downloads directory
        if (isFileDownloaded(fileName)) {
            openDownloadedFile(fileName, fileType);
        } else {
            downloadFile(file, fileName, fileType); // Pass the fileName and fileType to the download method
        }
    }

    private boolean isFileDownloaded(String fileName) {
        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + fileName;
        File file = new File(filePath);
        return file.exists();
    }

    private void downloadFile(Files file, String fileName, String fileType) { // Pass fileType parameter
        String fileUrl = file.getFileUrl();

        // Ensure the file name includes the extension
        if (!fileName.toLowerCase().endsWith("." + fileType.toLowerCase())) {
            fileName += "." + fileType;
        }

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(fileUrl));
        request.setTitle(fileName);
        request.setDescription("Downloading file...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        DownloadManager downloadManager = (DownloadManager) requireContext().getSystemService(Context.DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);
    }

    private void openDownloadedFile(String fileName, String fileType) { // Add fileType parameter
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
        Uri fileUri = FileProvider.getUriForFile(requireContext(), "com.example.bhelvisualizer.fileprovider", file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, getMimeType(fileType)); // Use getMimeType method to set the correct MIME type
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Cannot open file", Toast.LENGTH_SHORT).show();
        }
    }

    private String getMimeType(String fileType) {
        switch (fileType.toLowerCase()) {
            case "pdf":
                return "application/pdf";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "docx":
                return "application/msword";
            default:
                return "*/*"; // All file types
        }
    }
    private boolean shouldDisplayFile(Files file) {
        int reportPriority = Integer.parseInt(file.getPriority()); // Assuming Report class has getPriority() method
        return (userPriority == 1) ||
                (userPriority == 2 && reportPriority >= 2) ||
                (userPriority == 3 && reportPriority == 3);
    }
}
