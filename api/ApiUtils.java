package ir.nabaksoft.office.api;

import android.content.Context;
import android.graphics.Interpolator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Ali on 5/17/2019.
 */

public class ApiUtils
{
    private static final String BASE_URL = "...";

    private static Retrofit retrofit = null;
    private static OkHttpClient client = null;

    public static OkHttpClient getOkHttpClient(Context context)
    {
        if(client == null)
        {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.level(HttpLoggingInterceptor.Level.BODY);

            client = new OkHttpClient.Builder()
                    .addInterceptor(new HeaderInterceptor(context))
                    .addInterceptor(loggingInterceptor)
                    .build();
        }
        return client;
     }

    public static Retrofit getClient(Context context, String baseUrl)
    {
        if (retrofit == null)
        {
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            retrofit = new Retrofit.Builder()
                    .client(getOkHttpClient(context))
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }

        return retrofit;
    }

    public static OfficeApi getApi(Context context)
    {
        return getClient(context, BASE_URL).create(OfficeApi.class);
    }


}
