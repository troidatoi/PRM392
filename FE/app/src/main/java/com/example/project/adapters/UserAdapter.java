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
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {
        private CardView cardViewUser;
        private ImageView imgAvatar, imgStatus;
        private TextView tvUsername, tvEmail, tvRole, tvStatus, tvLastLogin, tvCreatedAt;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            cardViewUser = itemView.findViewById(R.id.cardViewUser);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            imgStatus = itemView.findViewById(R.id.imgStatus);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvRole = itemView.findViewById(R.id.tvRole);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvLastLogin = itemView.findViewById(R.id.tvLastLogin);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
        }

        public void bind(User user) {
            // Username
            tvUsername.setText(user.getUsername());

            // Email
            tvEmail.setText(user.getEmail());

            // Role with color coding
            String role = user.getRole();
            tvRole.setText(getRoleDisplayName(role));
            setRoleColor(tvRole, role);

            // Status
            boolean isActive = user.isActive();
            tvStatus.setText(isActive ? "Hoạt động" : "Tạm khóa");
            tvStatus.setTextColor(isActive ? 
                context.getResources().getColor(android.R.color.holo_green_dark) : 
                context.getResources().getColor(android.R.color.holo_red_dark));
            
            imgStatus.setImageResource(isActive ? R.drawable.ic_home : R.drawable.ic_search);
            imgStatus.setColorFilter(isActive ? 
                context.getResources().getColor(android.R.color.holo_green_dark) : 
                context.getResources().getColor(android.R.color.holo_red_dark));

            // Last login
            if (user.getLastLogin() != null && !user.getLastLogin().isEmpty()) {
                tvLastLogin.setText("Đăng nhập: " + formatDate(user.getLastLogin()));
            } else {
                tvLastLogin.setText("Chưa đăng nhập");
            }

            // Created date
            if (user.getCreatedAt() != null && !user.getCreatedAt().isEmpty()) {
                tvCreatedAt.setText("Tạo: " + formatDate(user.getCreatedAt()));
            } else {
                tvCreatedAt.setText("");
            }

            // Avatar placeholder
            imgAvatar.setImageResource(R.drawable.ic_account);
            imgAvatar.setColorFilter(context.getResources().getColor(android.R.color.darker_gray));

            // Click listener for user detail
            cardViewUser.setOnClickListener(v -> {
                if (onUserClickListener != null) {
                    onUserClickListener.onUserClick(user);
                }
            });
        }

        private String getRoleDisplayName(String role) {
            switch (role) {
                case "admin":
                    return "Quản trị viên";
                case "staff":
                    return "Nhân viên";
                case "customer":
                    return "Khách hàng";
                default:
                    return role;
            }
        }

        private void setRoleColor(TextView textView, String role) {
            int color;
            switch (role) {
                case "admin":
                    color = context.getResources().getColor(android.R.color.holo_red_dark);
                    break;
                case "staff":
                    color = context.getResources().getColor(android.R.color.holo_blue_dark);
                    break;
                case "customer":
                    color = context.getResources().getColor(android.R.color.holo_green_dark);
                    break;
                default:
                    color = context.getResources().getColor(android.R.color.darker_gray);
                    break;
            }
            textView.setTextColor(color);
        }

        private String formatDate(String dateString) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                Date date = inputFormat.parse(dateString);
                return outputFormat.format(date);
            } catch (Exception e) {
                return dateString;
            }
        }
    }
}
