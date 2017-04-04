package com.mateyinc.marko.matey.activity.register;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mateyinc.marko.matey.R;


public class FullNameFragment extends RegistrationFragment{

    private static final int MIN_NAME_LENGTH = 2;

    public FullNameFragment() {
    }

    public static RegistrationFragment newInstance(int fragPosition) {
        FullNameFragment fragment = new FullNameFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_FRAG_POS, fragPosition);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        tvHeaderText.setText(getString(R.string.register_name_header));
        etFirstInput.setInputType(InputType.TYPE_CLASS_TEXT);
        etSecondInput.setInputType(InputType.TYPE_CLASS_TEXT);
        // Handle focus navigation
        handleFocusSecondField();

        return rootView;
    }



    @Override
    protected void onNextClicked() {
        Bundle b = new Bundle();
        String name = etFirstInput.getText().toString().trim();
        if (isFirstInputValid(name)) {
            b.putString(EXTRA_NAME_STIRNG, name);
        } else {
            etFirstInput.setError(getString(R.string.register_error_name));
            return;
        }
        String lastName = etSecondInput.getText().toString().trim();
        if (isFirstInputValid(lastName)) {
            b.putString(EXTRA_LASTNAME_STIRNG, lastName);
        } else {
            etSecondInput.setError(getString(R.string.register_error_name));
            return;
        }

        mListener.onNext(b, mFragPos);
    }

    @Override
    /**
     * Method for validating name.
     */
    protected boolean isFirstInputValid(String input) {
        return input != null && input.length() > 0 && input.length() > MIN_NAME_LENGTH;
    }

    @Override
    public Bundle onBackButton() {
        return null;
    }



}
