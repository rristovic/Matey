package com.mateyinc.marko.matey.data.internet.home;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.mateyinc.marko.matey.activity.home.HomeActivity;
import com.mateyinc.marko.matey.activity.main.MainActivity;
import com.mateyinc.marko.matey.data.DataManager;
import com.mateyinc.marko.matey.data.internet.http.HTTP;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;

/**
 * Created by M4rk0 on 5/12/2016.
 */
public class BulletinAs extends AsyncTask<String, Void, String> {

    HomeActivity mContext;
    DataManager mBulletinManager;
    DataManager mDataManager;

    /**
     * Indicates if BulletinAs Async task is started for the first time in application or not.
     * If it is first start, then startDownloadAction friends data and bulletins data from the server, otherwise just startDownloadAction bulletin data
     */
    private boolean mIsInit = false;

    public BulletinAs(HomeActivity activity) {
        this.mContext = activity;
        mBulletinManager = DataManager.getInstance(activity);
        mDataManager = DataManager.getInstance(activity);
    }

    @Override
    protected String doInBackground(String... params) {

        boolean init = Boolean.parseBoolean(params[4].toLowerCase());

        if(mContext.isDebug()){
            if(init) mIsInit
                     = true;
           return null;
        }

        // Init startDownloadAction bulletins with friends list
        if (!isCancelled() && init) {

            mIsInit = true;
            String user_id = params[0];
            String uid = params[1];
            String device_id = params[2];

            try {

                String data = URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(user_id, "UTF-8") + "&" +
                        URLEncoder.encode("uid", "UTF-8") + "=" + URLEncoder.encode(uid, "UTF-8") + "&" +
                        URLEncoder.encode("device_id", "UTF-8") + "=" + URLEncoder.encode(device_id, "UTF-8");
                HTTP http = new HTTP(createUrl(params[3]), "POST");

                if (http.sendPost(data)) return http.getData();


            } catch (Exception e) {

                Log.e("BulletinAs", e.getMessage());
                return null;

            }


            // Downloading without friends list
        } else if (!isCancelled()) {
            String user_id = params[0];
            String uid = params[1];
            String device_id = params[2];

            try {

                String data = URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(user_id, "UTF-8") + "&" +
                        URLEncoder.encode("uid", "UTF-8") + "=" + URLEncoder.encode(uid, "UTF-8") + "&" +
                        URLEncoder.encode("device_id", "UTF-8") + "=" + URLEncoder.encode(device_id, "UTF-8");
                HTTP http = new HTTP(createUrl(params[3]), "POST");

                if (http.sendPost(data)) return http.getData();


            } catch (Exception e) {

                Log.e("BulletinAs", e.getMessage());
                return null;

            }
        }

        return null;

    }

    /**
     * Method for creating the url for the server to startDownloadAction the new data from the given page
     *
     * @param curPage the page of the new data to be downloaded
     * @return newly created url ready to send to the server
     */
    private String createUrl(String curPage) {
        // TODO - finish method with mCurPage as param
        return null;
    }

    @Override
    protected void onPostExecute(String result) {

        if (mIsInit) {
            parseFriendsAndBulletins(result);
            mIsInit = false;
        } else {
            parseBulletins(result);
        }
    }

    /**
     * Helper method for parsing friends and bulletins data into the database
     *
     * @param result the data collected from the server that needs to be parsed
     */
    private void parseFriendsAndBulletins(String result) {
        //Parsing friends
        // TODO - finish method with SQLite

        parseBulletins(result);
    }



    /**
     * Method for parsing bulletins into the db
     *
     * @param result bulletins that needs to be parsed
     */
    private void parseBulletins(String result) {
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

//                        Bulletin bulletin = new Bulletin(
//                                dataObj.getInt("post_id"),
//                                dataObj.getInt("user_id")
//                        );
//                        bulletin.setPostID();
//                        bulletin.setUserID();
//                        bulletin.setFirstName(dataObj.getString("first_name"));
//                        bulletin.setLastName(dataObj.getString("last_name"));
//                        bulletin.setDate(dataObj.getString("date_created"));
//                        bulletin.setMessage(dataObj.getString("text"));

//                        Log.d("BulletinAs", i + 1 + ". " + bulletin.toString());
//                        mBulletinManager.addBulletin(bulletin);

                    }

//                    LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(mContext); // TODO - add EXTRA_ITEM_DOWNLOADED_COUNT
//                    broadcastManager.sendBroadcast(new Intent(BulletinManager.BULLETIN_LIST_LOADED));
//                    Log.d("BulletinAs", "Bulletin list is downloaded.");

                } else if (!jsonObject.getBoolean("success") && (jsonObject.getString("message").equals("not_logged") || jsonObject.getString("message").equals("not_authorized"))) {

//                    mContext.clearUserCredentials();

                    Intent intent = new Intent(mContext, MainActivity.class);
                    mContext.startActivity(intent);
                    mContext.finish();

                } else throw new Exception();

            } else throw new Exception();

        } catch (Exception e) {
            mContext.showDialog(1000);
        }

    }

    ///////////////////////////////////////////////////////////////////////////////////////



}
