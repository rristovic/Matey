package com.mateyinc.marko.matey.model;

public class UserProfile {
    public static final String USER_ID = "userid";
    public static final String FIRST_NAME = "firstname";
    public static final String LAST_NAME = "lastname";
    public static final String LAST_MSG_ID = "lastmsgid";
    public static final String EMAIL = "email";

    private long userId;

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
        this.userId = userId;
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
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
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
        this.userId = currentUserProfile.userId;
    }
}
