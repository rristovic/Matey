package com.mateyinc.marko.matey.activity.home;

import android.support.v4.app.Fragment;

/**
 * Created by Sarma on 8/27/2016.
 */
public class MessagesFragment extends Fragment {

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * NotificationsFragment callback for when an item has been selected.
         */
        public void onItemSelected();
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MessagesFragment() {
    }
}
