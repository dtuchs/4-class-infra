package com.dtuchs.libs.retrofit.base;

import io.qameta.allure.Allure;
import retrofit2.Call;
import retrofit2.Response;

public class AllureCallExecutor implements Executor {

    private Executor callExecutor = new CallExecutor();

    @Override
    @Deprecated
    public <T> T execute(Call<T> call) {
        return Allure.step(
                "Send " + call.request().method() + " request to: " + call.request().url() + " & extract response body",
                context -> {
                    return callExecutor.execute(call);
                });
    }

    @Override
    public <T> T toBody(Call<T> call) {
        return this.execute(call);
    }

    @Override
    public <T> Response<T> toResponse(Call<T> call) {
        return Allure.step(
                "Send " + call.request().method() + " request to: " + call.request().url(),
                context -> {
                    return callExecutor.toResponse(call);
                });
    }

    @Override
    public <T> ResponseProxy<T> toResponseProxy(Call<T> call) {
        return new ResponseProxy<>(this.toResponse(call));
    }

    @Override
    public AllureCallExecutor acceptRedirectedResponse(boolean redirectedResponse) {
        callExecutor.acceptRedirectedResponse(redirectedResponse);
        return this;
    }
}
