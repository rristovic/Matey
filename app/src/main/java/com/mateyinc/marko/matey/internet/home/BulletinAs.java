package com.mateyinc.marko.matey.internet.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.mateyinc.marko.matey.activity.Util;
import com.mateyinc.marko.matey.activity.home.HomeActivity;
import com.mateyinc.marko.matey.activity.main.MainActivity;
import com.mateyinc.marko.matey.data_and_managers.DataManager;
import com.mateyinc.marko.matey.internet.http.HTTP;
import com.mateyinc.marko.matey.model.Bulletin;
import com.mateyinc.marko.matey.model.UserProfile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Random;

/**
 * Created by M4rk0 on 5/12/2016.
 */
public class BulletinAs extends AsyncTask<String, Void, String> {

    HomeActivity mContext;
    DataManager mBulletinManager;
    DataManager mDataManager;

    /**
     * Indicates if BulletinAs Async task is started for the first time in application or not.
     * If it is first start, then download friends data and bulletins data from the server, otherwise just download bulletin data
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

        // Init download bulletins with friends list
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
     * Method for creating the url for the server to download the new data from the given page
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

        if (!mContext.getPreferences(Context.MODE_PRIVATE).getBoolean("DATA_CREATED", false)) {
            createDummyFriendsData();
            createDummyBulletinData();
        }

        mBulletinManager.addNullBulletin();
        parseBulletins(result);
    }

    private void createDummyFriendsData() {
        SharedPreferences.Editor editor = mContext.getPreferences(Context.MODE_PRIVATE).edit();
        editor.putBoolean("DATA_CREATED", true);
        editor.commit();

        Random r = new Random();
        int namesSize = Util.names.length;
        int lNamesSize = Util.lastNames.length;

        for (int i = 0; i < HomeActivity.mCurUser.getNumOfFriends(); i++) {
            // define what gets saved and what not
            // For now only name and id

            mDataManager.addUserProfile(i, Util.names[r.nextInt(namesSize)], Util.lastNames[r.nextInt(lNamesSize)], -1);
        }
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

                        Bulletin bulletin = new Bulletin();
                        bulletin.setPostID(dataObj.getInt("post_id"));
                        bulletin.setUserID(dataObj.getInt("user_id"));
                        bulletin.setFirstName(dataObj.getString("first_name"));
                        bulletin.setLastName(dataObj.getString("last_name"));
                        bulletin.setDate(dataObj.getString("date_created"));
                        bulletin.setMessage(dataObj.getString("text"));

//                        Log.d("BulletinAs", i + 1 + ". " + bulletin.toString());
//                        mBulletinManager.addBulletin(bulletin);

                    }

//                    LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(mContext); // TODO - add EXTRA_ITEM_DOWNLOADED_COUNT
//                    broadcastManager.sendBroadcast(new Intent(BulletinManager.BULLETIN_LIST_LOADED));
//                    Log.d("BulletinAs", "Bulletin list is downloaded.");

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

    }

    private void createDummyBulletinData() {
        try {
            Random random = new Random();
            int itemDownloaded = 0;

            ArrayList<Bulletin> list = new ArrayList<>(DataManager.NO_OF_BULLETIN_TO_DOWNLOAD);
            LinkedList<Bulletin.Reply> repliesList = new LinkedList<>();

            for (int i = 0; i < DataManager.NO_OF_BULLETIN_TO_DOWNLOAD; i++) {

                UserProfile friend = mDataManager.getUserProfile(random.nextInt(DataManager.mFriendsListCount));
                Date date = new Date();
                date.setTime(date.getTime() - i * Util.ONE_MIN - DataManager.ONE_DAY * DataManager.mCurrentPage);

                Bulletin bulletin = new Bulletin();
                bulletin.setPostID(i + DataManager.NO_OF_BULLETIN_TO_DOWNLOAD * DataManager.mCurrentPage);
                bulletin.setUserID(friend.getUserId());
                bulletin.setFirstName(friend.getFirstName());
                bulletin.setLastName(friend.getLastName());
                bulletin.setDate(date);
                bulletin.setMessage(Util.loremIspum);
                bulletin.setNumOfReplies(random.nextInt(20));

                for (int j = 0; j < bulletin.getNumOfReplies(); j++) {

                    UserProfile friendReplied = mDataManager.getUserProfile(random.nextInt(DataManager.mFriendsListCount));
                    Bulletin.Reply r = bulletin.getReplyInstance();

                    r.replyId = Integer.parseInt(Integer.toString(bulletin.getPostID()) + Integer.toString(j)); // replyId eg - 05: 0 - postId, 5 - replyId;
                    r.userId = friendReplied.getUserId();
                    r.postId = bulletin.getPostID();
                    r.userFirstName = friendReplied.getFirstName();
                    r.userLastName = friendReplied.getLastName();
                    r.replyText = Util.loremIpsumShort;
                    r.replyDate = new Date(date.getTime() - Util.ONE_MIN * j - Util.ONE_DAY * DataManager.mCurrentPage);

                    for (int k = 0; k < random.nextInt(5); k++) {
                        r.replyApproves.add(new UserProfile(random.nextInt(DataManager.mFriendsListCount + 80)));
                        r.numOfApprvs++;
                    }

                    repliesList.add(r);
                }

                list.add(bulletin);
                itemDownloaded++;
            }
            mBulletinManager.addBulletins(list);
            mBulletinManager.addReplies(repliesList);

            LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(mContext);
            Intent i = new Intent(DataManager.BULLETIN_LIST_LOADED);
            i.putExtra(DataManager.EXTRA_ITEM_DOWNLOADED_COUNT, itemDownloaded);

            // Notifying HomeActivity that the data has been downloaded with broadcast and static member TODO - notify in onPostExecute later
            broadcastManager.sendBroadcast(i);
            HomeActivity.mListDownloaded = true;
        } catch (Exception e) {
            Log.e(BulletinAs.class.getSimpleName(), e.getLocalizedMessage(), e);
        }
    }
}
