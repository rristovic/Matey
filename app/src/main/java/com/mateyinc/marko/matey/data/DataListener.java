package com.mateyinc.marko.matey.data;

/**
 * Created by Sarma on 11/30/2016.
 */

public  abstract class DataListener {
    interface DataLoadedListener{
        void loadSuccess();
        void loadFailed();
    }
}
