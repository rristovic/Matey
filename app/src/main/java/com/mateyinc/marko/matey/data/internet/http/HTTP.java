package com.mateyinc.marko.matey.data.internet.http;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by M4rk0 on 4/25/2016.
 */
public class HTTP {

    private HttpURLConnection httpURLConnection;

    public HTTP (String urlStr, String method) {
        try {

            URL url = new URL(urlStr);
            this.httpURLConnection = (HttpURLConnection) url.openConnection();

            httpURLConnection.setConnectTimeout(60000);
            httpURLConnection.setRequestMethod(method);
            httpURLConnection.setDoInput(true);

            if(method.equals("POST")) {
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            }

        } catch (Exception e) {

            this.httpURLConnection = null;

        }
    }

    public boolean sendPost (String data) {

        try {

            OutputStream os = httpURLConnection.getOutputStream();
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            bw.write(data);
            bw.flush();
            bw.close();
            os.close();

        } catch (Exception e) {

            return false;

        }

        return true;

    }

    public String getData () {

        String response = "";

        try {

            InputStream is = httpURLConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = "";
            while ((line = br.readLine()) != null) {
                response += line;
            }
            br.close();
            is.close();

        }catch (Exception e) {

            return null;

        }

        if(response.equals("")) return null;

        return response;

    }

}
