package com.mateyinc.marko.matey.activity.register;


import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
        etFirstInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS|InputType.TYPE_CLASS_TEXT);
        // Handle focus navigation
        handleFocusFirstField();
        return rootView;
    }

    @Override
    protected void onNextClicked() {
        Bundle b = new Bundle();
        String text = etFirstInput.getText().toString().trim();
        if (isFirstInputValid(text)) {
            b.putString(EXTRA_EMAIL_STIRNG, text);
        } else {
            etFirstInput.setError(getString(R.string.register_error_email));
            return;
        }

       // Continue
        mListener.onNext(b, mFragPos);
    }

    @Override
    /**
     * Method for validating email address.
     */
    protected boolean isFirstInputValid(String input) {
        if (TextUtils.isEmpty(input)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches();
        }    }


    @Override
    public Bundle onBackButton() {
        return null;
    }
}
