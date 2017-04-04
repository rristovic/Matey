package com.mateyinc.marko.matey.activity.register;


import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.mateyinc.marko.matey.R;

public class FinalStepFragment extends RegistrationFragment {

    public FinalStepFragment() {
    }

    public static RegistrationFragment newInstance(int fragPosition) {
        FinalStepFragment fragment = new FinalStepFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_FRAG_POS, fragPosition);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        tvHeaderText.setText(getString(R.string.register_finalStep_text));
        btnNext.setText(getString(R.string.register_doneBtn_label));
        llInputFields.removeView(etSecondInput);
        llInputFields.removeView(etFirstInput);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) btnNext.getLayoutParams();
        params.topMargin += TypedValue.applyDimension(100, TypedValue.COMPLEX_UNIT_DIP, getResources().getDisplayMetrics());
        btnNext.setLayoutParams(params);
        return rootView;
    }

    @Override
    protected void onNextClicked() {

        mListener.onNext(null, mFragPos);
    }

    @Override
    public Bundle onBackButton() {
        return null;
    }
}
