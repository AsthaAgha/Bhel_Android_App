package com.example.bhelvisualizer.adapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bhelvisualizer.R;
import com.example.bhelvisualizer.model.Files;
import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

    private List<Files> files;
    private OnItemClickListener listener;

    public FileAdapter(List<Files> files, OnItemClickListener listener) {
        this.files = files;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        Files file = files.get(position);
        holder.bind(file, listener);
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public void filterList(List<Files> filteredFiles) {
        files.clear();
        files.addAll(filteredFiles);
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        View onCreateView(LayoutInflater inflater, ViewGroup container,
                          Bundle savedInstanceState);

        void onItemClick(int position);
    }

    public static class FileViewHolder extends RecyclerView.ViewHolder {

        private TextView fileNameTextView;
        private TextView descriptionTextView;
        private TextView userNameTextView;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            fileNameTextView = itemView.findViewById(R.id.fileNameTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            userNameTextView = itemView.findViewById(R.id.userNameTextView);
        }

        public void bind(Files file, OnItemClickListener listener) {
            fileNameTextView.setText(file.getFileName());
            descriptionTextView.setText(file.getDescription());
            userNameTextView.setText("-- by " + file.getUsername());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(getAdapterPosition());
                }
            });
        }
    }
}
