package com.mateyinc.marko.matey.activity.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.inall.MotherActivity;

@SuppressLint("NewApi")
public class MainActivity extends MotherActivity {

	private static final int SHORT_ANIM_TIME = 150;
	private static final int MED_ANIM_TIME = 500;
	private static final int INTERMED_ANIM_TIME = 700;
	private static final int LONG_ANIM_TIME = 1000;
	private static final long SERVER_TIMER = 5000;

	private ImageView ivLogo, ivLogoText, ivLoadingHead;
	private Button btnLogin, btnReg, btnFb;
	private RelativeLayout rlLogo, rlLoginButtons, rlLoadingHead;
	private LinearLayout llEmail, llPass, line1, line2;

	private float mLoginBtnMoveY;
	private float mRegBtnMoveY;
	private int mLoginBtnBotMargin;
	private boolean mLoginFormVisible;
	private boolean mServerReady = false;
	private boolean mRegFormVisible;
	private int mRegBtnBotMargin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		if (getSupportActionBar() != null)
			getSupportActionBar().hide();
		setContentView(R.layout.activity_main);

		init();

		//super.startTommy();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if(tommy.getStatus() != AsyncTask.Status.FINISHED) {
			tommy.cancel(true);
		}

	}

	private void init() {
		ivLogo = (ImageView) findViewById(R.id.ivLoginLogo);
		btnLogin = (ButtonLoginPage) findViewById(R.id.btnLogin);
		btnReg = (ButtonLoginPage) findViewById(R.id.btnReg);
		btnFb = (Button) findViewById(R.id.btnFacebook);
		ivLogoText = (ImageView) findViewById(R.id.ivLoginLogoText);
		llEmail = (LinearLayout) findViewById(R.id.llEmail);
		llPass = (LinearLayout) findViewById(R.id.llPass);
		line1 = (LinearLayout) findViewById(R.id.llLine1);
		line2 = (LinearLayout) findViewById(R.id.llLine2);
		rlLoginButtons = (RelativeLayout) findViewById(R.id.rlLoginButtons);
		ivLoadingHead = (ImageView) findViewById(R.id.ivLoadingHead);
		rlLoadingHead = (RelativeLayout) findViewById(R.id.rlLoadingHead);
		rlLogo = (RelativeLayout) findViewById(R.id.rlLogo);

		startIntro();
		setOnClickListeners();

	}

	private void setOnClickListeners() {
		btnLogin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!mLoginFormVisible)
					showLoginForm();
			}
		});

		btnReg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!mRegFormVisible)
					showRegForm();
			}
		});
	}

	private void showLoginForm() {
		startFadeAnimation(btnReg, 1, 0, SHORT_ANIM_TIME, false);
		startFadeAnimation(btnFb, 1, 0, SHORT_ANIM_TIME, false);
		startFadeAnimation(ivLogoText, 1, 0, SHORT_ANIM_TIME, false);

		showLoginFormAnim();
	}

	private void showRegForm() {
		startFadeAnimation(btnLogin, 1, 0, SHORT_ANIM_TIME);
		startFadeAnimation(btnFb, 1, 0, SHORT_ANIM_TIME);
		startFadeAnimation(ivLogoText, 1, 0, SHORT_ANIM_TIME);

		showRegFormAnim();
	}

	// Animations
	//////////////////////////////////////////////////////////////////////////
	private void startIntro() {

		// Get view's h and w after its layout process then start anim
		ViewTreeObserver viewTreeObserver = rlLoadingHead.getViewTreeObserver();
		if (viewTreeObserver.isAlive()) {
			viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					rlLoadingHead.getViewTreeObserver().removeGlobalOnLayoutListener(this);
					startIntroAnim(ivLoadingHead, 2, 1, 2, 1, 500);
				}
			});
		}


	}

	// Buttons anim
	private void showLoginFormAnim() {
		TranslateAnimation animation = new TranslateAnimation(0, 0, 0, mLoginBtnMoveY = calculateLoginBtnTransl());
		animation.setDuration(500);
		animation.setFillAfter(false);
		animation.setAnimationListener(new LoginBtnDownAnimListener());
		mLoginBtnBotMargin = ((RelativeLayout.LayoutParams) btnLogin.getLayoutParams()).bottomMargin;
		btnLogin.startAnimation(animation);

		// For anim control, only animate once on button click if form is visible
		mLoginFormVisible = true;
	}

	private void startLoginReverseAnim() {
		startFadeAnimation(llPass, 1, 0, SHORT_ANIM_TIME);
		startFadeAnimation(llEmail, 1, 0, SHORT_ANIM_TIME);
		startFadeAnimation(line2, 1, 0, SHORT_ANIM_TIME);
		startFadeAnimation(line1, 1, 0, SHORT_ANIM_TIME);

		// Return buttons to theirs original states
		TranslateAnimation animation = new TranslateAnimation(0, 0, 0, -mLoginBtnMoveY);
		animation.setDuration(500);
		animation.setFillAfter(false);
		animation.setAnimationListener(new LoginBtnUpAnimListener());

		btnLogin.startAnimation(animation);
	}

	private void showRegFormAnim() {
		TranslateAnimation animation = new TranslateAnimation(0, 0, 0, mRegBtnMoveY = calculateRegBtnTransl());
		animation.setDuration(500);
		animation.setFillAfter(false);
		animation.setAnimationListener(new RegBtnDownListener());
		btnReg.startAnimation(animation);

		// For anim control, only animate once on button click if form is visible
		mRegFormVisible = true;
	}

	private void startRegReverseAnim() {
		startFadeAnimation(llPass, 1, 0, SHORT_ANIM_TIME);
		startFadeAnimation(llEmail, 1, 0, SHORT_ANIM_TIME);
		startFadeAnimation(line2, 1, 0, SHORT_ANIM_TIME);
		startFadeAnimation(line1, 1, 0, SHORT_ANIM_TIME);

		// Return buttons to theirs original states
		TranslateAnimation animation = new TranslateAnimation(0, 0, 0, -mRegBtnMoveY);
		animation.setDuration(500);
		animation.setFillAfter(false);
		animation.setAnimationListener(new RegBtnUpListener());

		btnReg.startAnimation(animation);
	}

	private float calculateLoginBtnTransl() {
		return btnFb.getTop() - btnLogin.getTop();
	}

	private float calculateRegBtnTransl() {
		return btnFb.getTop() - btnReg.getTop();
	}

	// Fade anim
	private void startFadeAnimation(final View view, final int from, final int to, int time) {

		view.setVisibility(View.VISIBLE);
		AlphaAnimation fade = new AlphaAnimation(from, to);
		fade.setInterpolator(new LinearInterpolator());
		fade.setDuration(time);
		fade.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				if (from > to)
					view.setVisibility(View.GONE);
				else
					view.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});

		view.startAnimation(fade);
	}

	private void startFadeAnimation(final View view, final int from, final int to, int time, final boolean hide) {

		view.setVisibility(View.VISIBLE);
		AlphaAnimation fade = new AlphaAnimation(from, to);
		fade.setInterpolator(new LinearInterpolator());
		fade.setDuration(time);
		fade.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				if (hide)
					view.setVisibility(View.GONE);
				else if (from > to)
					view.setVisibility(View.INVISIBLE);
				else
					view.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});

		view.startAnimation(fade);
	}

	// Loading anim
	private void startIntroAnim(final View view, int fromX, int toX, int fromY, int toY, int time) {
		view.setVisibility(View.VISIBLE);
		AnimatorSet set = new AnimatorSet();
		set
				.play(ObjectAnimator.ofFloat(view, View.ALPHA, 0, 1f))
				.with(ObjectAnimator.ofFloat(view, View.SCALE_X, 2f, 1f))
				.with(ObjectAnimator.ofFloat(view, View.SCALE_Y, 2f, 1f));
		set.setInterpolator(new AccelerateInterpolator(2.5f));
		set.setDuration(time);
		set.start();

		CountDownTimer timer = new CountDownTimer(time * 2, time) {
			@Override
			public void onTick(long millisUntilFinished) {
			}

			@Override
			public void onFinish() {
				goUpAnim(500);
			}
		};
		timer.start();

		// TODO - add receiver to stop loading anim
		// Simulate server 10s
		CountDownTimer timer1 = new CountDownTimer(SERVER_TIMER, SERVER_TIMER) {
			@Override
			public void onTick(long millisUntilFinished) {
			}

			@Override
			public void onFinish() {
				mServerReady = true;
			}
		};
		timer1.start();

	}

	private void goUpAnim(final int time) {
		ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(ivLoadingHead, View.Y, -30f);
		objectAnimator.setDuration(time);
		objectAnimator.setStartDelay(100);
		objectAnimator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				ivLoadingHead.clearAnimation();
				if (!mServerReady) {
					goDownAnim(700);
				} else
					endLoadingAnim();
			}
		});
		objectAnimator.start();
	}

	private void goDownAnim(final int time) {
		ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(ivLoadingHead, View.Y, 30f);
		objectAnimator.setInterpolator(new BounceInterpolator());
		objectAnimator.setDuration(time);
		objectAnimator.setStartDelay(200);
		objectAnimator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				ivLoadingHead.clearAnimation();
				if (!mServerReady) {
					goUpAnim(time);
				} else
					endLoadingAnim();
			}
		});
		objectAnimator.start();
	}

	private void endLoadingAnim() {
		// Return pic to center and  move layout
		// TODO - rework other anims via Animator because it does not interfere with system transitions
		AnimatorSet set = new AnimatorSet();
		set
				.play(ObjectAnimator.ofFloat(rlLoadingHead, View.Y, 0))
				.with(ObjectAnimator.ofFloat(ivLoadingHead, View.Y, 0));
		set.setInterpolator(new OvershootInterpolator(2f));
		set.setDuration(1000);
		set.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				// Prevent anim lagging
				rlLogo.requestLayout();

				startFadeAnimation(rlLogo, 0, 1, INTERMED_ANIM_TIME);
				startFadeAnimation(ivLogoText, 0, 1, INTERMED_ANIM_TIME);
				CountDownTimer timer = new CountDownTimer(600, 600) {
					@Override
					public void onTick(long millisUntilFinished) {

					}

					@Override
					public void onFinish() {
						startFadeAnimation(rlLoginButtons, 0, 1, SHORT_ANIM_TIME);
					}
				}.start();
			}
		});
		set.start();

	}
	//////////////////////////////////////////////////////////////////////////


	// Animation listeners
	//////////////////////////////////////////////////////////////////////////
	private class LoginBtnDownAnimListener implements Animation.AnimationListener {

		@Override
		public void onAnimationEnd(Animation animation) {
			btnLogin.clearAnimation();

			// Set button position after anim
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(btnLogin.getWidth(), btnLogin.getHeight());
			layoutParams.setMargins(0, 0, 0, rlLoginButtons.getHeight() - btnFb.getBottom());
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

			btnLogin.setLayoutParams(layoutParams);

			startFadeAnimation(llPass, 0, 1, SHORT_ANIM_TIME);
			startFadeAnimation(llEmail, 0, 1, SHORT_ANIM_TIME);
			startFadeAnimation(line1, 0, 1, SHORT_ANIM_TIME);
			startFadeAnimation(line2, 0, 1, SHORT_ANIM_TIME);
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationStart(Animation animation) {

		}

	}

	private class LoginBtnUpAnimListener implements Animation.AnimationListener {

		@Override
		public void onAnimationEnd(Animation animation) {
			btnLogin.clearAnimation();
			btnFb.setVisibility(View.VISIBLE);
			btnReg.setVisibility(View.VISIBLE);

			// Set button position back to original layout position
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(btnLogin.getWidth(), btnLogin.getHeight());
			layoutParams.setMargins(0, 0, 0, mLoginBtnBotMargin);
			layoutParams.addRule(RelativeLayout.ALIGN_START, R.id.btnReg);
			layoutParams.addRule(RelativeLayout.ALIGN_LEFT, R.id.btnReg);
			layoutParams.addRule(RelativeLayout.ABOVE, R.id.btnReg);
			layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

			btnLogin.setLayoutParams(layoutParams);

			startFadeAnimation(btnFb, 0, 1, SHORT_ANIM_TIME);
			startFadeAnimation(btnReg, 0, 1, SHORT_ANIM_TIME);
			startFadeAnimation(ivLogoText, 0, 1, SHORT_ANIM_TIME);
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationStart(Animation animation) {
		}

	}


	private class RegBtnDownListener implements Animation.AnimationListener {

		@Override
		public void onAnimationEnd(Animation animation) {
			btnReg.clearAnimation();

			// Set button position after anim
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(btnReg.getWidth(), btnReg.getHeight());
			layoutParams.setMargins(0, 0, 0, rlLoginButtons.getHeight() - btnFb.getBottom());
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

			btnReg.setLayoutParams(layoutParams);

			startFadeAnimation(llPass, 0, 1, SHORT_ANIM_TIME);
			startFadeAnimation(llEmail, 0, 1, SHORT_ANIM_TIME);
			startFadeAnimation(line1, 0, 1, SHORT_ANIM_TIME);
			startFadeAnimation(line2, 0, 1, SHORT_ANIM_TIME);
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationStart(Animation animation) {

		}

	}

	private class RegBtnUpListener implements Animation.AnimationListener {

		@Override
		public void onAnimationEnd(Animation animation) {
			btnReg.clearAnimation();
			btnFb.setVisibility(View.VISIBLE);
			btnLogin.setVisibility(View.VISIBLE);

			// Set button position back to original layout position
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(btnLogin.getWidth(), btnLogin.getHeight());
			layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
			btnReg.setLayoutParams(layoutParams);

			startFadeAnimation(btnFb, 0, 1, SHORT_ANIM_TIME);
			startFadeAnimation(btnLogin, 0, 1, SHORT_ANIM_TIME);
			startFadeAnimation(ivLogoText, 0, 1, SHORT_ANIM_TIME);
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationStart(Animation animation) {
		}

	}
	//////////////////////////////////////////////////////////////////////////


	@Override
	protected void onPause() {
		super.onPause();
	}

	// Controlling physical back button
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (llEmail.getVisibility() == View.VISIBLE && btnLogin.getVisibility() == View.VISIBLE) {
				startLoginReverseAnim();
				mLoginFormVisible = false;
				return true;
			} else if (llEmail.getVisibility() == View.VISIBLE && btnReg.getVisibility() == View.VISIBLE) {
				startRegReverseAnim();
				mRegFormVisible = false;
				return true;
			} else
				return super.onKeyDown(keyCode, event);

		}
		return super.onKeyDown(keyCode, event);
	}

}
