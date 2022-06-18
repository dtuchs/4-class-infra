package com.dtuchs.libs.retrofit.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import io.qameta.allure.okhttp3.AllureOkHttp3;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ApiServiceFactory {

    private final Retrofit retrofit;

    public static class ApiServiceBuilder {

        public static final String ENABLE_ALLURE_LOGGER_PROP = "enable.allure.logging";
        private static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
        private static final FieldNamingPolicy GSON_DEFAULT_FIELD_POLICY = FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES;
        private static final PropertyNamingStrategy JACKSON_DEFAULT_FIELD_POLICY = PropertyNamingStrategy.SNAKE_CASE;

        private final String baseUrl;
        private List<Interceptor> interceptors = new ArrayList<>();
        private List<Converter.Factory> customConverters = new ArrayList<>();

        private final OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS);

        private final GsonBuilder gsonBuilder = new GsonBuilder()
                .setDateFormat(DEFAULT_DATE_FORMAT.toPattern())
                .setFieldNamingPolicy(GSON_DEFAULT_FIELD_POLICY)
                .setPrettyPrinting();

        private final ObjectMapper jacksonObjectMapper = new ObjectMapper()
                .setDateFormat(DEFAULT_DATE_FORMAT)
                .setPropertyNamingStrategy(JACKSON_DEFAULT_FIELD_POLICY)
                .enable(SerializationFeature.INDENT_OUTPUT);

        private boolean useGson = true;
        private boolean useJackson = false;
        private boolean useAllureAttaching = true;

        public ApiServiceBuilder(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public ApiServiceBuilder withInterceptors(Interceptor... interceptors) {
            this.interceptors.addAll(Arrays.asList(interceptors));
            return this;
        }

        public ApiServiceBuilder withCustomConverters(Converter.Factory... customConverters) {
            this.customConverters.addAll(Arrays.asList(customConverters));
            return this;
        }

        public ApiServiceBuilder withCustomDateFormat(@Nonnull String dateFormat) {
            this.gsonBuilder.setDateFormat(dateFormat);
            this.jacksonObjectMapper.setDateFormat(new SimpleDateFormat(dateFormat));
            return this;
        }

        public ApiServiceBuilder disableGsonConverter() {
            useGson = false;
            return this;
        }

        public ApiServiceBuilder enableGsonConverter() {
            useGson = true;
            return this;
        }

        public ApiServiceBuilder disableJacksonConverter() {
            useJackson = false;
            return this;
        }

        public ApiServiceBuilder enableJacksonConverter() {
            useJackson = true;
            return this;
        }

        public ApiServiceBuilder disableAllureAttaching() {
            useAllureAttaching = false;
            return this;
        }

        /**
         * @deprecated enabled by default, will be deleted
         */
        @Deprecated
        public ApiServiceBuilder setPrettyPrinting() {
            this.gsonBuilder.setPrettyPrinting();
            this.jacksonObjectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            return this;
        }

        /**
         * GSON
         */
        public ApiServiceBuilder withCustomFieldNamingPolicy(FieldNamingPolicy fieldNamingPolicy) {
            this.gsonBuilder.setFieldNamingPolicy(fieldNamingPolicy);
            return this;
        }

        /**
         * Jackson
         */
        public ApiServiceBuilder withCustomFieldNamingPolicy(PropertyNamingStrategy propertyNamingStrategy) {
            this.jacksonObjectMapper.setPropertyNamingStrategy(propertyNamingStrategy);
            return this;
        }

        /**
         * GSON
         */
        public ApiServiceBuilder withTypeAdapter(Type type, Object typeAdapter) {
            this.gsonBuilder.registerTypeAdapter(type, typeAdapter);
            return this;
        }

        public ApiServiceBuilder switchOffFollowRedirects() {
            this.okHttpClientBuilder.followRedirects(false);
            return this;
        }

        public ApiServiceFactory build() {
            if (useAllureAttaching && Boolean.parseBoolean(System.getProperty(ENABLE_ALLURE_LOGGER_PROP, "true"))) {
                interceptors.add(new AllureOkHttp3()
                        .setRequestTemplate("xhttp-request.ftl")
                        .setResponseTemplate("xhttp-response.ftl"));
            }
            okHttpClientBuilder.interceptors().addAll(this.interceptors);
            return new ApiServiceFactory(this);
        }
    }

    private ApiServiceFactory(ApiServiceBuilder apiServiceBuilder) {
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(apiServiceBuilder.baseUrl)
                .client(apiServiceBuilder.okHttpClientBuilder.build());

        if (apiServiceBuilder.customConverters != null && !apiServiceBuilder.customConverters.isEmpty())
            apiServiceBuilder.customConverters.forEach(builder::addConverterFactory);

        if (apiServiceBuilder.useGson)
            builder.addConverterFactory(GsonConverterFactory.create(apiServiceBuilder.gsonBuilder.create()));
        else if (apiServiceBuilder.useJackson)
            builder.addConverterFactory(JacksonConverterFactory.create(apiServiceBuilder.jacksonObjectMapper));

        retrofit = builder.build();
    }

    public <T> T getService(Class<T> service) {
        return retrofit.create(service);
    }
}
