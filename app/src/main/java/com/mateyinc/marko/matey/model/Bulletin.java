package com.mateyinc.marko.matey.model;

import android.util.Log;

import com.mateyinc.marko.matey.data_and_managers.DataManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

/**
 * Created by Sarma on 5/12/2016.
 */
public class Bulletin {
    private String TAG = Bulletin.class.getSimpleName();

    private String mFirstName;
    private String mLastName;
    private Date mDate;
    private String mMessage;
    private int mPostID;
    private int mUserID;
    private LinkedList<Attachment> mAttachments;

    private int noOfReplies = 0;

    public int getNumOfReplies() {
        return noOfReplies;
    }

    public void setNumOfReplies(int noOfReplies) {
        this.noOfReplies = noOfReplies;
    }

    public int mDbIndex;


    public int getPostID() {
        return mPostID;
    }

    public void setPostID(int mPostID) {
        this.mPostID = mPostID;
    }

    public int getUserID() {
        return mUserID;
    }

    public void setUserID(int mUserID) {
        this.mUserID = mUserID;
    }


    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String mMessage) {
        this.mMessage = mMessage;
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

    public LinkedList<Attachment> getAttachments() {
        return mAttachments;
    }

    public void setAttachmentsFromJSON(LinkedList<Attachment> mAttachments) {
        this.mAttachments = mAttachments;
    }

    public Reply getReplyInstance() {
        return new Reply();
    }

    public Interest getInterestInstance() {
        return new Interest();
    }


    @Override
    public String toString() {
        return "Message: " + mMessage.substring(0, 10)
                + "; From: " + mFirstName + " " + mLastName
                + "; Date: " + mDate +
                "UserID=" + mUserID + "; ReplyPostId=" + mPostID;
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
                r.replyDate = replyJson.getString(Reply.DATE);
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
        public int replyId;
        /**
         * The ID of the user that has replied
         */
        public int userId;
        /**
         * The ID of the post/bulletin that has been replied on
         */
        public int postId;

        public String userFirstName;
        public String userLastName;
        public String replyDate; // TODO - make date object if needed
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

        @Override
        public String toString() {
            return "Reply: " + "id=" + replyId
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
