package me.jeybi.uztaxi.network

import me.jeybi.uztaxi.utils.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class RetrofitHelper {

    companion object {
        fun retrofitInstance(): Retrofit {
            val interceptor =  HttpLoggingInterceptor()
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            var client =
                OkHttpClient.Builder().connectTimeout(20,TimeUnit.SECONDS) .addInterceptor(interceptor).build()


            return Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        }

        fun apiService(): ApiClient {
            return retrofitInstance().create(ApiClient::class.java)
        }
    }

}