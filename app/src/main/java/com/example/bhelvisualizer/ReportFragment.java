package com.example.bhelvisualizer;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.bhelvisualizer.adapter.ReportAdapter;
import com.example.bhelvisualizer.model.Report;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
public class ReportFragment extends Fragment implements ReportAdapter.OnReportClickListener {

    private RecyclerView recyclerView;
    private ReportAdapter reportAdapter;
    private List<Report> reports;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    private int userPriority;
    private EditText searchEditText;

    public ReportFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false);

        progressBar = view.findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.navy_blue),
                android.graphics.PorterDuff.Mode.SRC_IN);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchReportsFromFirestore();
            }
        });

        searchEditText = view.findViewById(R.id.searchEditText);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterReports(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not used
            }
        });

        userPriority = SharedPreferencesUtil.getUserPriority(requireContext());

        reports = new ArrayList<>();
        reportAdapter = new ReportAdapter(requireContext(), reports, this);
        recyclerView.setAdapter(reportAdapter);

        fetchReportsFromFirestore();

        return view;
    }

    private void fetchReportsFromFirestore() {
        progressBar.setVisibility(View.VISIBLE); // Show the progress bar

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference reportsCollection = db.collection("Reports");

        reportsCollection.orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            reports.clear();
                            for (DocumentSnapshot document : task.getResult()) {
                                Report report = document.toObject(Report.class);
                                if (shouldDisplayReport(report)) {
                                    reports.add(report);
                                }
                            }
                            progressBar.setVisibility(View.GONE); // Hide the progress bar
                            reportAdapter.notifyDataSetChanged();
                            swipeRefreshLayout.setRefreshing(false); // Stop refreshing animation
                        } else {
                            Toast.makeText(requireContext(), "Error getting reports: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE); // Hide the progress bar on error
                            swipeRefreshLayout.setRefreshing(false); // Stop refreshing animation
                        }
                    }
                });
    }

    private boolean shouldDisplayReport(Report report) {
        int reportPriority = Integer.parseInt(report.getPriority()); // Assuming Report class has getPriority() method
        return (userPriority == 1) ||
                (userPriority == 2 && reportPriority >= 2) ||
                (userPriority == 3 && reportPriority == 3);
    }

    private void filterReports(String query) {
        List<Report> filteredList = new ArrayList<>();

        for (Report report : reports) {
            if (report.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(report);
            }
        }

        reportAdapter.filterList(filteredList);
    }

    @Override
    public void onReportClick(Report report) {
        Intent intent = new Intent(requireContext(), Graph.class);
        intent.putExtra("reportName", report.getName());
        intent.putExtra("chartType", report.getChartType());
        intent.putExtra("csvFileName", report.getCsvFileName());
        startActivity(intent);
    }
}
