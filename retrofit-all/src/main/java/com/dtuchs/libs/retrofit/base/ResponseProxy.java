package com.dtuchs.libs.retrofit.base;


import io.qameta.allure.Step;
import okhttp3.Headers;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.function.ThrowingConsumer;
import retrofit2.Response;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;

public class ResponseProxy<T> {

    private final Response<T> response;

    public ResponseProxy(Response<T> response) {
        this.response = response;
    }

    public okhttp3.Response raw() {
        return response.raw();
    }

    /**
     * HTTP status code.
     */
    public int code() {
        return response.code();
    }

    /**
     * HTTP status message or null if unknown.
     */
    public String message() {
        return response.message();
    }

    /**
     * HTTP headers.
     */
    public Headers headers() {
        return response.headers();
    }

    /**
     * Returns true if {@link #code()} is in the range [200..300).
     */
    public boolean isSuccessful() {
        return response.isSuccessful();
    }

    /**
     * The deserialized response body of a {@linkplain #isSuccessful() successful} response.
     */
    public @Nullable
    T body() {
        return response.body();
    }

    /**
     * The raw response body of an {@linkplain #isSuccessful() unsuccessful} response.
     */
    public @Nullable
    ResponseBody errorBody() {
        return response.errorBody();
    }

    @Override
    public String toString() {
        return response.toString();
    }

    /**
     * Check response code
     */
    public ResponseProxy<T> checkCode(int expectedCode) {
        return check("Check response code",
                response -> assertThat(response.code()).isEqualTo(expectedCode)
        );
    }

    /**
     * Evaluate any checks, for example:
     * response ->  assertThat(response.body()).containsExactlyInAnyOrder("1", "2")
     */
    public ResponseProxy<T> check(@Nonnull ThrowingConsumer<Response<T>> check) {
        return check("Check response", check);
    }

    /**
     * Evaluate any checks with custom allure step name, for example:
     * response -> assertThat(response.body()).containsExactlyInAnyOrder("1", "2")
     */
    public ResponseProxy<T> check(@Nonnull String checkName, @Nonnull ThrowingConsumer<Response<T>> check) {
        step(checkName, () -> check.accept(response));
        return this;
    }
}
