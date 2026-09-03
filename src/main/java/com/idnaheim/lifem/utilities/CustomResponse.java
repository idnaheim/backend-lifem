package com.idnaheim.lifem.utilities;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CustomResponse<T>(
    boolean success,
    int statusCode,
    String message,
    T data
) {
    public static <T> CustomResponse<T> success(T data) {
        return new CustomResponse<>(true, 200, "Success", data);
    }

    public static <T> CustomResponse<T> success(int statusCode, T data) {
        return new CustomResponse<>(true, statusCode, "Success", data);
    }

    public static <T> CustomResponse<T> success(int statusCode, String message, T data) {
        return new CustomResponse<>(true, statusCode, message, data);
    }

    public static <T> CustomResponse<T> created(T data) {
        return new CustomResponse<>(true, 201, "Resource created", data);
    }

    public static <T> CustomResponse<T> error(int statusCode, String message) {
        return new CustomResponse<>(false, statusCode, message, null);
    }

    public static <T> CustomResponse<T> notFound() {
        return new CustomResponse<>(false, 404, "Resource not found", null);
    }

    public static <T> CustomResponse<T> badRequest(String message) {
        return new CustomResponse<>(false, 400, message, null);
    }
}
