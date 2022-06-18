package com.dtuchs.libs.retrofit.base;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.gson.FieldNamingPolicy;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import retrofit2.Call;
import retrofit2.Response;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.temporaryRedirect;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ApiServiceFactoryTest {

    static WireMockServer wireMockServer = new WireMockServer(wireMockConfig().dynamicPort().dynamicHttpsPort());
    Executor executor = new CommonExecutor();

    @BeforeAll
    static void setUp() {
        wireMockServer.start();
        setUpStubs();
    }

    static void setUpStubs() {
        wireMockServer.stubFor(post(urlEqualTo("/test"))
                .willReturn(aResponse().withStatus(201)));

        wireMockServer.stubFor(get(urlEqualTo("/test"))
                .willReturn(aResponse().withStatus(200)));

        wireMockServer.stubFor(post(urlEqualTo("/nogson"))
                .willReturn(aResponse()
                        .withBody("qwerty")
                        .withStatus(200)));

        wireMockServer.stubFor(post(urlEqualTo("/redirect"))
                .willReturn(temporaryRedirect("/test")));

        wireMockServer.stubFor(put(urlEqualTo("/adapter"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withBody("{\"date_time\": \"2010-11-20\"}")));
    }

    @Test
    void defaultApiServiceFactoryTest() throws Exception {
        ApiServiceFactory apiServiceFactory = new ApiServiceFactory
                .ApiServiceBuilder("http://localhost:" + wireMockServer.port())
                .build();

        Call<ResponseBody> call = apiServiceFactory.getService(TestService.class)
                .executeTest(new TestData(new SimpleDateFormat("dd/MM/yyyy").parse("20/11/2017")));

        executor.toVerifiableResponse(call)
                .checkCode(201);

        wireMockServer.verify(postRequestedFor(urlEqualTo("/test"))
                .withRequestBody(equalToJson("{\"date_time\": \"20/11/2017\"}")));
    }

    @Test
    void apiServiceFactoryWithCustomDateFormatterTest() throws Exception {
        ApiServiceFactory apiServiceFactory = new ApiServiceFactory
                .ApiServiceBuilder("http://localhost:" + wireMockServer.port())
                .withCustomDateFormat("yyyy-MM-dd")
                .build();

        Call<ResponseBody> call = apiServiceFactory.getService(TestService.class)
                .executeTest(new TestData(new SimpleDateFormat("dd/MM/yyyy").parse("20/11/2017")));

        executor.toVerifiableResponse(call)
                .checkCode(201);

        wireMockServer.verify(postRequestedFor(urlEqualTo("/test"))
                .withRequestBody(equalToJson("{\"date_time\": \"2017-11-20\"}")));
    }

    @Test
    void apiServiceFactoryWithCustomFieldNamingPolicyTest() throws Exception {
        ApiServiceFactory apiServiceFactory = new ApiServiceFactory
                .ApiServiceBuilder("http://localhost:" + wireMockServer.port())
                .withCustomFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .build();

        Call<ResponseBody> call = apiServiceFactory.getService(TestService.class)
                .executeTest(new TestData(new SimpleDateFormat("dd/MM/yyyy").parse("20/11/2017")));

        executor.toVerifiableResponse(call)
                .checkCode(201);

        wireMockServer.verify(postRequestedFor(urlEqualTo("/test"))
                .withRequestBody(equalToJson("{\"DateTime\": \"20/11/2017\"}")));
    }

    @Test
    void apiServiceFactoryWithDisableGsonTest() {
        ApiServiceFactory apiServiceFactory = new ApiServiceFactory
                .ApiServiceBuilder("http://localhost:" + wireMockServer.port())
                .disableGsonConverter()
                .build();

        Call<ResponseBody> call = apiServiceFactory.getService(TestService.class)
                .executeNoGson(RequestBody.create(MediaType.parse("text/html"), "123"));

        executor.toVerifiableResponse(call)
                .checkCode(200)
                .check(response -> assertThat(response.body()).isNotNull())
                .check(response -> assertThat(response.body().string()).isEqualTo("qwerty"));
    }

    @Test
    void apiServiceFactoryWithSwitchedOffFollowRedirectsTest() throws Exception {
        ApiServiceFactory apiServiceFactory = new ApiServiceFactory
                .ApiServiceBuilder("http://localhost:" + wireMockServer.port())
                .switchOffFollowRedirects()
                .build();

        Call<ResponseBody> call = apiServiceFactory.getService(TestService.class)
                .redirect(new TestData(new SimpleDateFormat("dd/MM/yyyy").parse("20/11/2017")));

        executor.toVerifiableResponse(call)
                .checkCode(302);

        wireMockServer.verify(postRequestedFor(urlEqualTo("/redirect")));
    }

    @Test
    void apiServiceFactoryWithSwitchedOnFollowRedirectsTest() throws Exception {
        ApiServiceFactory apiServiceFactory = new ApiServiceFactory
                .ApiServiceBuilder("http://localhost:" + wireMockServer.port())
                .build();

        Call<ResponseBody> call = apiServiceFactory.getService(TestService.class)
                .redirect(new TestData(new SimpleDateFormat("dd/MM/yyyy").parse("20/11/2017")));

        executor.toVerifiableResponse(call)
                .checkCode(200);

        wireMockServer.verify(postRequestedFor(urlEqualTo("/redirect")));
        wireMockServer.verify(getRequestedFor(urlEqualTo("/test")));
    }

    @Test
    void apiServiceFactoryWithTypeAdapterTest() throws Exception {
        ApiServiceFactory apiServiceFactory = new ApiServiceFactory
                .ApiServiceBuilder("http://localhost:" + wireMockServer.port())
                .withTypeAdapter(Date.class, new DateSerializer())
                .build();

        Call<TestData> call = apiServiceFactory.getService(TestService.class)
                .typeAdapter(new TestData(new SimpleDateFormat("dd/MM/yyyy").parse("20/11/2017")));

        final Date expectedDate = new SimpleDateFormat("dd/MM/yyyy").parse("20/11/2010");

        //check response deserialization
        executor.toVerifiableResponse(call)
                .checkCode(201)
                        .check(response -> assertThat(response.body()).isNotNull())
                        .check(response -> assertThat(response.body().getDateTime()).isEqualTo(expectedDate));

        // check request serialization
        wireMockServer.verify(putRequestedFor(urlEqualTo("/adapter"))
                .withRequestBody(equalToJson("{\"date_time\": \"11/20/2017\"}")));
    }

    @AfterAll
    static void tearDown() {
        wireMockServer.stop();
    }
}