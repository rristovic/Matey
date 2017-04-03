package com.mateyinc.marko.matey.activity.register;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mateyinc.marko.matey.R;
import com.rengwuxian.materialedittext.MaterialEditText;


public abstract class RegistrationFragment extends Fragment {

    // Extra string for bundle that will be send to register activity
    static final String EXTRA_EMAIL_STIRNG = "extra_email";
    static final String EXTRA_PASSWORD_STIRNG = "extra_password";
    static final String EXTRA_NAME_STIRNG = "extra_name";
    static final String EXTRA_LASTNAME_STIRNG = "extra_last_name";

    protected static final String ARG_FRAG_POS = "fragment_position";

    protected MaterialEditText etFirstInput, etSecondInput;
    protected LinearLayout llInputFields;
    protected TextView tvHeaderText;
    protected Button btnNext;

    protected FragmentListener mListener;
    protected int mFragPos;

    public RegistrationFragment(){
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mFragPos = getArguments().getInt(ARG_FRAG_POS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_register, container, false);

        tvHeaderText = (TextView) rootView.findViewById(R.id.tvHeaderText);
        etFirstInput = (MaterialEditText) rootView.findViewById(R.id.etFirstInputField);
        etSecondInput = (MaterialEditText) rootView.findViewById(R.id.etSecondInputField);
        llInputFields = (LinearLayout) rootView.findViewById(R.id.llInputFields);
        btnNext = (Button) rootView.findViewById(R.id.btnNextStep);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNextClicked();
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentListener) {
            mListener = (FragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement FragmentListener");
        }
    }



    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    protected abstract void onNextClicked();
    public abstract Bundle onBackButton();

}
