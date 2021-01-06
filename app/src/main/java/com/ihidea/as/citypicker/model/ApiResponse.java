package com.ihidea.as.citypicker.model;

import android.support.annotation.AnyThread;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiResponse<T> {

    public final static int FAIL = 0;
    public final static int SUCCESS = 1;

    public int state;
    public T data;
    public String message;

    @IntDef({SUCCESS, FAIL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ResponseState {}

    public ApiResponse(@ResponseState int state, T data, String message) {
        this.state = state;
        this.data = data;
        this.message = message;
    }

    @NonNull
    @Override
    public String toString() {
        return "ApiResponse{" +
                "state=" + state +
                ", data=" + data +
                ", message='" + message + '\'' +
                '}';
    }

    public boolean isSuccess() {
        return state == SUCCESS;
    }

    public boolean isFail() {
        return state == FAIL;
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<T>(SUCCESS, data, message);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<T>(SUCCESS, data, null);
    }

    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<T>(FAIL, null, message);
    }

    public static <T> ApiResponse<T> create(Throwable error) {
        String errorMessage = error.getMessage();
        return ApiResponse.fail(errorMessage);
    }

    public static <T> ApiResponse<T> create(Response<T> response) {
        if (response.isSuccessful()) {
            T body = response.body();
            if (body == null || response.code() == 204) {
                return ApiResponse.success(null, "empty");
            } else {
                return ApiResponse.success(body, null);
            }
        } else {
            String errorMsg = null;
            ResponseBody errorBody = response.errorBody();
            if (errorBody != null) {
                try {
                    errorMsg = errorBody.string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (errorMsg == null || errorMsg.length() == 0) {
                errorMsg = response.message();
            }
            if (errorMsg.length() == 0) {
                errorMsg = "unknown error";
            }
            return ApiResponse.fail(errorMsg);
        }
    }

    @NonNull
    public static <T> ApiResponse<T> parseResponseResult(Response<ResultDto<T>> response) {
        if (response.isSuccessful()) {
            ResultDto<T> body = response.body();
            if (body == null || response.code() == 204) {
                return ApiResponse.success(null, "empty");
            } else {
                if (body.getCode() == 0) {
                    return ApiResponse.success(body.getData(), body.getMsg());
                } else {
                    return ApiResponse.fail(body.toString());
                }
            }
        } else {
            String errorMsg = null;
            ResponseBody errorBody = response.errorBody();
            if (errorBody != null) {
                try {
                    errorMsg = errorBody.string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (errorMsg == null || errorMsg.length() == 0) {
                errorMsg = response.message();
            }
            if (errorMsg.length() == 0) {
                errorMsg = "unknown error";
            }
            return ApiResponse.fail(errorMsg);
        }
    }

    @NonNull
    @WorkerThread
    public static <T> ApiResponse<T> execute(Call<ResultDto<T>> call) {
        String errMsg;
        try {
            Response<ResultDto<T>> response = call.execute();
            return parseResponseResult(response);
        } catch (IOException e) {
            errMsg = e.getMessage();
            e.printStackTrace();
        }
        return ApiResponse.fail(errMsg);
    }

    @AnyThread
    public static <T> void enqueue(Call<ResultDto<T>> call, @NonNull ApiCallback<T> callback) {
        call.enqueue(new Callback<ResultDto<T>>() {
            @Override
            public void onResponse(@NonNull Call<ResultDto<T>> call, @NonNull Response<ResultDto<T>> response) {
                ApiResponse<T> apiResponse = parseResponseResult(response);
                callback.onResponse(apiResponse);
            }

            @Override
            public void onFailure(@NonNull Call<ResultDto<T>> call, @NonNull Throwable t) {
                callback.onResponse(ApiResponse.fail(t.getMessage()));
            }
        });
    }

    public interface ApiCallback<T> {
        void onResponse(ApiResponse<T> apiResponse);
    }
}
