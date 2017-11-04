package message.centit.com.message.net;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by zhuyu on 2017/11/4.
 */

public class OkHttpUtil {


    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public final static int CONNECT_TIMEOUT = 60;
    public final static int READ_TIMEOUT = 100;
    public final static int WRITE_TIMEOUT = 60;

    private static OkHttpClient client;
    //非常有必要，要不此类还是可以被new，但是无法避免反射，好恶心
    private OkHttpUtil(){

    }
    public static OkHttpClient getInstance() {
        if (client == null)
        {
            synchronized (OkHttpUtil.class)
            {
                if (client == null)
                {
                    client = new OkHttpClient.Builder()
                            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)//设置读取超时时间
                            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)//设置写的超时时间
                            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)//设置连接超时时间
                            .build();
                }
            }
        }
        return client;
    }




    public static void post(String url, String json, Callback callback) {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
       client.newCall(request).enqueue(callback);
    }
}
