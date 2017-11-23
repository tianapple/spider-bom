package org.spider.http;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * http操作类
 * <p>
 * Created by tianapple on 2016/10/19.
 */
public class HttpUtils {
    private static int Default_Timeout = 10000;

    //httpGet
    public static HttpResponse httpGet(String url) throws IOException, URISyntaxException {
        return httpGetImpl(url, null, Default_Timeout);
    }

    public static HttpResponse httpGet(String url, String contentType) throws IOException, URISyntaxException {
        Map<String, String> headers = new HashMap<>(1);
        headers.put("Content-Type", contentType);
        return httpGetImpl(url, headers, Default_Timeout);
    }

    public static HttpResponse httpGet(String url, String contentType, int timeout) throws IOException, URISyntaxException {
        Map<String, String> headers = new HashMap<>(1);
        headers.put("Content-Type", contentType);
        return httpGetImpl(url, headers, timeout);
    }

    public static HttpResponse httpGet(String url, Map<String, String> headers) throws IOException, URISyntaxException {
        return httpGetImpl(url, headers, Default_Timeout);
    }

    public static HttpResponse httpGet(String url, Map<String, String> headers, int timeout) throws IOException, URISyntaxException {
        return httpGetImpl(url, headers, timeout);
    }

    private static HttpResponse httpGetImpl(String urlStr, Map<String, String> headers, int timeout) throws IOException, URISyntaxException {
        RequestConfig config = RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(timeout).build();
        URL url = new URL(urlStr);
        URI uri = new URI(url.getProtocol(), url.getHost() + ":" + url.getPort(),url.getPath(), url.getQuery(), null);
        HttpGet httpGet = new HttpGet(uri);
        httpGet.setConfig(config);
        if (headers != null && headers.size() > 0) {
            for (Map.Entry<String, String> kv : headers.entrySet()) {
                httpGet.setHeader(kv.getKey(), kv.getValue());
            }
        }
        CloseableHttpClient httpClient = HttpClients.createDefault();
        return httpClient.execute(httpGet);
    }

    //httpPost;
    public static HttpResponse httpPost(String url, String postStr, ContentType contentType) throws IOException {
        return httpPost(url, postStr, contentType, null, Default_Timeout);
    }

    public static HttpResponse httpPost(String url, String postStr, ContentType contentType, int timeout) throws IOException {
        return httpPost(url, postStr, contentType, null, timeout);
    }

    public static HttpResponse httpPost(String url, String postStr, ContentType contentType, Map<String, String> headers, int timeout) throws IOException {
        //public static HttpResponse httpPost(String url, List<NameValuePair> params, Map<String, String> headers, int timeout) throws IOException {
        RequestConfig config = RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(timeout).build();
        EntityBuilder builder = EntityBuilder.create();
        if (contentType != null) {
            builder.setContentType(contentType);
        }
        builder.setText(postStr);
        HttpEntity httpEntity = builder.build();

        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(config);
        httpPost.setEntity(httpEntity);
        if (headers != null && headers.size() > 0) {
            for (Map.Entry<String, String> kv : headers.entrySet()) {
                httpPost.setHeader(kv.getKey(), kv.getValue());
            }
        }

        CloseableHttpClient httpClient = HttpClients.createDefault();
        return httpClient.execute(httpPost);
    }

    //response
    public static String getResponseContext(HttpResponse response) throws IOException {
        return getResponseContext(response, "UTF-8");
    }

    public static String getResponseContext(HttpResponse response, String charset) throws IOException {
        HttpEntity entity = response.getEntity();
        String str = EntityUtils.toString(entity, charset);
        return str.trim();
    }
}
