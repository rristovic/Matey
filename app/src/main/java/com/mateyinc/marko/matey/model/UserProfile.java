package com.mateyinc.marko.matey.model;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.mateyinc.marko.matey.data.DataContract;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Vector;

public class UserProfile extends MModel{
    private static final String TAG = UserProfile.class.getSimpleName();

    public static final String USER_ID = "userid";
    public static final String FIRST_NAME = "firstname";
    public static final String LAST_NAME = "lastname";
    public static final String LAST_MSG_ID = "lastmsgid";
    public static final String EMAIL = "email";

    // Json keys in response from the server TODO - finish keys
    public static final String KEY_ID = "user_id";
    public static final String KEY_FIRST_NAME = "first_name";
    public static final String KEY_LAST_NAME = "last_name";
    public static final String KEY_FULL_NAME = "full_name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PROFILE_PIC = "picture_url";
    public static final String KEY_COVER_PIC = "cover_url";
    public static final String KEY_FOLLOWERS_NUM = "num_of_followers";
    public static final String KEY_FOLLOWING_NUM = "num_of_following";
    public static final String KEY_VERIFIED = "verified";
    public static final String KEY_FOLLOWING = "following";

    private String firstName;
    private String lastName;
    private String email;
    private String profilePictureLink;
    private String coverPictureLink;
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

    public String getCoverPictureLink() {
        return coverPictureLink;
    }

    public void setCoverPictureLink(String coverPictureLink) {
        this.coverPictureLink = coverPictureLink;
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

    public void copy(UserProfile currentUserProfile) {
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

    /**
     * Method for saving multiple values into the database
     * @param cVVector {@link Vector} object which contains {@link ContentValues} object values;
     * @param c context used for db control
     * @param isNew boolean that indicates if this data is new or not, thus updates it or just inerts it into db
     */
    public void saveToDbMultiple(Vector<ContentValues> cVVector, Context c, boolean isNew){
    }

    public static void saveToDb(String response, Context c) {
        try {
            JSONObject object =  new JSONObject(response);

            // Parsing
            Long id = object.getLong(KEY_ID);
            String name = object.getString(KEY_FIRST_NAME);
            String lastName = object.getString(KEY_LAST_NAME);
            String fullName = object.getString(KEY_FULL_NAME);
            String email = object.getString(KEY_EMAIL);
            String picLink = object.getString(KEY_PROFILE_PIC);
            String coverLink = object.getString(KEY_COVER_PIC);
            int followersNum = object.getInt(KEY_FOLLOWERS_NUM);
            int followingNum = object.getInt(KEY_FOLLOWING_NUM);
//            boolean verified = object.getBoolean(KEY_VERIFIED);

            // Saving
            ContentValues userValues = new ContentValues();
            userValues.put(DataContract.ProfileEntry._ID, id);
            userValues.put(DataContract.ProfileEntry.COLUMN_NAME, name);
            userValues.put(DataContract.ProfileEntry.COLUMN_LAST_NAME, lastName);
            userValues.put(DataContract.ProfileEntry.COLUMN_EMAIL, email);
            userValues.put(DataContract.ProfileEntry.COLUMN_PROF_PIC, picLink);
            userValues.put(DataContract.ProfileEntry.COLUMN_COVER_PIC, coverLink);
            userValues.put(DataContract.ProfileEntry.COLUMN_FOLLOWERS_NUM, followersNum);
            userValues.put(DataContract.ProfileEntry.COLUMN_FOLLOWING_NUM, followingNum);

//            userValues.put(DataContract.ProfileEntry.COLUMN_IS_FRIEND, mIsFriend);
//            userValues.put(DataContract.ProfileEntry.COLUMN_LAST_MSG_ID, lastMsgId);

            // Add to db
            Uri uri = c.getContentResolver().insert(
                    DataContract.ProfileEntry.CONTENT_URI, userValues);

//            // Debug
//            String debugString = toString();
//            if (uri == null ) {
//                Log.e(TAG, "Error inserting " + debugString);
//            } else {
//                Log.d(TAG, "New profile added: " + debugString);
//            }
        } catch (JSONException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }
    }

    public void save(Context c) {
        ContentValues userValues = new ContentValues();
        userValues.put(DataContract.ProfileEntry._ID, _id);
        userValues.put(DataContract.ProfileEntry.COLUMN_NAME, firstName);
        userValues.put(DataContract.ProfileEntry.COLUMN_LAST_NAME, lastName);
        userValues.put(DataContract.ProfileEntry.COLUMN_EMAIL, email);
        userValues.put(DataContract.ProfileEntry.COLUMN_PROF_PIC, profilePictureLink);
        userValues.put(DataContract.ProfileEntry.COLUMN_FOLLOWED, mIsFriend);
        userValues.put(DataContract.ProfileEntry.COLUMN_LAST_MSG_ID, lastMsgId);
        userValues.put(DataContract.ProfileEntry.COLUMN_COVER_PIC, coverPictureLink);

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
    public String toString() {
        return String.format(Locale.US,"UserProfileOps: ID=%d; UserName:%s %s; Email:%s; PicLink:%s", _id, firstName, lastName, email, profilePictureLink);
    }
}
