package ir.nabaksoft.office.api;

import android.content.Context;
import android.support.annotation.NonNull;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import ir.amp.tools.MLog;
import ir.amp.tools.utils.HttpUtils;
import ir.nabaksoft.office.tools.DeviceInfo;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;

public class HeaderInterceptor implements Interceptor
{
    Context context;

    public HeaderInterceptor(Context context)
    {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException
    {
        Request request = chain.request();

        Map<String, String> defaultPostParams = DefaultPost.get(context);
        String parameter = "";

        Request.Builder builder = request.newBuilder()
                .addHeader("deviceplatform", "android")
                .removeHeader("User-Agent")
                .addHeader("User-Agent", "OfficeMeshkat");

        for(String key : defaultPostParams.keySet())
        {
            parameter += "&" + key + "=" + defaultPostParams.get(key);
        }

        if("POST".equals(request.method()))
        {
            RequestBody body = getNewBody(request, parameter);
            builder.post(body);
        }
        else
        {
            HttpUrl.Builder urlBuilder = request.url().newBuilder();
            for(String key : defaultPostParams.keySet())
            {
                urlBuilder.addQueryParameter(key, defaultPostParams.get(key));
            }

            builder.url(urlBuilder.build());
        }

        request = builder.build();
        Response response = chain.proceed(request);
        return response;
    }

    public RequestBody getNewBody(@NonNull Request request, @NonNull String parameter) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Sink sink = Okio.sink(baos);
        BufferedSink bufferedSink = Okio.buffer(sink);

        /**
         * Write old params
         * */
        if(request.body() != null)
            request.body().writeTo(bufferedSink);

        /**
         * write to buffer additional params
         * */
        bufferedSink.writeString(parameter, Charset.defaultCharset());

        RequestBody newRequestBody = RequestBody.create(
                request.body() == null ? null : request.body().contentType(),
                bufferedSink.buffer().readUtf8()
        );

        return newRequestBody;
    }
}