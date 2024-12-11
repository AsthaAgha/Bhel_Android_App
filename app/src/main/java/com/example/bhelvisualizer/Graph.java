package com.example.bhelvisualizer;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.BubbleChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Graph extends AppCompatActivity {
    private BarChart barChart;
    private PieChart pieChart;
    private LineChart lineChart;
    private BubbleChart bubbleChart;
    private HorizontalBarChart horizontalBarChart;
    private ScatterChart scatterChart;
    private TextView xAxisLabel;
    private TextView yAxisLabel;
    private ImageView yAxisArrow;
    private ImageView xAxisArrow;
    private Spinner chartTypeSpinner;
    private File outputFile;
    private String currentChartType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); // Hide default title

        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText("BHEL Visualizer");

        String reportName = getIntent().getStringExtra("reportName");
        currentChartType = getIntent().getStringExtra("chartType");

        barChart = findViewById(R.id.barChart);
        pieChart = findViewById(R.id.pieChart);
        lineChart = findViewById(R.id.lineChart);
        bubbleChart = findViewById(R.id.bubbleChart);
        horizontalBarChart = findViewById(R.id.horizontalBarChart);
        scatterChart = findViewById(R.id.scatterChart);
        xAxisLabel = findViewById(R.id.xAxisLabel);
        yAxisLabel = findViewById(R.id.yAxisLabel);
        yAxisArrow = findViewById(R.id.yAxisArrow);
        xAxisArrow = findViewById(R.id.xAxisArrow);
        chartTypeSpinner = findViewById(R.id.chartTypeSpinner);
        TextView chartTitle = findViewById(R.id.chartTitleTextView);
        chartTitle.setText(reportName);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference reportRef = db.collection("Reports").document(reportName);
        reportRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String csvUrl = documentSnapshot.getString("csvUrl");
                outputFile = new File(getCacheDir(), reportName + ".csv");

                if (outputFile.exists()) {
                    // File already exists, use it
                    Toast.makeText(Graph.this, "Using cached chart data file", Toast.LENGTH_SHORT).show();
                    displayChart(outputFile, currentChartType);
                    setSpinnerSelection(currentChartType); // Set initial spinner selection
                } else {
                    // File doesn't exist, download it
                    downloadFile(csvUrl, outputFile, new OnDownloadCompleteListener() {
                        @Override
                        public void onDownloadComplete(File file) {
                            Toast.makeText(Graph.this, "Chart data file downloaded successfully", Toast.LENGTH_SHORT).show();
                            displayChart(file, currentChartType);
                            setSpinnerSelection(currentChartType); // Set initial spinner selection
                        }

                        @Override
                        public void onDownloadFailed(Exception e) {
                            Toast.makeText(Graph.this, "Error downloading CSV file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                Toast.makeText(Graph.this, "Error: Report does not exist", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(Graph.this, "Error fetching report: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });

        chartTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String newChartType = parent.getItemAtPosition(position).toString();
                if (outputFile != null && outputFile.exists()) {
                    displayChart(outputFile, newChartType);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void displayChart(File file, String chartType) {
        hideAllCharts();

        switch (chartType) {
            case "Bar Chart":
                barChart.setVisibility(View.VISIBLE);
                CSVDataReader.displayChart(Graph.this, file, chartType, barChart);
                xAxisLabel.setText(CSVDataReader.getxlabel());
                yAxisLabel.setText(CSVDataReader.getylabel());
                yAxisArrow.setVisibility(View.VISIBLE);
                yAxisLabel.setVisibility(View.VISIBLE);
                xAxisLabel.setVisibility(View.VISIBLE);
                xAxisArrow.setVisibility(View.VISIBLE);
                break;
            case "Pie Chart":
                pieChart.setVisibility(View.VISIBLE);
                yAxisArrow.setVisibility(View.INVISIBLE);
                yAxisLabel.setVisibility(View.INVISIBLE);
                xAxisLabel.setVisibility(View.INVISIBLE);
                xAxisArrow.setVisibility(View.INVISIBLE);
                CSVDataReader.displayChart(Graph.this, file, chartType, pieChart);
                break;
            case "Line Chart":
                lineChart.setVisibility(View.VISIBLE);
                CSVDataReader.displayChart(Graph.this, file, chartType, lineChart);
                xAxisLabel.setText(CSVDataReader.getxlabel());
                yAxisLabel.setText(CSVDataReader.getylabel());
                yAxisArrow.setVisibility(View.VISIBLE);
                yAxisLabel.setVisibility(View.VISIBLE);
                xAxisLabel.setVisibility(View.VISIBLE);
                xAxisArrow.setVisibility(View.VISIBLE);
                break;
            case "Bubble Chart":
                bubbleChart.setVisibility(View.VISIBLE);
                CSVDataReader.displayChart(Graph.this, file, chartType, bubbleChart);
                xAxisLabel.setText(CSVDataReader.getxlabel());
                yAxisLabel.setText(CSVDataReader.getylabel());
                yAxisArrow.setVisibility(View.VISIBLE);
                yAxisLabel.setVisibility(View.VISIBLE);
                xAxisLabel.setVisibility(View.VISIBLE);
                xAxisArrow.setVisibility(View.VISIBLE);
                break;
            case "Horizontal Bar Chart":
                horizontalBarChart.setVisibility(View.VISIBLE);
                CSVDataReader.displayChart(Graph.this, file, chartType, horizontalBarChart);
                xAxisLabel.setText(CSVDataReader.getxlabel());
                yAxisLabel.setText(CSVDataReader.getylabel());
                yAxisArrow.setVisibility(View.VISIBLE);
                yAxisLabel.setVisibility(View.VISIBLE);
                xAxisLabel.setVisibility(View.VISIBLE);
                xAxisArrow.setVisibility(View.VISIBLE);
                break;
            case "Scatter Chart":
                scatterChart.setVisibility(View.VISIBLE);
                CSVDataReader.displayChart(Graph.this, file, chartType, scatterChart);
                xAxisLabel.setText(CSVDataReader.getxlabel());
                yAxisLabel.setText(CSVDataReader.getylabel());
                yAxisArrow.setVisibility(View.VISIBLE);
                yAxisLabel.setVisibility(View.VISIBLE);
                xAxisLabel.setVisibility(View.VISIBLE);
                xAxisArrow.setVisibility(View.VISIBLE);
                break;
            default:
                Toast.makeText(Graph.this, "Unsupported chart type: " + chartType, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void hideAllCharts() {
        barChart.setVisibility(View.GONE);
        pieChart.setVisibility(View.GONE);
        lineChart.setVisibility(View.GONE);
        bubbleChart.setVisibility(View.GONE);
        horizontalBarChart.setVisibility(View.GONE);
        scatterChart.setVisibility(View.GONE);
    }

    private void downloadFile(String url, File outputFile, OnDownloadCompleteListener listener) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl(url);

        storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
            try {
                FileOutputStream fos = new FileOutputStream(outputFile);
                fos.write(bytes);
                fos.close();
                listener.onDownloadComplete(outputFile);
            } catch (IOException e) {
                e.printStackTrace();
                listener.onDownloadFailed(e);
            }
        }).addOnFailureListener(e -> {
            listener.onDownloadFailed(e);
        });
    }

    private void setSpinnerSelection(String chartType) {
        for (int i = 0; i < chartTypeSpinner.getCount(); i++) {
            if (chartTypeSpinner.getItemAtPosition(i).toString().equalsIgnoreCase(chartType)) {
                chartTypeSpinner.setSelection(i);
                break;
            }
        }
    }

    private interface OnDownloadCompleteListener {
        void onDownloadComplete(File file);

        void onDownloadFailed(Exception e);
    }
}
