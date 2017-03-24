package com.mateyinc.marko.matey.internet.events;

import java.util.List;



public class SearchHintEvent {
    public final List<String> mData;

    public SearchHintEvent(List<String> data){
        this.mData = data;
    }
}
