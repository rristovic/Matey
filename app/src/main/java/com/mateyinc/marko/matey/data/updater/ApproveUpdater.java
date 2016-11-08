package com.mateyinc.marko.matey.data.updater;


import android.content.Context;

import java.util.Date;

public class ApproveUpdater  extends DataUpdater {

    public ApproveUpdater(int level){
        this.level = level;
    }

    @Override
    void updateServerStatus(int serverStatus, long id, Context context) {

    }

    @Override
    void updateIdAndDate(long oldId, long newId, Date date, Context context) {

    }
}
