package com.aroncent.api;

import com.aroncent.utils.ExtensionsKt;
import com.tencent.mmkv.MMKV;

import java.io.IOException;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;

/**
 * @Author: DX
 * @CreateTime: 2020/5/13
 * @Explain :公共參數攔截器
 */
public class LogInterceptor implements Interceptor {
    public static String TAG = "LogInterceptor";

    @Override
    public okhttp3.Response intercept(Chain chain) throws IOException {
        Request oldRequest = chain.request();
        Request.Builder newRequestBuild = null;
        String method = oldRequest.method();
        String postBodyString = "";
        if ("POST".equals(method)) {
            RequestBody oldBody = oldRequest.body();
            if (oldBody instanceof FormBody) {
                FormBody.Builder formBodyBuilder = new FormBody.Builder();
                MMKV kv = MMKV.defaultMMKV();
                if (kv.containsKey("token")) {
                    formBodyBuilder.add("token", ExtensionsKt.getUserToken());
                }
                newRequestBuild = oldRequest.newBuilder();
                RequestBody formBody = formBodyBuilder.build();
                postBodyString = bodyToString(oldRequest.body());
                postBodyString += ((postBodyString.length() > 0) ? "&" : "") + bodyToString(formBody);
                newRequestBuild.post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded;charset=UTF-8"), postBodyString));
            } else if (oldBody instanceof MultipartBody) {
                MultipartBody oldBodyMultipart = (MultipartBody) oldBody;
                List<MultipartBody.Part> oldPartList = oldBodyMultipart.parts();
                MultipartBody.Builder builder = new MultipartBody.Builder();
                builder.setType(MultipartBody.FORM);
                for (MultipartBody.Part part : oldPartList) {
                    builder.addPart(part);
                    postBodyString += (bodyToString(part.body()) + "\n");
                }
                newRequestBuild = oldRequest.newBuilder();
                newRequestBuild.post(builder.build());
            } else {
                newRequestBuild = oldRequest.newBuilder();
            }
        } else {

            // 添加新的参数
            HttpUrl.Builder commonParamsUrlBuilder = oldRequest.url()
                    .newBuilder()
                    .scheme(oldRequest.url().scheme())
                    .host(oldRequest.url().host());
            MMKV kv = MMKV.defaultMMKV();
            if (kv.containsKey("token")) {
                commonParamsUrlBuilder.addQueryParameter("token",ExtensionsKt.getUserToken());
            }
            newRequestBuild = oldRequest.newBuilder()
                    .method(oldRequest.method(), oldRequest.body())
                    .url(commonParamsUrlBuilder.build());
        }
        Request newRequest = newRequestBuild
                .build();

        okhttp3.Response response = chain.proceed(newRequest);
        MediaType mediaType = response.body().contentType();
        String content = response.body().string();
        return response.newBuilder()
                .body(okhttp3.ResponseBody.create(mediaType, content))
                .build();
    }

    private static String bodyToString(final RequestBody request) {
        try {
            final RequestBody copy = request;
            final Buffer buffer = new Buffer();
            if (copy != null)
                copy.writeTo(buffer);
            else
                return "";
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "did not work";
        }
    }
}
