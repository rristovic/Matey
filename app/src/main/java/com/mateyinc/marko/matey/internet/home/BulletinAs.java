package com.mateyinc.marko.matey.internet.home;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.mateyinc.marko.matey.activity.Util;
import com.mateyinc.marko.matey.activity.home.HomeActivity;
import com.mateyinc.marko.matey.activity.main.MainActivity;
import com.mateyinc.marko.matey.data_and_managers.BulletinManager;
import com.mateyinc.marko.matey.data_and_managers.UrlData;
import com.mateyinc.marko.matey.internet.http.HTTP;
import com.mateyinc.marko.matey.model.Bulletin;
import com.mateyinc.marko.matey.model.UserProfile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;

/**
 * Created by M4rk0 on 5/12/2016.
 */
public class BulletinAs extends AsyncTask<String, Void, String> {

    HomeActivity mContext;
    BulletinManager bulletinManager;

    public BulletinAs(HomeActivity activity) {
        this.mContext = activity;
        bulletinManager = BulletinManager.getInstance(activity);
    }

    @Override
    protected String doInBackground(String... params) {

        if (!isCancelled()) {

            String user_id = params[0];
            String uid = params[1];
            String device_id = params[2];

            try {

                String data = URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(user_id, "UTF-8") + "&" +
                        URLEncoder.encode("uid", "UTF-8") + "=" + URLEncoder.encode(uid, "UTF-8") + "&" +
                        URLEncoder.encode("device_id", "UTF-8") + "=" + URLEncoder.encode(device_id, "UTF-8");
                HTTP http = new HTTP(UrlData.FETCH_ALL_POSTS, "POST");

                if (http.sendPost(data)) return http.getData();


            } catch (Exception e) {

                Log.e("BulletinAs", e.getMessage());
                return null;

            }

        }

        return null;

    }

    @Override
    protected void onPostExecute(String result) {

        try {

            // if there is some result check if successful
            if (result != null) {

                JSONObject jsonObject = new JSONObject(result);

                // if successful, set everything to SecurePreferences
                if (jsonObject.getBoolean("success")) {

                    // converting data
                    JSONArray dataArr = new JSONArray(jsonObject.getString("posts"));

                    // punjenje Bulletin objekata podacima
                    for (int i = 0; i < dataArr.length(); i++) {

                        JSONObject dataObj = new JSONObject(dataArr.get(i).toString());

                        Bulletin bulletin = new Bulletin();
                        bulletin.setPostID(dataObj.getInt("post_id"));
                        bulletin.setUserID(dataObj.getInt("user_id"));
                        bulletin.setFirstName(dataObj.getString("first_name"));
                        bulletin.setLastName(dataObj.getString("last_name"));
                        bulletin.setDate(dataObj.getString("date_created"));
                        bulletin.setMessage(dataObj.getString("text"));

//                        Log.d("BulletinAs", i + 1 + ". " + bulletin.toString());
                        bulletinManager.addBulletin(bulletin);

                    }

                    LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(mContext);
                    broadcastManager.sendBroadcast(new Intent(BulletinManager.BULLETIN_LIST_LOADED));
                    Log.d("BulletinAs", "Bulletin list is downloaded.");

                } else if (!jsonObject.getBoolean("success") && (jsonObject.getString("message").equals("not_logged") || jsonObject.getString("message").equals("not_authorized"))) {

                    mContext.clearUserCredentials();

                    Intent intent = new Intent(mContext, MainActivity.class);
                    mContext.startActivity(intent);
                    mContext.finish();

                } else throw new Exception();

            } else throw new Exception();

        } catch (Exception e) {
            mContext.showDialog(1000);
        }

        createDummyData();

    }

    private void createDummyData() {
        JSONArray posts;
        try {
            JSONObject jsonObject = new JSONObject(Util.LoadFile("posts", true, mContext));
            posts = jsonObject.getJSONArray("posts");
        } catch (JSONException e) {
            Log.e(BulletinAs.class.getSimpleName(), e.getLocalizedMessage(), e);
            return;
        } catch (IOException e) {
            Log.e(BulletinAs.class.getSimpleName(), e.getLocalizedMessage(), e);
            return;
        }
        try {
            bulletinManager.addBulletin(null);
            for (int i = 0; i < posts.length(); i++) {
                JSONObject post = (JSONObject)posts.get(i);

                Bulletin bulletin = new Bulletin();
                bulletin.setPostID(post.getInt("post_id"));
                bulletin.setUserID(post.getInt("user_posted_id"));
                bulletin.setFirstName(post.getString("user_posted_first_name"));
                bulletin.setLastName(post.getString("user_posted_last_name"));
                bulletin.setDate(post.getString("post_date"));
                bulletin.setMessage(post.getString("post_text"));

                JSONArray replies = post.getJSONArray("replies");
                for (int j = 0; j< replies.length();j++){
                    JSONObject reply = (JSONObject)replies.get(j);

                    Bulletin.Reply r = bulletin.getReplyInstance();
                    r.replyId = reply.getInt("reply_id");
                    r.userId = reply.getInt("reply_user_id");
                    r.userFirstName = reply.getString("user_replied_first_name");
                    r.userLastName = reply.getString("user_replied_last_name");
                    r.replyText = reply.getString("reply_text");
                    r.replyDate = reply.getString("reply_date");

                    JSONArray replyApproves = reply.getJSONArray("reply_approves");
                    for(int k = 0;k<replyApproves.length();k++){
                        JSONObject replyApprove = replyApproves.getJSONObject(k);
                        r.replyApproves.add(new UserProfile(replyApprove.getInt("aprv_user_id")));
                    }

                    bulletin.getReplies().add(r);
                }

//                        Log.d("BulletinAs", i + 1 + ". " + bulletin.toString());
                bulletinManager.addBulletin(bulletin);
            }
        } catch (JSONException e) {
            Log.e(BulletinAs.class.getSimpleName(), e.getLocalizedMessage(), e);
        }

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(mContext);
        broadcastManager.sendBroadcast(new Intent(BulletinManager.BULLETIN_LIST_LOADED));
        Log.d("BulletinAs", "Bulletin list is downloaded.");
    }

}
