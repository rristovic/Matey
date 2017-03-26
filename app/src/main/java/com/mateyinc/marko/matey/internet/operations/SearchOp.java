package com.mateyinc.marko.matey.internet.operations;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.android.volley.VolleyError;
import com.mateyinc.marko.matey.data.DataAccess;
import com.mateyinc.marko.matey.internet.UrlData;
import com.mateyinc.marko.matey.internet.events.DownloadEvent;
import com.mateyinc.marko.matey.internet.events.SearchHintEvent;
import com.mateyinc.marko.matey.model.Bulletin;
import com.mateyinc.marko.matey.model.Group;
import com.mateyinc.marko.matey.model.UserProfile;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.mateyinc.marko.matey.internet.operations.OperationType.SEARCH_BULLETINS;
import static com.mateyinc.marko.matey.internet.operations.OperationType.SEARCH_GROUPS;
import static com.mateyinc.marko.matey.internet.operations.OperationType.SEARCH_PROFILES;


public class SearchOp extends Operations {
    private static String mUserNextUrl = "";
    private static String mGroupNextUrl = "";
    private static String mBulletinNextUrl = "";

    private String mSearchQuery;

    public SearchOp(String searchQuery, Context context) {
        super(context);
        this.mSearchQuery = searchQuery;
    }

    @Override
    public void startDownloadAction() {
        switch (mOpType) {
            default:
            case SEARCH_TOP:
                mUrl = Uri.parse(UrlData.GET_SEARCH_TOP).buildUpon()
                        .appendQueryParameter("q", mSearchQuery).build().toString();
                break;
            case SEARCH_PROFILES:
                if (mUserNextUrl.isEmpty()) {
                    mUrl = Uri.parse(UrlData.GET_SEARCH_USERS).buildUpon()
                            .appendQueryParameter("q", mSearchQuery).build().toString();
//                    mClearData = true;
                } else
                    mUrl = buildNextPageUrl(mUserNextUrl);
                break;
            case SEARCH_GROUPS:
                if (mGroupNextUrl.isEmpty()) {
                    mUrl = Uri.parse(UrlData.GET_SEARCH_GROUPS).buildUpon()
                            .appendQueryParameter("q", mSearchQuery).build().toString();
//                    mClearData = true;
                } else
                    mUrl = buildNextPageUrl(mGroupNextUrl);
                break;
            case SEARCH_BULLETINS:
                if (mBulletinNextUrl.isEmpty()) {
                    mUrl = Uri.parse(UrlData.GET_SEARCH_BULLETINS).buildUpon()
                            .appendQueryParameter("q", mSearchQuery).build().toString();
//                    mClearData = true;
                } else
                    mUrl = buildNextPageUrl(mBulletinNextUrl);
                break;
            case SEARCH_AUTOCOMPLETE:
                mUrl = Uri.parse(UrlData.GET_SEARCH_AUTOCOMPLETE).buildUpon()
                        .appendQueryParameter("q", mSearchQuery).build().toString();
        }

        startDownload();
    }

    @Override
    protected void onDownloadSuccess(String response) {
        switch (mOpType) {
            default:
            case SEARCH_TOP:
                try {
                    JSONObject object = new JSONObject(response).getJSONObject(KEY_DATA);
                    // Parse users
                    JSONArray users = object.getJSONObject("users").getJSONArray(KEY_DATA);
                    List<UserProfile> userList = new ArrayList<>(users.length());
                    mUserNextUrl = super.parseNextUrl(object.getJSONObject("users"));
                    parseUsers(users, userList);

                    // Parse groups
                    JSONArray groups = object.getJSONObject("groups").getJSONArray(KEY_DATA);
                    List<Group> groupList = new ArrayList<>(groups.length());
                    mGroupNextUrl = super.parseNextUrl(object.getJSONObject("groups"));
                    parseGroups(groups, groupList);

                    // Parse bulletins
                    JSONArray bulletins = object.getJSONObject("posts").getJSONArray(KEY_DATA);
                    List<Bulletin> bulletinList = new ArrayList<>(bulletins.length());
                    mBulletinNextUrl = super.parseNextUrl(object.getJSONObject("posts"));
                    parseBulletins(bulletins, bulletinList);

                    // Save data
                    DataAccess dataAccess = DataAccess.getInstance(mContextRef.get());
                    if (shouldClearData()) {
                        dataAccess.clearSearch();
                        dataCleared();
                    }
                    dataAccess.mUserSearchList.addAll(userList);
                    dataAccess.userListSize += userList.size();
                    dataAccess.mGroupSearchList.addAll(groupList);
                    dataAccess.groupListSize += groupList.size();
                    dataAccess.mBulletinSearchList.addAll(bulletinList);
                    dataAccess.bulletinListSize += bulletinList.size();

                    // Notify UI
                    EventBus.getDefault().post(new DownloadEvent(true, OperationType.SEARCH_TOP));
                } catch (JSONException e) {
                    EventBus.getDefault().post(new DownloadEvent(false, OperationType.SEARCH_TOP));
                    Log.e(getTag(), "Failed parsing search results.", e);
                }
                break;
            case SEARCH_PROFILES:
                try {
                    JSONObject object = new JSONObject(response);
                    // Parse users
                    JSONArray users = object.getJSONArray(KEY_DATA);
                    List<UserProfile> userList = new ArrayList<>(users.length());
                    mUserNextUrl = super.parseNextUrl(object);
                    parseUsers(users, userList);

                    // Save data
                    DataAccess dataAccess = DataAccess.getInstance(mContextRef.get());
                    if (shouldClearData()) {
                        dataAccess.clearSearch();
                        dataCleared();
                    }
                    dataAccess.mUserSearchList.addAll(userList);
                    dataAccess.userListSize += userList.size();

                    // Notify UI
                    EventBus.getDefault().post(new DownloadEvent(true, SEARCH_PROFILES));
                } catch (JSONException e) {
                    EventBus.getDefault().post(new DownloadEvent(false, SEARCH_PROFILES));

                    Log.e(getTag(), "Failed parsing profile search results.", e);
                }
                break;
            case SEARCH_GROUPS:
                try {
                    JSONObject object = new JSONObject(response);
                    // Parse users
                    JSONArray users = object.getJSONArray(KEY_DATA);
                    List<Group> groupList = new ArrayList<>(users.length());
                    mGroupNextUrl = super.parseNextUrl(object);
                    parseGroups(users, groupList);

                    // Save data
                    DataAccess dataAccess = DataAccess.getInstance(mContextRef.get());
                    if (shouldClearData()) {
                        dataAccess.clearSearch();
                        dataCleared();
                    }
                    dataAccess.mGroupSearchList.addAll(groupList);
                    dataAccess.groupListSize += groupList.size();

                    // Notify UI
                    EventBus.getDefault().post(new DownloadEvent(true, SEARCH_GROUPS));
                } catch (JSONException e) {
                    EventBus.getDefault().post(new DownloadEvent(false, SEARCH_GROUPS));
                    Log.e(getTag(), "Failed parsing group search results.", e);
                }
                break;
            case SEARCH_BULLETINS:
                try {
                    JSONObject object = new JSONObject(response);
                    // Parse users
                    JSONArray users = object.getJSONArray(KEY_DATA);
                    List<Bulletin> bulletinList = new ArrayList<>(users.length());
                    mBulletinNextUrl = super.parseNextUrl(object);
                    parseBulletins(users, bulletinList);

                    // Save data
                    DataAccess dataAccess = DataAccess.getInstance(mContextRef.get());
                    if (shouldClearData()) {
                        dataAccess.clearSearch();
                        dataCleared();
                    }
                    dataAccess.mBulletinSearchList.addAll(bulletinList);
                    dataAccess.bulletinListSize += bulletinList.size();

                    // Notify UI
                    EventBus.getDefault().post(new DownloadEvent(true, SEARCH_BULLETINS));
                } catch (JSONException e) {
                    EventBus.getDefault().post(new DownloadEvent(false, SEARCH_BULLETINS));
                    Log.e(getTag(), "Failed parsing search results.", e);
                }
                break;

            case SEARCH_AUTOCOMPLETE:
                try {
                    JSONArray array = new JSONObject(response).getJSONArray(KEY_DATA);
                    int size = array.length();
                    List<String> data = new ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        data.add(array.get(i).toString());
                    }
                    EventBus.getDefault().post(new SearchHintEvent(data));
                } catch (JSONException e) {
                    EventBus.getDefault().post(new SearchHintEvent(new ArrayList<String>()));
                    Log.e(getTag(), "Failed parsing search autocomplete results.", e);
                }
                break;
        }
    }

    private void parseBulletins(JSONArray bulletins, List<Bulletin> bulletinList) {
        int size = bulletins.length();
        for (int i = 0; i < size; i++) {
            try {
                Bulletin b = new Bulletin().parse(bulletins.getJSONObject(i));
                bulletinList.add(b);
            } catch (JSONException e) {
                Log.e(getTag(), "Failed to parse bulletin.", e);
            }
        }
    }

    private void parseGroups(JSONArray groups, List<Group> groupList) {
        int size = groups.length();
        for (int i = 0; i < size; i++) {
            try {
                Group g = new Group().parse(groups.getJSONObject(i));
                groupList.add(g);
            } catch (JSONException e) {
                Log.e(getTag(), "Failed to parse group.", e);
            }
        }
    }

    private void parseUsers(JSONArray users, List<UserProfile> userList) {
        int size = users.length();
        for (int i = 0; i < size; i++) {
            try {
                UserProfile profile = new UserProfile().parse(users.getJSONObject(i));
                userList.add(profile);
            } catch (JSONException e) {
                Log.e(getTag(), "Failed to parse user profile.", e);
            }
        }
    }

    @Override
    protected void onDownloadFailed(VolleyError error) {

    }

    @Override
    public void startUploadAction() {
    }

    @Override
    protected void onUploadSuccess(String response) {

    }

    @Override
    protected void onUploadFailed(VolleyError error) {

    }

    @Override
    protected void clearNextUrl() {
        mBulletinNextUrl = "";
        mGroupNextUrl = "";
        mUserNextUrl = "";
    }

    @Override
    protected String getTag() {
        return "SearchOp";
    }
}
