package com.dtuchs.libs.retrofit.base;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.PUT;


public interface TestService {

    @POST("/test")
    Call<ResponseBody> executeTest(@Body TestData testData);

    @POST("/nogson")
    Call<ResponseBody> executeNoGson(@Body RequestBody data);

    @POST("/redirect")
    Call<ResponseBody> redirect(@Body TestData testData);

    @PUT("/adapter")
    Call<TestData> typeAdapter(@Body TestData testData);


}
