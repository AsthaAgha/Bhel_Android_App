package com.example.bhelvisualizer;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.BubbleChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BubbleData;
import com.github.mikephil.charting.data.BubbleDataSet;
import com.github.mikephil.charting.data.BubbleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVDataReader {

    private static final String TAG = "CSVDataReader";
    private static String xlabel = "";
    private static String ylabel = "";

    // Interface for callback on data loading
    public interface ChartDisplayListener {
        void onDataLoaded(List<? extends Entry> entries, String xlabel, String ylabel);
        void onDataNotAvailable();
    }

    // Method to read data from CSV file asynchronously
    public static void readDataFromCSV(File csvFile, String chartType, ChartDisplayListener listener) {
        List<Entry> entries = new ArrayList<>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(csvFile));
            String line;
            int index = 0;

            // Read the first line as headers
            if ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length >= 2) {
                    xlabel = values[0];
                    ylabel = values[1];
                } else {
                    Log.e(TAG, "Invalid header format in CSV file");
                    listener.onDataNotAvailable();
                    return;
                }
            } else {
                Log.e(TAG, "CSV file is empty");
                listener.onDataNotAvailable();
                return;
            }

            // Read the rest of the lines as data
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length >= 2) {
                    try {
                        double xValue = Double.parseDouble(values[0]);
                        double yValue = Double.parseDouble(values[1]);

                        Entry entry;

                        switch (chartType.toLowerCase()) {
                            case "bar chart":
                            case "horizontal bar chart":
                                entry = new BarEntry((float) xValue, (float) yValue, index);
                                break;
                            case "pie chart":
                                entry = new PieEntry((float) yValue, String.valueOf(xValue));
                                break;
                            case "bubble chart":
                                entry = new BubbleEntry((float) xValue, (float) yValue, 1f); // Assuming size is 1 for simplicity
                                break;
                            case "line chart":
                            case "scatter chart":
                            default:
                                entry = new Entry((float) xValue, (float) yValue);
                                break;
                        }

                        entries.add(entry);
                        index++;
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Error parsing double values from CSV file at line: " + line, e);
                        listener.onDataNotAvailable();
                        return;
                    }
                } else {
                    Log.e(TAG, "Invalid data format in CSV file at line: " + line);
                    listener.onDataNotAvailable();
                    return;
                }
            }
            reader.close();

            if (!entries.isEmpty()) {
                listener.onDataLoaded(entries, xlabel, ylabel);
            } else {
                listener.onDataNotAvailable();
            }

        } catch (IOException e) {
            Log.e(TAG, "Error reading CSV file", e);
            listener.onDataNotAvailable();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing BufferedReader", e);
                }
            }
        }
    }

    // Method to display a bar chart
    private static void displayBarChart(Context context, List<BarEntry> entries, String label, BarChart barChart) {
        if (entries.isEmpty()) {
            Log.e(TAG, "No data available for bar chart");
            return;
        }

        BarDataSet dataSet = new BarDataSet(entries, label);
        dataSet.setColors(Color.parseColor("#030333"));
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(8f);
        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.setPinchZoom(true);
        barChart.setFitBars(true);
        barChart.getDescription().setText("");
        barChart.animateX(2000);
        barChart.invalidate(); // Refresh the chart
    }

    // Method to display a pie chart
    private static void displayPieChart(Context context, List<PieEntry> entries, String label, PieChart pieChart) {
        if (entries.isEmpty()) {
            Log.e(TAG, "No data available for pie chart");
            return;
        }

        // Retrieve custom colors array from resources
        int[] customColors = context.getResources().getIntArray(R.array.custom_pie_chart_colors);

        // Create a list to hold the colors to be used
        ArrayList<Integer> colors = new ArrayList<>();
        for (int color : customColors) {
            colors.add(color);
        }

        // Adjust number of colors based on number of entries
        while (colors.size() < entries.size()) {
            // If the number of entries exceeds the number of custom colors, loop through colors
            for (int color : customColors) {
                colors.add(color);
                if (colors.size() >= entries.size()) {
                    break;
                }
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, label);
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(16f);
        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.invalidate(); // Refresh the chart
    }


    // Method to display a line chart
    private static void displayLineChart(Context context, List<Entry> entries, String label, LineChart lineChart) {
        if (entries.isEmpty()) {
            Log.e(TAG, "No data available for line chart");
            return;
        }

        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColors(Color.parseColor("#030333"));
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(16f);
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.getDescription().setText("");
        lineChart.animateX(2000);
        lineChart.setPinchZoom(true);
        lineChart.invalidate(); // Refresh the chart
    }

    // Method to display a bubble chart
    private static void displayBubbleChart(Context context, List<BubbleEntry> entries, String label, BubbleChart bubbleChart) {
        if (entries.isEmpty()) {
            Log.e(TAG, "No data available for bubble chart");
            return;
        }

        BubbleDataSet dataSet = new BubbleDataSet(entries, label);
        dataSet.setColors(Color.parseColor("#030333"));
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(10f);
        BubbleData bubbleData = new BubbleData(dataSet);
        bubbleChart.setData(bubbleData);
        bubbleChart.setPinchZoom(true);
        bubbleChart.getDescription().setText("");
        bubbleChart.animateXY(2000, 2000);
        bubbleChart.invalidate(); // Refresh the chart
    }

    // Method to display a horizontal bar chart
    private static void displayHorizontalBarChart(Context context, List<BarEntry> entries, String label, HorizontalBarChart horizontalBarChart) {
        if (entries.isEmpty()) {
            Log.e(TAG, "No data available for horizontal bar chart");
            return;
        }

        BarDataSet dataSet = new BarDataSet(entries, label);
        dataSet.setColors(Color.parseColor("#030333"));
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(16f);
        BarData barData = new BarData(dataSet);
        horizontalBarChart.setData(barData);
        horizontalBarChart.getDescription().setText("");
        horizontalBarChart.animateX(2000);
        horizontalBarChart.setPinchZoom(true);
        horizontalBarChart.invalidate(); // Refresh the chart
    }

    // Method to display a scatter chart
    private static void displayScatterChart(Context context, List<Entry> entries, String label, ScatterChart scatterChart) {
        if (entries.isEmpty()) {
            Log.e(TAG, "No data available for scatter chart");
            return;
        }

        ScatterDataSet dataSet = new ScatterDataSet(entries, label);
        dataSet.setColors(Color.parseColor("#030333"));
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(16f);
        ScatterData scatterData = new ScatterData(dataSet);
        scatterChart.setData(scatterData);
        scatterChart.setPinchZoom(true);
        scatterChart.getDescription().setText("");
        scatterChart.animateXY(2000, 2000);
        scatterChart.invalidate(); // Refresh the chart
    }

    // Method to display a chart based on the specified type
    public static void displayChart(Context context, File csvFile, String chartType, View chartView) {
        ChartDisplayListener listener = new ChartDisplayListener() {

            @Override
            public void onDataLoaded(List<? extends Entry> entries, String xlabel, String ylabel) {
                if (chartView instanceof HorizontalBarChart && chartType.equalsIgnoreCase("Horizontal Bar Chart")) {
                    displayHorizontalBarChart(context, (List<BarEntry>) entries, ylabel, (HorizontalBarChart) chartView);
                    chartView.setVisibility(View.VISIBLE);
                } else if (chartView instanceof BarChart) {
                    displayBarChart(context, (List<BarEntry>) entries, ylabel, (BarChart) chartView);
                    chartView.setVisibility(View.VISIBLE);
                } else if (chartView instanceof PieChart) {
                    displayPieChart(context, (List<PieEntry>) entries, xlabel, (PieChart) chartView);
                    chartView.setVisibility(View.VISIBLE);
                } else if (chartView instanceof LineChart) {
                    displayLineChart(context, (List<Entry>) entries, ylabel, (LineChart) chartView);
                    chartView.setVisibility(View.VISIBLE);
                } else if (chartView instanceof BubbleChart) {
                    displayBubbleChart(context, (List<BubbleEntry>) entries, ylabel, (BubbleChart) chartView);
                    chartView.setVisibility(View.VISIBLE);
                } else if (chartView instanceof ScatterChart) {
                    displayScatterChart(context, (List<Entry>) entries, ylabel, (ScatterChart) chartView);
                    chartView.setVisibility(View.VISIBLE);
                } else {
                    throw new IllegalArgumentException("Unsupported chart type or mismatch");
                }
            }

            @Override
            public void onDataNotAvailable() {
                // Handle case where data couldn't be loaded or was empty
                Log.e(TAG, "Failed to load or empty data for chart type: " + chartType);
                Toast.makeText(context, "Failed to load or empty data for chart type: " + chartType, Toast.LENGTH_SHORT).show();
                chartView.setVisibility(View.GONE); // Hide the chart view if data loading fails
            }
        };
        readDataFromCSV(csvFile, chartType, listener);
    }

    public static String getxlabel() {
        return xlabel;
    }

    public static String getylabel() {
        return ylabel;
    }
}
