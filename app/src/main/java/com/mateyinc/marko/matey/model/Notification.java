package com.mateyinc.marko.matey.model;

import android.content.Context;
import android.content.Intent;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.Util;
import com.mateyinc.marko.matey.activity.home.HomeActivity;
import com.mateyinc.marko.matey.activity.profile.ProfileActivity;
import com.mateyinc.marko.matey.activity.view.BulletinViewActivity;
import com.mateyinc.marko.matey.activity.view.GroupActivity;
import com.mateyinc.marko.matey.inall.MotherActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class Notification extends MModel{

    private static final String KEY_ACTIVITY_ID = "activity_id";
    private static final String KEY_ACTIVITY_TYPE = "activity_type";
    // Activity type values
    private static final String VALUE_TYPE_APPROVE = "APPROVE";
    private static final String VALUE_TYPE_BOOST = "BOOST";
    private static final String VALUE_TYPE_FOLLOW = "FOLLOW";
    private static final String VALUE_TYPE_POST_CREATE = "POST_CREATE";
    private static final String VALUE_TYPE_REPLY_CREATE = "REPLY_CREATE";
    private static final String VALUE_TYPE_RE_REPLY_CREATE = "REREPLY_CREATE";
    private static final String VALUE_TYPE_GROUP_CREATE = "GROUP_CREATE";

    private static final String KEY_TIME_C = "time_c";
    private static final String KEY_TITLE = "title";
    private static final String KEY_PIC_URL = "picture_url";
    private static final String KEY_MSG = "message";

    private static final String KEY_BULLETIN = "post";
    private static final String KEY_USER = "user";
    private static final String KEY_USER_FOLLOWED = "user_followed";
    private static final String KEY_REPLY = "reply";
    private static final String KEY_REREPLY = "rereply";
    private static final String KEY_GROUP = "group";

    private static final String KEY_SOURCE_TYPE = "source_type";
    private static final String KEY_PARENT_TYPE = "parent_type";
    // Source type and parent type values
    private static final String VALUE_TYPE_GROUP = "GROUP";
    private static final String VALUE_TYPE_USER = "MATEY_USER";
    private static final String VALUE_TYPE_BULLETIN = "POST";
    private static final String VALUE_TYPE_REPLY = "REPLY";
    private static final String VALUE_TYPE_REREPLY = "REREPLY";

    // Model position in array
    private static final int BULLETIN_POS = 0;
    private static final int REPLY_POS = 1;
    private static final int REREPLY_POS = 2;
    private int USER_GENERATED_POS;
    private static final int USER_FOLLOWED_POS = 0;
    private static final int GROUP_POS = 1;

    private String mActivityType;
    private Class mClass;
    private UserProfile mUserProfile;
    private Date mTimeCreated;
    private String mUrl;

    private MModel[] mModelArray;
    // Indicates if this notification object is listed in activity on profile view.
    private boolean isActivity;

    public Notification() {
    }

    public Notification(boolean isActivity) {
        this.isActivity = isActivity;
    }

    @Override
    public void onDownloadSuccess(String response, Context c) {

    }

    @Override
    public void onDownloadFailed(String error, Context c) {

    }

    @Override
    public void onUploadSuccess(String response, Context c) {

    }

    @Override
    public void onUploadFailed(String error, Context c) {

    }

    public Notification parse(JSONObject object) throws JSONException {
        mUserProfile = new UserProfile().parse(object.getJSONObject(KEY_USER));
        mTimeCreated = Util.parseDate(object.getString(KEY_TIME_C));
        mActivityType = object.getString(KEY_ACTIVITY_TYPE);
        switch (mActivityType) {
            case VALUE_TYPE_BOOST:
                mClass = BulletinViewActivity.class;
                mModelArray = new MModel[2];
                mModelArray[BULLETIN_POS] = new Bulletin().parse(object.getJSONObject(KEY_BULLETIN));
                parseUserGenerated(object);
                mUrl = ((UserProfile) mModelArray[USER_GENERATED_POS]).getProfilePictureLink();
                break;
            case VALUE_TYPE_APPROVE:
                mClass = BulletinViewActivity.class;
                String sourceType = object.getString(KEY_SOURCE_TYPE);
                if (sourceType.equals(VALUE_TYPE_REPLY)) {
                    mModelArray = new MModel[3];
                    mModelArray[BULLETIN_POS] = new Bulletin().parse(object.getJSONObject(KEY_BULLETIN));
                    mModelArray[REPLY_POS] = new Reply().parse(object.getJSONObject(KEY_REPLY));
                    parseUserGenerated(object);
                } else {
                    mModelArray = new MModel[4];
                    mModelArray[BULLETIN_POS] = new Bulletin().parse(object.getJSONObject(KEY_BULLETIN));
                    mModelArray[REPLY_POS] = new Reply().parse(object.getJSONObject(KEY_REPLY));
                    mModelArray[REREPLY_POS] = new Reply().parse(object.getJSONObject(KEY_REREPLY));
                    parseUserGenerated(object);
                }
                mUrl = ((UserProfile) mModelArray[USER_GENERATED_POS]).getProfilePictureLink();
                break;
            case VALUE_TYPE_POST_CREATE:
                mClass = BulletinViewActivity.class;
                mModelArray = new MModel[1];
                mModelArray[BULLETIN_POS] = new Bulletin().parse(object.getJSONObject(KEY_BULLETIN));
                mUrl = ((Bulletin) mModelArray[BULLETIN_POS]).getUserProfile().getProfilePictureLink();
                break;
            case VALUE_TYPE_REPLY_CREATE:
                mClass = BulletinViewActivity.class;
                mModelArray = new MModel[2];
                mModelArray[BULLETIN_POS] = new Bulletin().parse(object.getJSONObject(KEY_BULLETIN));
                mModelArray[REPLY_POS] = new Reply().parse(object.getJSONObject(KEY_REPLY));
                mUrl = ((Bulletin) mModelArray[BULLETIN_POS]).getUserProfile().getProfilePictureLink();
                break;
            case VALUE_TYPE_RE_REPLY_CREATE:
                mClass = BulletinViewActivity.class;
                mModelArray = new MModel[3];
                mModelArray[BULLETIN_POS] = new Bulletin().parse(object.getJSONObject(KEY_BULLETIN));
                mModelArray[REPLY_POS] = new Reply().parse(object.getJSONObject(KEY_REPLY));
                mModelArray[REREPLY_POS] = new Reply().parse(object.getJSONObject(KEY_REREPLY));
                mUrl = ((Bulletin) mModelArray[BULLETIN_POS]).getUserProfile().getProfilePictureLink();
                break;
            case VALUE_TYPE_GROUP_CREATE:
                mClass = GroupActivity.class;
                mModelArray = new MModel[1];
                mModelArray[GROUP_POS] = new Group().parse(object.getJSONObject(KEY_GROUP));
                mUrl = ((Group) mModelArray[GROUP_POS]).getUserProfile().getProfilePictureLink();
                break;
            case VALUE_TYPE_FOLLOW:
                mClass = ProfileActivity.class;
                mModelArray = new MModel[2];
                mModelArray[USER_FOLLOWED_POS] = new UserProfile().parse(object.getJSONObject(KEY_USER_FOLLOWED));
                parseUserGenerated(object);
                mUrl = ((UserProfile) mModelArray[USER_GENERATED_POS]).getProfilePictureLink();
                break;

            default:
                throw new JSONException("Not implemented.");
        }

        return this;
    }

    private void parseUserGenerated(JSONObject object) throws JSONException {
        mModelArray[USER_GENERATED_POS = mModelArray.length - 1] = new UserProfile().parse(object.getJSONObject(KEY_USER));
    }

    public Intent buildIntent(Context context) {
        Intent i = new Intent(context, mClass);
        String action;
        switch (mActivityType) {
            case VALUE_TYPE_GROUP_CREATE:
                i.putExtra(GroupActivity.EXTRA_GROUP_ID, mModelArray[GROUP_POS].getId());
                action = HomeActivity.ACTION_SHOW_GROUP;
                break;
            case VALUE_TYPE_APPROVE:
                i.putExtra(BulletinViewActivity.EXTRA_BULLETIN_ID, mModelArray[BULLETIN_POS].getId());
                action = HomeActivity.ACTION_SHOW_REREPLY;
                break;
            case VALUE_TYPE_BOOST:
                i.putExtra(BulletinViewActivity.EXTRA_BULLETIN_ID, mModelArray[BULLETIN_POS].getId());
                action = HomeActivity.ACTION_SHOW_BULLETIN;
                break;
            case VALUE_TYPE_FOLLOW:
                i.putExtra(ProfileActivity.EXTRA_PROFILE_ID, mModelArray[USER_GENERATED_POS].getId());
                action = HomeActivity.ACTION_SHOW_PROFILE;
                break;
            case VALUE_TYPE_POST_CREATE:
                i.putExtra(BulletinViewActivity.EXTRA_BULLETIN_ID, mModelArray[BULLETIN_POS].getId());
                action = HomeActivity.ACTION_SHOW_BULLETIN;
                break;
            case VALUE_TYPE_REPLY_CREATE:
                i.putExtra(BulletinViewActivity.EXTRA_BULLETIN_ID, mModelArray[BULLETIN_POS].getId());
                action = HomeActivity.ACTION_SHOW_BULLETIN;
                break;
            case VALUE_TYPE_RE_REPLY_CREATE:
                i.putExtra(BulletinViewActivity.EXTRA_BULLETIN_ID, mModelArray[BULLETIN_POS].getId());
                action = HomeActivity.ACTION_SHOW_BULLETIN;
                break;
            default:
                return null;
        }

        i.setAction(action);
        return i;
    }

    /**
     * Call this method for building notification message.
     *
     * @param context context used for retrieving string patterns.
     * @return formatted message.
     */
    public String buildNotificationMessage(Context context) {
        String message;
        switch (mActivityType) {
            case VALUE_TYPE_POST_CREATE: {
                Bulletin b = (Bulletin) mModelArray[BULLETIN_POS];
                message = String.format(context.getString(R.string.activity_post_created),
                        b.getUserProfile().getFullName(), b.getSubject());
                break;
            }
            case VALUE_TYPE_REPLY_CREATE: {
                Bulletin b = (Bulletin) mModelArray[BULLETIN_POS];
                Reply r = (Reply) mModelArray[REPLY_POS];
                message = String.format(context.getString(R.string.activity_reply_created),
                        r.getUserProfile().getFullName(),
                        buildNameStringFromModel(b.getUserProfile(), context),
                                b.getSubject());
                break;
            }
            case VALUE_TYPE_RE_REPLY_CREATE: {
                Reply r = (Reply) mModelArray[REPLY_POS];
                Reply re = (Reply) mModelArray[REREPLY_POS];
                message = String.format(context.getString(R.string.activity_reply_created),
                        re.getUserProfile().getFullName(),
                        buildNameStringFromModel(r.getUserProfile(), context),
                        r.getReplyText());
                break;
            }
            case VALUE_TYPE_GROUP_CREATE: {
                Group g = (Group) mModelArray[GROUP_POS];
                message = String.format(context.getString(R.string.activity_group_created),
                        g.getUserProfile().getFullName(), g.getGroupName());
                break;
            }
            case VALUE_TYPE_BOOST: {
                Bulletin b = (Bulletin) mModelArray[BULLETIN_POS];
                UserProfile profile = (UserProfile) mModelArray[USER_GENERATED_POS];
                message = String.format(context.getString(R.string.activity_boost),
                        profile.getFullName(),
                        buildNameStringFromModel(b.getUserProfile(), context),
                        b.getSubject());
                break;
            }
            case VALUE_TYPE_APPROVE: {
                Reply r = (Reply) mModelArray[USER_GENERATED_POS - 1];
                UserProfile profile = (UserProfile) mModelArray[USER_GENERATED_POS];
                message = String.format(context.getString(R.string.activity_approve),
                        profile.getFullName(),
                        buildNameStringFromModel(r.getUserProfile(), context),
                        r.getReplyText());
                break;
            }
            case VALUE_TYPE_FOLLOW: {
                UserProfile profileFollowed = (UserProfile) mModelArray[USER_FOLLOWED_POS];
                UserProfile profile = (UserProfile) mModelArray[USER_GENERATED_POS];
                message = String.format(context.getString(R.string.activity_follow),
                        profile.getFullName(),
                        buildNameStringFromModel(profileFollowed, context));
                break;
            }
            default:
                message = "Error";
        }

        return message;
    }

    /**
     * Method for creating name string based on user profile.
     *
     * @param profile user profile.
     * @return name from profile object or "your".
     */
    private String buildNameStringFromModel(UserProfile profile, Context context) {
        if (isActivity) {
            return profile.getFullName();
        } else {
            return profile.getId() == MotherActivity.user_id ? context.getString(R.string.activity_name_replace_string) : profile.getFullName();
        }
    }

    /**
     * Returns picture url that will be shown in notification picture.
     *
     * @return built url.
     */
    public String buildIconUrl() {
        return mUrl;
    }

    /**
     * Call this when notification is listed in activity log on profile view activity.
     *
     * @param isActivity set to true if this is listed in activity.
     */
    public void setIsActivity(boolean isActivity) {
        this.isActivity = isActivity;
    }
}
