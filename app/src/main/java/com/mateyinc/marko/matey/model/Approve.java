package com.mateyinc.marko.matey.model;

public class Approve extends MModel{

    public Approve(long userId, long postId, long replyId){
        this.userId = userId;
        this.postId = postId;
        this.replyId = replyId;
    }
    public long userId;
    public long postId;
    public long replyId;
    public long _id;
}
