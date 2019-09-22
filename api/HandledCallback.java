package ir.nabaksoft.office.api;

import android.content.Context;
import android.content.Intent;

import ir.amp.tools.MLog;
import ir.nabaksoft.office.LoginActivity;
import ir.nabaksoft.office.model.BaseApiModel;
import ir.nabaksoft.office.model.Person;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.Callback;

/**
 * Created by Ali on 6/7/2019.
 */

public class HandledCallback<T> implements Callback<T>
{
    Context context;

    public HandledCallback(Context context)
    {
        this.context = context;
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response)
    {
        if(response.code() == 503)
        {
            Person.needLogin(context);
            return;
        }
        if(response.body() instanceof BaseApiModel)
        {
            BaseApiModel model = ((BaseApiModel) response.body());

            int code = model.ResultCode;
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t)
    {

    }
}
