package com.mateyinc.marko.matey.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.mateyinc.marko.matey.data.DataManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

public class Bulletin extends MModel {
    private String TAG = Bulletin.class.getSimpleName();

    // Keys for JSON data from the server
    public static final String KEY_DATA = "data";
    public static final String KEY_POST_ID = "post_id";
    public static final String KEY_TEXT = "text";
    public static final String KEY_STATISTICS = "statistics";
    public static final String KEY_NUM_OF_RESPONSES = "num_of_responses";
    public static final String KEY_NUM_OF_SHARES = "num_of_shares";
    public static final String KEY_LAST_USER_RESPOND = "last_user_respond";

    private String mFirstName;
    private String mLastName;
    private Date mDate;
    private String mText;
    private long mPostID;
    private long mUserID;
    private LinkedList<Attachment> mAttachments;

    public Bulletin(long post_id, long user_id, String firstName, String lastName, String text, Date date) {
        mPostID = post_id;
        mUserID = user_id;
        mFirstName = firstName;
        mLastName = lastName;
        mText = text;
        mDate = date;
    }

    public Bulletin(long post_id, long user_id, String firstName, String lastName, String text, Date date, int serverStatus) {
        mPostID = post_id;
        mUserID = user_id;
        mFirstName = firstName;
        mLastName = lastName;
        mText = text;
        mDate = date;
        this.setServerStatus(serverStatus);
    }

    private int noOfReplies = 0;

    public int getNumOfReplies() {
        return noOfReplies;
    }

    public void setNumOfReplies(int noOfReplies) {
        this.noOfReplies = noOfReplies;
    }


    public long getPostID() {
        return mPostID;
    }

    public void setPostID(int mPostID) {
        this.mPostID = mPostID;
    }

    public long getUserID() {
        return mUserID;
    }

    public void setUserID(int mUserID) {
        this.mUserID = mUserID;
    }


    public String getMessage() {
        return mText;
    }

    public void setMessage(String mMessage) {
        this.mText = mMessage;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public void setFirstName(String mFirstName) {
        this.mFirstName = mFirstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public void setLastName(String mLastName) {
        this.mLastName = mLastName;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(String mDateString) {

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            this.mDate = dateFormat.parse(mDateString);
        } catch (Exception e) {
            try {
                this.mDate = new Date(mDateString);
            } catch (Exception es) {
                Log.e(TAG, es.getLocalizedMessage(), es);
                this.mDate = new Date();
            }
        }

    }

    public void setDate(Date date) {
        this.mDate = date;
    }

    public void setDate(long timeInMilis) {
        this.mDate = new Date(timeInMilis);
    }

    /**
     * Method for parsing the date string into {@link Date}
     *
     * @param date the date string retrieved from the server in format "YYYY-mm-DD HH:MM:SS"
     * @return the Date object represented by the date argument
     */
    public static Date parseDate(String date) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return format.parse(date);
    }

    public LinkedList<Attachment> getAttachments() {
        return mAttachments;
    }

    public void setAttachmentsFromJSON(LinkedList<Attachment> mAttachments) {
        this.mAttachments = mAttachments;
    }

    public Reply getReplyInstance() {
        return new Reply();
    }

    public Bulletin() {
    }

    public Interest getInterestInstance() {
        return new Interest();
    }

    public void setRepliesFromJSON(String string) {
        LinkedList<Reply> replies = new LinkedList<>();

        try {
            JSONObject jObject = new JSONObject(string);
            JSONArray jArray = jObject.getJSONArray(DataManager.REPLIES_LIST);
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject replyJson = jArray.getJSONObject(i);

                Reply r = new Reply();
                r.replyId = replyJson.getInt(Reply.REPLY_ID);
                r.userId = replyJson.getInt(Reply.USER_ID);
                r.userFirstName = replyJson.getString(Reply.FIRST_NAME);
                r.userLastName = replyJson.getString(Reply.LAST_NAME);
                r.replyDate = new Date(replyJson.getString(Reply.DATE));
                r.replyText = replyJson.getString(Reply.TEXT);

                JSONArray replyApprvs = replyJson.getJSONArray(DataManager.REPLY_APPRVS);
                for (int j = 0; j < replyApprvs.length(); j++) {
                    JSONObject userProfile = replyApprvs.getJSONObject(j);

                    UserProfile profile = new UserProfile();
                    profile.setUserId(userProfile.getInt(UserProfile.USER_ID));
//                    profile.setFirstName(userProfile.getString(UserProfile.FIRST_NAME));
//                    profile.setLastName(userProfile.getString(UserProfile.LAST_NAME));
                    profile.setLastMsgId(userProfile.getInt(UserProfile.LAST_MSG_ID));

                    r.replyApproves.add(profile);
                }

                replies.add(r);
            }
        } catch (JSONException jse) {
            Log.e(TAG, jse.getLocalizedMessage(), jse);
        }

//        mReplies = replies;
    }

    public void setAttachmentsFromJSON(String string) {
        return; // TODO - finish method
    }

    /**
     * Method for parsing JSON response into new {@link Bulletin}
     *
     * @param response string response retrieved from the server
     * @return A Bulletin object made from JSON data
     */
    public static Bulletin parseBulletin(String response) throws JSONException, ParseException {
        JSONObject object = new JSONObject(response);
        JSONObject dataObject = object.getJSONObject(KEY_DATA);

        Bulletin b = new Bulletin(
                dataObject.getLong(KEY_POST_ID),
                object.getLong(KEY_USER_ID),
                object.getString(KEY_FIRSTNAME),
                object.getString(KEY_LASTNAME),
                dataObject.getString(KEY_TEXT),
                parseDate(object.getString(KEY_DATE_ADDED))
        );

        return b;
    }

    public Bulletin(Parcel source){
        mPostID = source.readLong();
        mUserID = source.readLong();
        mFirstName = source.readString();
        mLastName = source.readString();
        mText = source.readString();
        mServerStatus = source.readInt();
        mDate = (Date)source.readSerializable();
        source.readList(mAttachments, Attachment.class.getClassLoader());
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator<Bulletin>(){

        @Override
        public Bulletin createFromParcel(Parcel source) {
            return new Bulletin(source);
        }

        @Override
        public Bulletin[] newArray(int size) {
            return new Bulletin[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mPostID);
        dest.writeLong(mUserID);
        dest.writeString(mFirstName);
        dest.writeString(mLastName);
        dest.writeString(mText);
        dest.writeInt(mServerStatus);
        dest.writeSerializable(mDate);
        dest.writeList(mAttachments);
    }

    @Override
    public String toString() {
        return "Message: " + mText.substring(0, 10)
                + "; From: " + mFirstName + " " + mLastName
                + "; Date: " + mDate +
                "UserID=" + mUserID + "; ReplyPostId=" + mPostID;
    }




    public class Reply {
        public static final String REPLY_ID = "reply_id";
        public static final String FIRST_NAME = "reply_username";
        public static final String LAST_NAME = "reply_lastname";
        public static final String USER_ID = "reply_userid";
        public static final String DATE = "reply_date";
        public static final String TEXT = "reply_text";
        public static final String REPLY_APPRVS = "reply_approves";

        /**
         * The ID of the reply
         */
        public long replyId;
        /**
         * The ID of the user that has replied
         */
        public long userId;
        /**
         * The ID of the post/bulletin that has been replied on
         */
        public long postId;

        public String userFirstName;
        public String userLastName;
        public Date replyDate;
        public String replyText;
        public int numOfApprvs;
        public LinkedList<UserProfile> replyApproves;// Not in DB

        public Reply() {
            replyApproves = new LinkedList<>();
        }

        public boolean hasReplyApproveWithId(int id) {
            for (UserProfile p :
                    replyApproves) {
                if (p != null && p.getUserId() == id)
                    return true;
            }
            return false;
        }

        public boolean removeReplyApproveWithId(int id) {
            for (UserProfile p :
                    replyApproves) {
                if (p != null && p.getUserId() == id) {
                    replyApproves.remove();
                    return true;
                }
            }
            return false;
        }

        public LinkedList<UserProfile> setApprovesFromJSON(String string) {
            LinkedList<UserProfile> apprvs = new LinkedList<>();
            if (string == null || string.length() == 0) {
                return apprvs;
            }

            try {
                JSONObject jObject = new JSONObject(string);
                JSONArray jArray = jObject.getJSONArray(DataManager.REPLY_APPRVS);
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject apprv = jArray.getJSONObject(i);

                    UserProfile r = new UserProfile();
                    r.setUserId(apprv.getInt(UserProfile.USER_ID));

                    apprvs.add(r);
                }
            } catch (JSONException jse) {
                Log.e(TAG, jse.getLocalizedMessage(), jse);
            }

            return apprvs;
        }

        public void setDate(String mDateString) {

            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                replyDate = dateFormat.parse(mDateString);
            } catch (Exception e) {
                try {
                    replyDate = new Date(mDateString);
                } catch (Exception es) {
                    Log.e(TAG, es.getLocalizedMessage(), es);
                    replyDate = new Date();
                }
            }

        }

        public void setDate(Date date) {
            replyDate = date;
        }

        public void setDate(long timeInMilis) {
            replyDate = new Date(timeInMilis);
        }


        @Override
        public String toString() {
            return "Reply: " + "id=" + replyId + "; postId=" + postId
                    + "; From: " + userFirstName + " " + userLastName
                    + "; Date: " + replyDate +
                    "; Text=" + replyText;
        }
    }

    public class Interest {
        public int replyId;
        public String userFirstName;
        public String userLastName;
        public int userId;
        public String replyDate;
        public String replyText;
        public LinkedList<UserProfile> replyApproves;

        public Interest() {
        }
    }

    public class Attachment {
        public int replyId;
        public String userFirstName;
        public String userLastName;
        public int userId;
        public String replyDate;
        public String replyText;
        public LinkedList<UserProfile> replyApproves;

        public Attachment() {
        }
    }
}
