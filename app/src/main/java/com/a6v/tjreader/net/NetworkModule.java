package com.a6v.tjreader.net;

import com.google.gson.Gson;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class NetworkModule {
  public static final String NETWORK_INTERCEPTOR = "networkInterceptor";
  public static final String INTERCEPTOR = "interceptor";

  @Provides
  @Singleton
  public static TJournalAPI provideAPI(Gson gson, OkHttpClient client) {
    return new Retrofit.Builder()
      .addConverterFactory(GsonConverterFactory.create(gson))
      .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
      .client(client)
      .baseUrl("https://api.tjournal.ru/2.2/")
      .build()
      .create(TJournalAPI.class);
  }

  @Provides
  @Singleton
  public static OkHttpClient provideClient(
    @Named(INTERCEPTOR) Set<Interceptor> interceptors,
    @Named(NETWORK_INTERCEPTOR) Set<Interceptor> networkInterceptors
  )
  {
    OkHttpClient.Builder builder = new OkHttpClient.Builder()
      .readTimeout(5, TimeUnit.SECONDS)
      .connectTimeout(5, TimeUnit.SECONDS);

    for (Interceptor interceptor : interceptors) {
      builder.addInterceptor(interceptor);
    }
    for (Interceptor interceptor : networkInterceptors) {
      builder.addNetworkInterceptor(interceptor);
    }
    return builder.build();
  }
}
