package com.dtuchs.libs.retrofit.base;

import org.opentest4j.AssertionFailedError;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;

public class CommonExecutor implements Executor {

    private boolean acceptRedirectedResponse = false;

    /**
     * @deprecated use methods {@link #toBody(Call)}, {@link #toResponse(Call)}
     */
    @Override
    @Deprecated
    public <T> T execute(Call<T> call) {
        T result;
        try {
            Response<T> response = call.execute();
            if (response.isSuccessful() || (acceptRedirectedResponse && response.code() == 302)) {
                result = response.body();
            } else {
                throw new AssertionFailedError(response.errorBody().string());
            }
        } catch (IOException e) {
            throw new AssertionFailedError("Network failure: " + e.getMessage());
        }
        return result;
    }

    /**
     * @return body (if successfully request expected), else AssertionFailedError will be thrown
     */
    @Override
    public <T> T toBody(Call<T> call) {
        return execute(call);
    }

    /**
     * @return raw response
     */
    @Override
    public <T> Response<T> toResponse(Call<T> call) {
        try {
            return call.execute();
        } catch (IOException e) {
            throw new AssertionFailedError("Network failure: " + e.getMessage());
        }
    }

    /**
     * @return raw response with fluent check functionality
     */
    @Override
    public <T> VerifiableResponse<T> toVerifiableResponse(Call<T> call) {
        return new VerifiableResponse<>(toResponse(call));
    }

    @Override
    public CommonExecutor acceptRedirectedResponse(boolean redirectedResponse) {
        acceptRedirectedResponse = redirectedResponse;
        return this;
    }
}
