package net.nmtss.mp.api;

import android.app.Application;
import android.content.Context;

import net.nmtss.mp.views.R;

import java.util.List;


import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiUtils extends Application {
    private static Retrofit retrofit = null;
    public MpEndpoint mpService;

   // public static final String BASE_URL = "http://192.168.20.110/demo/";


    public  static MpEndpoint getMpEndpoint(Context c, String BASE_URL){
        return  getClient(BASE_URL, c).create(MpEndpoint.class);
    }




    public static Retrofit getClient(String url, Context c) {
        if (retrofit == null) {

            OkHttpClient client;
            OkHttpClient.Builder builder = new OkHttpClient.Builder();

            builder.addInterceptor(new AddCookiesInterceptor(c)); // VERY VERY IMPORTANT
            builder.addInterceptor(new ReceivedCookiesInterceptor(c)); // VERY VERY IMPORTANT
            client = builder.build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }

        return retrofit;
    }





}


