package com.mateyinc.marko.matey.internet.events;

import com.mateyinc.marko.matey.internet.operations.OperationType;


public abstract class Event {
    public boolean isSuccess;
    public OperationType mEventType;

    public Event(boolean isSuccess, OperationType eventType){
        this.isSuccess = isSuccess;
        this.mEventType = eventType;
    }
}
