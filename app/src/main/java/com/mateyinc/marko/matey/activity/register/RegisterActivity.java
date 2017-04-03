package com.mateyinc.marko.matey.activity.register;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.internet.SessionManager;
import com.mateyinc.marko.matey.internet.events.UploadEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class RegisterActivity extends MotherActivity implements FragmentListener {
    private static final String TAG = "RegisterActivity";
    // Fragments position
    private static final int FULLNAME_FRAG_POS = 0;
    private static final int EMAIL_FRAG_POS = FULLNAME_FRAG_POS + 1;
    private static final int PASSWORD_FRAG_POS = EMAIL_FRAG_POS + 1;
    private static final int FINAL_FRAG_POS = PASSWORD_FRAG_POS + 1;

    private String mEmail, mPass, mFirstName, mLastName;
    private FragmentManager mFragManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        setChildSupportActionBar();
        init();
    }

    private void init() {
        // Initialising first fragment in reg process
        mFragManager = getSupportFragmentManager();
        mFragManager.beginTransaction().add(R.id.container, FullNameFragment.newInstance(FULLNAME_FRAG_POS)).commit();
    }

    @Override
    public void onNext(Bundle b, int fragPos) {
        RegistrationFragment fragment;
        switch (fragPos) {
            case FULLNAME_FRAG_POS: {
                fragment = EmailFragment.newInstance(EMAIL_FRAG_POS);
                mFirstName = b.getString(RegistrationFragment.EXTRA_NAME_STIRNG);
                mLastName = b.getString(RegistrationFragment.EXTRA_LASTNAME_STIRNG);
                break;
            }
            case EMAIL_FRAG_POS: {
                fragment = PasswordFragment.newInstance(PASSWORD_FRAG_POS);
                mEmail = b.getString(RegistrationFragment.EXTRA_EMAIL_STIRNG);
                break;
            }
            case PASSWORD_FRAG_POS: {
                mPass = b.getString(RegistrationFragment.EXTRA_PASSWORD_STIRNG);
                SessionManager.getInstance(this).registerWithVolley(this, mFirstName, mLastName, mEmail, mPass);
                return;
            }
            case FINAL_FRAG_POS:{
                finish();
            }
            default:
                return;
        }

        mFragManager.beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                .replace(R.id.container, fragment).
                addToBackStack(null).commit();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRegisterListener(UploadEvent event) {
        if (event.isSuccess) {
            mFragManager.beginTransaction()
                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                    .replace(R.id.container, FinalStepFragment.newInstance(FINAL_FRAG_POS))
                    .commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}
