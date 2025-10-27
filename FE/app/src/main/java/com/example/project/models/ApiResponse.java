package com.example.project.models;

import com.google.gson.annotations.SerializedName;

public class ApiResponse<T> {
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("token")
    private String token;
    
    @SerializedName("user")
    private T user;
    
    @SerializedName("users")
    private T users;
    
    @SerializedName("count")
    private int count;
    
    @SerializedName("data")
    private T data;
    
    @SerializedName("pagination")
    private Pagination pagination;
    
    @SerializedName("errors")
    private Object errors;

    public ApiResponse() {}

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public T getUser() {
        return user;
    }

    public void setUser(T user) {
        this.user = user;
    }

    public T getUsers() {
        return users;
    }

    public void setUsers(T users) {
        this.users = users;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    public Object getErrors() {
        return errors;
    }

    public void setErrors(Object errors) {
        this.errors = errors;
    }

    public static class Pagination {
        @SerializedName("currentPage")
        private int currentPage;

        @SerializedName("totalPages")
        private int totalPages;

        @SerializedName("totalItems")
        private int totalItems;

        @SerializedName("itemsPerPage")
        private int itemsPerPage;

        @SerializedName("hasNextPage")
        private boolean hasNextPage;

        @SerializedName("hasPrevPage")
        private boolean hasPrevPage;

        public Pagination() {}

        public int getCurrentPage() {
            return currentPage;
        }

        public void setCurrentPage(int currentPage) {
            this.currentPage = currentPage;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }

        public int getTotalItems() {
            return totalItems;
        }

        public void setTotalItems(int totalItems) {
            this.totalItems = totalItems;
        }

        public int getItemsPerPage() {
            return itemsPerPage;
        }

        public void setItemsPerPage(int itemsPerPage) {
            this.itemsPerPage = itemsPerPage;
        }

        public boolean isHasNextPage() {
            return hasNextPage;
        }

        public void setHasNextPage(boolean hasNextPage) {
            this.hasNextPage = hasNextPage;
        }

        public boolean isHasPrevPage() {
            return hasPrevPage;
        }

        public void setHasPrevPage(boolean hasPrevPage) {
            this.hasPrevPage = hasPrevPage;
        }
    }
}

