package com.mateyinc.marko.matey.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

/**
 * Created by Sarma on 5/12/2016.
 */
public class Bulletin {
    private String mFirstName;
    private String mLastName;
    private Date mDate;
    private String mMessage;
    private int mPostID;
    private int mUserID;
    private LinkedList<BulletinAttachment> mAttachments;
    private LinkedList<Reply> mReplies;

    public LinkedList<Reply> getReplies() {
        if (mReplies == null)
            mReplies = new LinkedList<>();
        return mReplies;
    }

    public void setReplies(LinkedList<Reply> mReplies) {
        this.mReplies = mReplies;
    }

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
        }

    }

    public void setDate(Date date) {
        this.mDate = date;
    }

    public LinkedList<BulletinAttachment> getAttachments() {
        return mAttachments;
    }

    public void setAttachments(LinkedList<BulletinAttachment> mAttachments) {
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
        return "Message: " + mMessage
                + "; From: " + mFirstName + " " + mLastName
                + "; Date: " + mDate;
    }

    public LinkedList<Reply> setReplies(String string) {
        return null; // TODO - finish method
    }

    public LinkedList<Attachment> setAttachments(String string) {
        return null; // TODO - finish method
    }

    public class Reply {
        public int replyId;
        public String userFirstName;
        public String userLastName;
        public int userId;
        public String replyDate; // TODO - make date object if needed
        public String replyText;
        public LinkedList<UserProfile> replyApproves;

        public Reply() {
            replyApproves = new LinkedList<>();
        }

        public boolean hasReplyApproveWithId(int id){
            for (UserProfile p :
                    replyApproves) {
                if (p != null && p.getUserId() == id)
                    return true;
            }
            return false;
        }

        public boolean removeReplyApproveWithId(int id){
            for (UserProfile p :
                    replyApproves) {
                if (p != null && p.getUserId() == id) {
                    replyApproves.remove();
                    return true;
                }
            }
            return false;
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
