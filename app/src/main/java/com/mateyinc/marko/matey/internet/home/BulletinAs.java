package com.mateyinc.marko.matey.internet.home;

import android.content.Intent;
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
import java.util.Random;

/**
 * Created by M4rk0 on 5/12/2016.
 */
public class BulletinAs extends AsyncTask<String, Void, String> {

    HomeActivity mContext;
    DataManager mBulletinManager;
    DataManager mDataManager;

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

    private void parseFriendsAndBulletins(String result) {
        //Parsing friends
        // TODO - finish method with SQLite
        createDummyFriendsData();

        parseBulletins(result);
    }

    private void createDummyFriendsData() {
        Random r = new Random();
        int namesSize = Util.names.length;
        int lNamesSize = Util.lastNames.length;

        for (int i = 0; i < HomeActivity.mCurUser.getNumOfFriends(); i++) {
            // TODO - define what gets saved and what not
            // For now only name and id

            mDataManager.addUserProfile(i, Util.names[r.nextInt(namesSize)], Util.lastNames[r.nextInt(lNamesSize)], -1);
        }
    }

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

        createDummyBulletinData();
    }

    private void createDummyBulletinData() {
        try {
            mBulletinManager.addNullBulletin();
            Random random = new Random();
            int itemDownloaded = 0;

            ArrayList<Bulletin> list = new ArrayList<>(DataManager.NO_OF_BULLETIN_TO_DOWNLOAD);
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

                for (int j = 0; j < random.nextInt(20); j++) {

                    UserProfile friendReplied = mDataManager.getUserProfile(random.nextInt(DataManager.mFriendsListCount));
                    Bulletin.Reply r = bulletin.getReplyInstance();

                    r.replyId = Integer.parseInt(Integer.toString(i) + Integer.toString(j)); // replyId eg - 05: 0 - postId, 5 - replyId;
                    r.userId = friendReplied.getUserId();
                    r.userFirstName = friendReplied.getFirstName();
                    r.userLastName = friendReplied.getLastName();
                    r.replyText = Util.loremIpsumShort;
                    r.replyDate = new Date(date.getTime() - Util.ONE_MIN * j - Util.ONE_DAY * DataManager.mCurrentPage).toString();

                    for (int k = 0; k < random.nextInt(5); k++) {
                        r.replyApproves.add(new UserProfile(random.nextInt(DataManager.mFriendsListCount + 80)));
                    }

                    bulletin.getReplies().add(r);
                }

//                mBulletinManager.addBulletin(bulletin);
                list.add(bulletin);
                itemDownloaded++;
            }
            mBulletinManager.addBulletins(list);

            LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(mContext);
            Intent i = new Intent(DataManager.BULLETIN_LIST_LOADED);
            i.putExtra(DataManager.EXTRA_ITEM_DOWNLOADED_COUNT, itemDownloaded);
            broadcastManager.sendBroadcast(i);
            HomeActivity.mListDownloaded = true;
        } catch (Exception e) {
            Log.e(BulletinAs.class.getSimpleName(), e.getLocalizedMessage(), e);
        }
    }
}
