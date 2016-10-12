package com.tinderapp.model;

import android.util.Log;

import com.tinderapp.BuildConfig;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TinderRetrofit {
    private final static String TAG = TinderRetrofit.class.getName();
    public static TinderAPI tinderTokenAPI;
    public static TinderAPI tinderRawAPI;
    public static String userToken;

    public static TinderAPI getRawInstance() {
        if (tinderRawAPI == null) {

            OkHttpClient okClientLoggerInterceptor = new OkHttpClient.Builder()
                    .addInterceptor(
                            new Interceptor() {
                                @Override
                                public okhttp3.Response intercept(Interceptor.Chain chain) throws IOException {
                                    Request original = chain.request();
                                    Request request = original.newBuilder()
                                            .header("Content-Type", "application/json")
                                            .method(original.method(), original.body())
                                            .build();

                                    Log.i(TAG, "Url -> " + request.url());
                                    return chain.proceed(request);
                                }
                            })
                    .build();


            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okClientLoggerInterceptor)
                    .build();

            tinderRawAPI = retrofit.create(TinderAPI.class);
        }

        return tinderRawAPI;
    }

    //Get recommendations wouldn't work with content type
    public static TinderAPI getTokenInstance(String token) {
        userToken = token;
        if (tinderTokenAPI == null) {

            OkHttpClient okClientLoggerInterceptor = new OkHttpClient.Builder()
                    .addInterceptor(
                            new Interceptor() {
                                @Override
                                public okhttp3.Response intercept(Interceptor.Chain chain) throws IOException {
                                    Request original = chain.request();
                                    Request request = original.newBuilder()
                                            .header("X-Auth-Token", userToken)
                                            .method(original.method(), original.body())
                                            .build();

                                    Log.i(TAG, "Url -> " + request.url());
                                    Log.i(TAG, "Token -> " + userToken);
                                    return chain.proceed(request);
                                }
                            })
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okClientLoggerInterceptor)
                    .build();

            tinderTokenAPI = retrofit.create(TinderAPI.class);
        }

        return tinderTokenAPI;
    }

    //Send message endpoint needs both token and content type
    public static TinderAPI getTokenInstanceWithContentType(String token) {
        userToken = token;
        if (tinderTokenAPI == null) {
            OkHttpClient okClientLoggerInterceptor = new OkHttpClient.Builder()
                    .addInterceptor(
                            new Interceptor() {
                                @Override
                                public okhttp3.Response intercept(Interceptor.Chain chain) throws IOException {
                                    Request original = chain.request();
                                    Request request = original.newBuilder()
                                            .header("X-Auth-Token", userToken)
                                            .header("Content-type", "application/json")
                                            .method(original.method(), original.body())
                                            .build();

                                    Log.i(TAG, "Url -> " + request.url());
                                    Log.i(TAG, "Token -> " + userToken);
                                    return chain.proceed(request);
                                }
                            })
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okClientLoggerInterceptor)
                    .build();

            tinderTokenAPI = retrofit.create(TinderAPI.class);
        }

        return tinderTokenAPI;
    }
}
