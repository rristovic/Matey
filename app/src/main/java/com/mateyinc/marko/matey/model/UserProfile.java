package com.mateyinc.marko.matey.model;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.mateyinc.marko.matey.data.DataContract;

import java.util.Locale;

public class UserProfile extends MModel{
    private static final String TAG = UserProfile.class.getSimpleName();

    public static final String USER_ID = "userid";
    public static final String FIRST_NAME = "firstname";
    public static final String LAST_NAME = "lastname";
    public static final String LAST_MSG_ID = "lastmsgid";
    public static final String EMAIL = "email";

    private String firstName;
    private String lastName;
    private String email;
    private String profilePictureLink;
    private String gender;
    private String birthday;
    private String hometown;
    private String location;
    private String quoteStatus;

    private int lastMsgId;
    private int numOfPosts;
    private int numOfFriends;
    private boolean mIsFriend = false;

    public UserProfile() {
    }

    public UserProfile(long userId, String firstName, String lastName, String email, String picture) {
        this._id = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.profilePictureLink = picture;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getUserId() {
        return _id;
    }

    public void setUserId(long userId) {
        this._id = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getProfilePictureLink() {
        return profilePictureLink;
    }

    public void setProfilePictureLink(String profilePictureLink) {
        this.profilePictureLink = profilePictureLink;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getHometown() {
        return hometown;
    }

    public void setHometown(String hometown) {
        this.hometown = hometown;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getQuoteStatus() {
        return quoteStatus;
    }

    public void setQuoteStatus(String quoteStatus) {
        this.quoteStatus = quoteStatus;
    }

    public int getNumOfPosts() {
        return numOfPosts;
    }

    public void setNumOfPosts(int numOfPosts) {
        this.numOfPosts = numOfPosts;
    }

    public int getNumOfFriends() {
        return numOfFriends;
    }

    public void setNumOfFriends(int numOfFriends) {
        this.numOfFriends = numOfFriends;
    }

    public void setLastMsgId(int msgId) {
        lastMsgId = msgId;
    }

    public int getLastMsgId() {
        return lastMsgId;
    }

    public boolean isFriend() {
        return mIsFriend;
    }

    public void setFriend(boolean isFriend) {
        mIsFriend = isFriend;
    }

    public void setData(UserProfile currentUserProfile) {
        this.firstName = currentUserProfile.firstName;
        this.lastName = currentUserProfile.lastName;
        this.mIsFriend = currentUserProfile.mIsFriend;
        this.birthday = currentUserProfile.birthday;
        this.email = currentUserProfile.email;
        this.gender = currentUserProfile.gender;
        this.numOfFriends = currentUserProfile.numOfFriends;
        this.numOfPosts = currentUserProfile.numOfPosts;
        this.lastMsgId = currentUserProfile.lastMsgId;
        this.profilePictureLink = currentUserProfile.profilePictureLink;
        this.hometown = currentUserProfile.hometown;
        this.location = currentUserProfile.location;
        this.quoteStatus = currentUserProfile.quoteStatus;
        this._id = currentUserProfile._id;
    }

    @Override
    public void upload(Context context, RequestQueue queue, String accessToken) {

    }

    @Override
    protected void uploadSucceeded(String response, Context context) {

    }

    @Override
    protected void uploadFailed(Exception e, Context context) {

    }

    @Override
    public void addToDb(Context c) {
        ContentValues userValues = new ContentValues();
        userValues.put(DataContract.ProfileEntry._ID, _id);
        userValues.put(DataContract.ProfileEntry.COLUMN_NAME, firstName);
        userValues.put(DataContract.ProfileEntry.COLUMN_LAST_NAME, lastName);
        userValues.put(DataContract.ProfileEntry.COLUMN_EMAIL, email);
        userValues.put(DataContract.ProfileEntry.COLUMN_PICTURE, profilePictureLink);
        userValues.put(DataContract.ProfileEntry.COLUMN_IS_FRIEND, mIsFriend);
        userValues.put(DataContract.ProfileEntry.COLUMN_LAST_MSG_ID, lastMsgId);

        // Add to db
        Uri insertedUri = c.getContentResolver().insert(
                DataContract.ProfileEntry.CONTENT_URI,
                userValues);

        if (insertedUri == null) {
            Log.e(TAG, "Error inserting: " + UserProfile.this);
        } else {
            Log.d(TAG, "New profile added: " + UserProfile.this);
        }
    }

    @Override
    public void download(Context context, RequestQueue queue, String accessToken) {

    }

    @Override
    public String toString() {
        return String.format(Locale.US,"UserProfileOps: ID=%d; UserName:%s %s; Email:%s; PicLink:%s", _id, firstName, lastName, email, profilePictureLink);
    }
}
