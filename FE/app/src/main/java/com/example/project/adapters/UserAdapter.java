package com.example.project.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;
import com.example.project.models.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private Context context;
    private OnUserClickListener onUserClickListener;

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public UserAdapter(List<User> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    public void setOnUserClickListener(OnUserClickListener listener) {
        this.onUserClickListener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        android.util.Log.d("UserAdapter", "Binding user at position " + position + ": " + (user.getUsername() != null ? user.getUsername() : user.getEmail()));
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        int count = userList.size();
        android.util.Log.d("UserAdapter", "getItemCount: " + count);
        return count;
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {
        private CardView cardViewUser;
        private ImageView imgAvatar;
        private View statusDot;
        private TextView tvUsername, tvEmail, tvRole, tvStatus;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            cardViewUser = itemView.findViewById(R.id.cardViewUser);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            statusDot = itemView.findViewById(R.id.statusDot);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvRole = itemView.findViewById(R.id.tvRole);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }

        public void bind(User user) {
            // Username
            if (tvUsername != null) {
                tvUsername.setText(user.getUsername() != null ? user.getUsername() : "Unknown");
            }

            // Email
            if (tvEmail != null) {
                tvEmail.setText(user.getEmail() != null ? user.getEmail() : "No email");
            }

            // Role with color coding
            String role = user.getRole();
            if (tvRole != null) {
                tvRole.setText(getRoleDisplayName(role));
                // Set role badge background color based on role
                if (role != null) {
                    switch (role.toLowerCase()) {
                        case "admin":
                        case "staff":
                            tvRole.setTextColor(0xFF0284C7); // Blue
                            break;
                        case "customer":
                            tvRole.setTextColor(0xFF0284C7); // Blue
                            break;
                        default:
                            tvRole.setTextColor(0xFF64748B); // Gray
                            break;
                    }
                }
            }

            // Status
            boolean isActive = user.isActive();
            if (tvStatus != null) {
                tvStatus.setText(isActive ? "Active" : "Inactive");
                tvStatus.setTextColor(isActive ? 0xFF059669 : 0xFFDC2626); // Green or Red
            }
            
            // Status dot color
            if (statusDot != null) {
                statusDot.setBackgroundResource(isActive ? R.drawable.circle_green : R.drawable.circle_red);
            }

            // Avatar placeholder - just show icon, no need to set color as it's in the CardView
            if (imgAvatar != null) {
                imgAvatar.setImageResource(R.drawable.ic_account_circle);
            }

            // Click listener for user detail
            if (cardViewUser != null) {
                cardViewUser.setOnClickListener(v -> {
                    if (onUserClickListener != null) {
                        onUserClickListener.onUserClick(user);
                    }
                });
            }
        }

        private String getRoleDisplayName(String role) {
            if (role == null) return "Customer";
            
            switch (role.toLowerCase()) {
                case "admin":
                    return "Admin";
                case "staff":
                    return "Staff";
                case "customer":
                    return "Customer";
                default:
                    return role;
            }
        }
    }
}
