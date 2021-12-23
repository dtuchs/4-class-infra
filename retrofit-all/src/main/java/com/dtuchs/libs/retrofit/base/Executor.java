package com.dtuchs.libs.retrofit.base;

import retrofit2.Call;
import retrofit2.Response;

public interface Executor {
    /**
     * @deprecated use methods {@link #toBody(Call <T>)}, {@link #toResponse(Call<T>)}
     */
    @Deprecated
    <T> T execute(Call<T> call);

    /**
     * @return body (if successfully request expected), else AssertionFailedError will be thrown
     */
    <T> T toBody(Call<T> call);

    /**
     * @return raw response
     */
    <T> Response<T> toResponse(Call<T> call);

    /**
     * @return raw response with fluent check() method;
     */
    <T> ResponseProxy<T> toResponseProxy(Call<T> call);

    /**
     * Accept 302 HTTP code as successful request. Used only in {@link #toBody(Call <T>)}, {@link #execute(Call<T>)} methods
     * by default - false
     */
    <A extends Executor> A acceptRedirectedResponse(boolean redirectedResponse);
}
