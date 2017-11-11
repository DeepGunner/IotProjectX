package com.eespl.iotapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class ServerConnection {
    String url = "http://www.techpacs.com/iot/appfiles/";
    JSONTokener tokener;
    JSONObject jObject;

    JSONObject Connection(String scriptFileName, List<NameValuePair> parameters) {
        String json = null;
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(String.valueOf(this.url) + scriptFileName);
        BufferedReader in = null;
        try {
            httpPost.setEntity((HttpEntity)new UrlEncodedFormEntity(parameters));
            HttpResponse response = httpClient.execute((HttpUriRequest)httpPost);
            in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            json = in.readLine();
            System.out.println("RESULT="+json);
            this.tokener = new JSONTokener(json);
            this.jObject = new JSONObject(this.tokener);
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        catch (ClientProtocolException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return this.jObject;
    }

    public static void main(String[] a) {
        System.out.println("Hello");
    }
}