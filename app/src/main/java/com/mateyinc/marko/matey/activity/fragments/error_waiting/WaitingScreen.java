package com.mateyinc.marko.matey.activity.fragments.error_waiting;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mateyinc.marko.matey.R;

/**
 * Created by M4rk0 on 4/27/2016.
 */
public class WaitingScreen extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.waiting_screen, container, false);
    }
}
