package com.example.bhelvisualizer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bhelvisualizer.R;
import com.example.bhelvisualizer.model.Report;

import java.util.ArrayList;
import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {

    private Context context;
    private List<Report> reports;
    private List<Report> reportsFull; // Full copy of original list for filtering
    private OnReportClickListener onReportClickListener;

    public ReportAdapter(Context context, List<Report> reports, OnReportClickListener onReportClickListener) {
        this.context = context;
        this.reports = reports;
        this.reportsFull = new ArrayList<>(reports); // Copy of original list
        this.onReportClickListener = onReportClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.reportmodel, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Report report = reports.get(position);
        holder.reportNameTextView.setText(report.getName());

        holder.innerCardView.setOnClickListener(v -> {
            if (onReportClickListener != null) {
                onReportClickListener.onReportClick(report);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    public void filterList(List<Report> filteredList) {
        reports = filteredList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView reportNameTextView;
        CardView innerCardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            reportNameTextView = itemView.findViewById(R.id.reportNameTextView);
            innerCardView = itemView.findViewById(R.id.cardView);
        }
    }

    public interface OnReportClickListener {
        void onReportClick(Report report);
    }
}
