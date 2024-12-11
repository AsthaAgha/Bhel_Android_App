package com.example.bhelvisualizer.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bhelvisualizer.R;
import com.example.bhelvisualizer.model.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class CrudAdapter extends RecyclerView.Adapter<CrudAdapter.UserViewHolder> {

    private Context mContext;
    private List<User> mUserList;
    private DatabaseReference mDatabase;

    public CrudAdapter(Context context, List<User> userList) {
        this.mContext = context;
        this.mUserList = userList;
        this.mDatabase = FirebaseDatabase.getInstance().getReference().child("User");
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.crud_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = mUserList.get(position);
        holder.userName.setText(user.getName());
        holder.userEmail.setText(user.getEmail());
        holder.userPassword.setText("*****");

        holder.editButton.setOnClickListener(v -> showEditDialog(user));
        holder.deleteButton.setOnClickListener(v -> showDeleteDialog(user));
    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }

    public void setUserList(List<User> userList) {
        mUserList = userList;
        notifyDataSetChanged();
    }

    private void showEditDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Edit User");

        View viewInflated = LayoutInflater.from(mContext).inflate(R.layout.edit_user_dialogue, null, false);
        EditText editName = viewInflated.findViewById(R.id.editName);
        EditText editEmail = viewInflated.findViewById(R.id.editEmail);
        EditText editPassword = viewInflated.findViewById(R.id.editPassword);

        editName.setText(user.getName());
        editEmail.setText(user.getEmail());
        editPassword.setText(user.getPassword());

        builder.setView(viewInflated);
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            // Do nothing here because we override this button later to change the close behaviour.
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String newName = editName.getText().toString();
            String newEmail = editEmail.getText().toString();
            String newPassword = editPassword.getText().toString();

            if (newPassword.length() < 6) {
                editPassword.setError("Password must be at least 6 characters long");
                return;
            }

            user.setName(newName);
            user.setEmail(newEmail);
            user.setPassword(newPassword);

            mDatabase.child(user.getName()).child("Name").setValue(user.getName());
            mDatabase.child(user.getName()).child("Email").setValue(user.getEmail());
            mDatabase.child(user.getName()).child("Password").setValue(user.getPassword());
            mDatabase.child(user.getName()).child("Role").setValue(user.getRole());
            mDatabase.child(user.getName()).child("Priority").setValue(user.getPriority())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(mContext, "User updated", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(mContext, "Failed to update user", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void showDeleteDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Delete User");
        builder.setMessage("Are you sure you want to delete this user?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            mDatabase.child(user.getName()).removeValue()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(mContext, "User deleted", Toast.LENGTH_SHORT).show();
                            mUserList.remove(user);
                            notifyDataSetChanged();
                        } else {
                            Toast.makeText(mContext, "Failed to delete user", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        TextView userName, userEmail, userPassword;
        ImageButton editButton, deleteButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            userEmail = itemView.findViewById(R.id.userEmail);
            userPassword = itemView.findViewById(R.id.userPassword);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
