package com.mateyinc.marko.matey.activity.register;


import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.mateyinc.marko.matey.R;

public class PasswordFragment extends RegistrationFragment {

    private static final int MIN_PASS_LENGTH = 6;

    public PasswordFragment() {
    }

    public static RegistrationFragment newInstance(int fragPosition) {
        PasswordFragment fragment = new PasswordFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_FRAG_POS, fragPosition);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        tvHeaderText.setText(getString(R.string.register_password_header));
        llInputFields.removeView(etSecondInput);
        etFirstInput.setHint(getString(R.string.register_password));
        etFirstInput.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        return rootView;
    }

    @Override
    protected void onNextClicked() {
        Bundle b = new Bundle();
        String pass = etFirstInput.getText().toString();
        if (isValidPass(pass)) {
            b.putString(EXTRA_PASSWORD_STIRNG, pass);
        } else {
            etFirstInput.setError(getString(R.string.register_error_pass));
            return;
        }

        // Hide keyboard first
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etFirstInput.getWindowToken(), 0);
        // Continue
        mListener.onNext(b, mFragPos);
    }

    private boolean isValidPass(String pass) {
        return pass != null && pass.length() > 0 && pass.length() > MIN_PASS_LENGTH;
    }

    @Override
    public Bundle onBackButton() {
        return null;
    }
}
