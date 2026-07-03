package com.example.igngbbs;

import java.io.IOException;

public final class ApiException extends IOException {
    public final int statusCode;
    public final String errorCode;

    public ApiException(int statusCode, String errorCode, String message) {
        super(message == null || message.trim().isEmpty() ? "请求失败：" + statusCode : message);
        this.statusCode = statusCode;
        this.errorCode = errorCode == null ? "" : errorCode;
    }

    public boolean isAccessDenied() {
        return statusCode == 403 || statusCode == 404;
    }
}
