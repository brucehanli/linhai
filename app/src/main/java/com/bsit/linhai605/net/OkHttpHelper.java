package com.bsit.linhai605.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.bsit.linhai605.constant.Constant;
import com.bsit.linhai605.utils.CommonUtil;
import com.bsit.linhai605.utils.SharedUtils;
import com.google.gson.Gson;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpHelper {
    /**
     * 采用单例模式使用OkHttpClient
     */
    private static OkHttpHelper mOkHttpHelperInstance;
    private static OkHttpClient mClientInstance;
    private Handler mHandler;
    private Gson mGson;

    /**
     * 单例模式，私有构造函数，构造函数里面进行一些初始化
     */
    private OkHttpHelper() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(300, TimeUnit.SECONDS);
        builder.readTimeout(300, TimeUnit.SECONDS);
        builder.writeTimeout(300, TimeUnit.SECONDS);
        mClientInstance = builder.build();

//        mClientInstance = new OkHttpClient();
//        mClientInstance.newBuilder()
//                .connectTimeout(30, TimeUnit.SECONDS)
//                .readTimeout(30, TimeUnit.SECONDS).build();
        mGson = new Gson();
        mHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * 获取实例
     *
     * @return
     */
    public static OkHttpHelper getInstance() {
        if (mOkHttpHelperInstance == null) {
            synchronized (OkHttpHelper.class) {
                if (mOkHttpHelperInstance == null) {
                    mOkHttpHelperInstance = new OkHttpHelper();
                }
            }
        }
        return mOkHttpHelperInstance;
    }

    /**
     * 封装一个request方法，不管post或者get方法中都会用到
     */
    public void request(Context context, final Request request, final NetCallBack callback) {        //在请求之前所做的事，比如弹出对话框等
        if (!isNetworkAvailable(context)) {
            callbackFailure(callback, "网络不可用", 400);
            return;
        }
        mClientInstance.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callbackFailure(callback, e.getMessage(), -1);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200)
                    callbackSuccess(response.body().string(), callback);
                else
                    callbackFailure(callback, "服务器异常" + response.code(), response.code());


            }
        });
    }

    /**
     * 检查网络是否可用
     *
     * @return
     */
    public boolean isNetworkAvailable(Context context) {

        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);

        if (manager == null) {
            return false;
        }

        NetworkInfo networkinfo = manager.getActiveNetworkInfo();

        if (networkinfo == null || !networkinfo.isAvailable()) {
            return false;
        }

        return true;
    }

    /**
     * 在主线程中执行的回调
     *
     * @param msg
     * @param callback
     */
    private void callbackSuccess(final String msg, final NetCallBack callback) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.successCallBack(msg);
            }
        });
    }

    /**
     * 在主线程中执行的回调
     *
     * @param callback
     * @param msg
     */
    private void callbackFailure(final NetCallBack callback, final String msg, final int code) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.failedCallBack(msg, code);
            }
        });
    }

    /**
     * 对外公开的post方法
     *
     * @param url
     * @param params
     * @param callback
     */
    public void post(Context context, String url, Map<String, String> params, NetCallBack callback) {
        Request request = buildRequest(context, url, params, HttpMethodType.POST);
        request(context, request, callback);
    }

    public void get(Context context, String url, NetCallBack callback) {
        Request request = buildRequest(context, url, null, HttpMethodType.POST);
        request(context, request, callback);
    }

    /**
     * 这是一个下载文件的方法
     */
    public void downloadFile(Context context, String url, final Map<String, String> params, final NetCallBack callback) {
        Request request = buildRequest(context, url, params, HttpMethodType.POST);
        if (!isNetworkAvailable(context)) {
            callbackFailure(callback, "网络不可用", 400);
            return;
        }
        mClientInstance.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callbackFailure(callback, e.getMessage(), -1);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200) {
                    CommonUtil.mkdirs(Constant.PATH_DIR);
                    //将返回结果转化为流，并写入文件
                    int len;
                    byte[] buf = new byte[2048];
                    InputStream inputStream = response.body().byteStream();

                    FileOutputStream out = new FileOutputStream(Constant.PATH_DIR + params.get("fileName"));
                    while ((len = inputStream.read(buf)) != -1) {
                        out.write(buf, 0, len);
                    }
                    callbackSuccess(params.get("fileName"), callback);
                    out.flush();
                    out.close();
                    inputStream.close();

                } else
                    callbackFailure(callback, "服务器异常" + response.code(), response.code());
            }
        });

    }

    /**
     * 构建请求对象
     *
     * @param url
     * @param params
     * @param type
     * @return
     */
    private Request buildRequest(Context context, String url, Map<String, String> params, HttpMethodType type) {
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        if (!TextUtils.isEmpty(SharedUtils.getToken(context)))
            builder.header("ACCESS_TOKEN", SharedUtils.getToken(context));
        if (type == HttpMethodType.GET) {
            builder.get();
        } else if (type == HttpMethodType.POST && params != null) {
            builder.post(buildRequestBody(params));
        }
        return builder.build();
    }

    /**
     * 通过Map的键值对构建请求对象的body
     *
     * @return
     */
    private RequestBody buildRequestBody(Map<String, String> paramsMap) {
        FormBody.Builder formBody = new FormBody.Builder();
        for (String key : paramsMap.keySet()) {
            //追加表单信息
            formBody.add(key, paramsMap.get(key) == null ? "" : paramsMap.get(key));
        }
        return formBody.build();
//        return FormBody.create(MediaType.parse("application/json; charset=utf-8"), mGson.toJson(paramsMap));
    }

    /**
     * 这个枚举用于指明是哪一种提交方式
     */
    enum HttpMethodType {
        GET,
        POST
    }

}