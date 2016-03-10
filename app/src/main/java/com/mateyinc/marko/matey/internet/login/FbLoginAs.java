package com.mateyinc.marko.matey.internet.login;

import android.os.AsyncTask;

import com.mateyinc.marko.matey.data.UrlData;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class FbLoginAs extends AsyncTask<String,Void,String> {

    @Override
    protected String doInBackground(String... params) {

        String token = params[0];
        String fbid = params[1];
        String firstName = params[2];
        String lastName = params[3];
        String email = params[4];

        try{

            URL url = new URL(UrlData.FB_LOG_URL);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");


            OutputStream os = httpURLConnection.getOutputStream();
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            String data = URLEncoder.encode("token", "UTF-8") + "=" + URLEncoder.encode(token, "UTF-8") + "&" +
                    URLEncoder.encode("fbid", "UTF-8") + "=" + URLEncoder.encode(fbid, "UTF-8") + "&" +
                    URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8") + "&" +
                    URLEncoder.encode("firstName", "UTF-8") + "=" + URLEncoder.encode(firstName, "UTF-8") + "&" +
                    URLEncoder.encode("lastName", "UTF-8") + "=" + URLEncoder.encode(lastName, "UTF-8");
            bw.write(data);
            bw.flush();
            bw.close();
            os.close();

            InputStream is = httpURLConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "iso-8859-1"));
            String response = "";
            String line = "";
            while((line=br.readLine()) != null) {
                response += line;
            }
            br.close();
            is.close();
            httpURLConnection.disconnect();

            return response;

        } catch (MalformedURLException e) {
                return "Greska";
        } catch (IOException e) {
                return "Greska IO";
        } catch (Exception e) {
            return "Greska IO";
        }

    }

    protected void onProgressUpdate(Integer... progress) {

    }

    protected void onPostExecute(String result) {

    }


}
