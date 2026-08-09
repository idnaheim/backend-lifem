package com.idnaheim.lifem.config;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
    boolean success,
    int statusCode,
    String message,
    T data
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, 200, "Success", data);
    }

    public static <T> ApiResponse<T> success(int statusCode, T data) {
        return new ApiResponse<>(true, statusCode, "Success", data);
    }

    public static <T> ApiResponse<T> success(int statusCode, String message, T data) {
        return new ApiResponse<>(true, statusCode, message, data);
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(true, 201, "Resource created", data);
    }

    public static <T> ApiResponse<T> error(int statusCode, String message) {
        return new ApiResponse<>(false, statusCode, message, null);
    }

    public static <T> ApiResponse<T> notFound() {
        return new ApiResponse<>(false, 404, "Resource not found", null);
    }

    public static <T> ApiResponse<T> badRequest(String message) {
        return new ApiResponse<>(false, 400, message, null);
    }
}
