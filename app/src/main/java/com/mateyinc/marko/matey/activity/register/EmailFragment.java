package com.mateyinc.marko.matey.activity.register;


import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.mateyinc.marko.matey.R;

public class EmailFragment extends RegistrationFragment {

    public EmailFragment() {
    }

    public static RegistrationFragment newInstance(int fragPosition) {
        EmailFragment fragment = new EmailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_FRAG_POS, fragPosition);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        tvHeaderText.setText(getString(R.string.register_email_header));
        llInputFields.removeView(etSecondInput);
        etFirstInput.setHint(getString(R.string.register_email));
        etFirstInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        return rootView;
    }

    @Override
    protected void onNextClicked() {
        Bundle b = new Bundle();
        String text = etFirstInput.getText().toString();
        if (isValidEmail(text)) {
            b.putString(EXTRA_EMAIL_STIRNG, text);
        } else {
            etFirstInput.setError(getString(R.string.register_error_email));
            return;
        }

       // Continue
        mListener.onNext(b, mFragPos);
    }

    /**
     * Method for validating email address.
     */
    private boolean isValidEmail(CharSequence target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    @Override
    public Bundle onBackButton() {
        return null;
    }
}
