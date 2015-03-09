package co.mindquake.nester;
//http://developer.android.com/training/articles/memory.html
import co.mindquake.nester.startup.ServerRequest;
import io.userhabit.service.Userhabit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import co.mindquake.nester.NesterApp.TrackerName;
import co.mindquake.nester.alarmmode.AlarmListActivity;
import co.mindquake.nester.appselection.OWLAppSelectionFragment;
import co.mindquake.nester.item.OWLItemFragment;
import co.mindquake.nester.market.OWLMarketFragment;
import co.mindquake.nester.preference.OWLPreference;
import co.mindquake.nester.startup.LauncherGuideActivity;
import co.mindquake.nester.startup.LoginActivity;
import co.mindquake.nester.startup.ModeSelection;
import co.mindquake.nester.timer.OWLTimerFragment;

import com.flurry.android.FlurryAgent;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.viewpagerindicator.ColorPagerAdapter;
import com.viewpagerindicator.TabPageIndicator;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

public class MainActivity extends FragmentActivity {

	private SectionsPagerAdapter mSectionsPagerAdapter;
	private TabPageIndicator mTabPageIndicator;
	private ViewPager mViewPager;
	private OWLPreference preference;
	Typeface typeface;
	ImageView imageView;
	int mCurrentPosition = 0;
	Dialog mDialog;
	Button mBtnMain;
	Runnable touchRunnable;
	Tracker mTracker;
	
	String mLastScreen = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);

		preference = OWLPreference.getInstance( getApplicationContext() );
		
		if(preference.getDontShowAgainNotice() == false) {
			final Dialog dialog = new Dialog(MainActivity.this);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
			dialog.setContentView(R.layout.dialog_notice);
			dialog.show();
			
			Button without = (Button)dialog.findViewById(R.id.without_login);
			without.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					preference.setDontShowAgainNotice(true);
					dialog.dismiss();
				}
			});
		}
		
		ActionBar actionBar = getActionBar();
	    actionBar.setHomeButtonEnabled(true);

		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());
		
		if(savedInstanceState != null) {
			FragmentTransaction ft  = getSupportFragmentManager().beginTransaction();
			//ft.replace(arg0, arg1)
		}

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.mainPager);
		mViewPager.setOffscreenPageLimit(6);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		mTabPageIndicator = (TabPageIndicator) findViewById( R.id.pagerMainIndicator );
		mTabPageIndicator.setViewPager( mViewPager );

		if(!NesterUtils.isDebuggable(getApplicationContext())) {
			mLastScreen = "Handpick";
			mTracker = ((NesterApp)getApplication()).getTracker(TrackerName.APP_TRACKER);
			mTracker.setScreenName(mLastScreen);
			FlurryAgent.logEvent(mLastScreen, true);
			//Userhabit.openSubview(mLastScreen);
		}




        Intent intent = getIntent();
        String extra = intent.getStringExtra("notification");


        if(extra != null && extra.equals("true") && (intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0)
        {

            String uuid = intent.getStringExtra("uuid");
            String title = intent.getStringExtra("title");
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("uuid", uuid));

            ServerRequest sr = new ServerRequest();
            JSONObject json = sr.getJSON(((NesterApp)getApplication()).getServerAddress() + "/api/push/addPushEnterCount",params);


            if(!NesterUtils.isDebuggable(getApplicationContext())) {
                Tracker t = ((NesterApp)getApplication()).getTracker(TrackerName.APP_TRACKER);
                t.send(new HitBuilders.EventBuilder()
                        .setCategory("Notification")
                        .setAction("touch")
                        .setLabel(title)
                        .build());
            }
        }else{
            mTabPageIndicator.setCurrentItem(1);
            mCurrentPosition = 1;
        }







		final View.OnClickListener listener = new OnClickListener() {
			ImageButton btn;
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mCurrentPosition < 3) {
					mTabPageIndicator.setCurrentItem(mCurrentPosition + 1);
				}
				else if(mCurrentPosition == 3) {
					//mTabPageIndicator.setCurrentItem(2);
					drawTouchImage();
					if(mDialog == null) {
						mDialog = new Dialog(MainActivity.this);
						mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
						mDialog.setCancelable(true);
						mDialog.setCanceledOnTouchOutside(true);
					}
					if(mDialog.isShowing()) {
						mDialog.dismiss();
					}
					else {
						mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
						mDialog.setContentView(R.layout.dialog_current_info);
						mDialog.show();

						//mDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
			            //        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
						mDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	
						WindowManager.LayoutParams params = mDialog.getWindow().getAttributes();
						params.y = Math.round(103 * getApplication().getResources().getDisplayMetrics().density);
						params.x = 0;
						params.gravity = Gravity.CENTER_HORIZONTAL|Gravity.TOP;       
						mDialog.getWindow().setAttributes(params); 

						TextView apps = (TextView)mDialog.findViewById(R.id.selected_num);
						apps.setText(Html.fromHtml(String.format(getResources().getString(R.string.selected_apps_info), preference.getSelectedApps().size())));
						TextView time = (TextView)mDialog.findViewById(R.id.selected_time);
						time.setText(Html.fromHtml(String.format(getResources().getString(R.string.set_timer_info), preference.getTime())));
						ImageView arrow = (ImageView)mDialog.findViewById(R.id.arrow);
						float density = getResources().getDisplayMetrics().density;
						TranslateAnimation arrowani = new TranslateAnimation(0.0f, 0.0f, 0.0f, -8.0f * density);
						arrowani.setRepeatMode(Animation.REVERSE);
						arrowani.setRepeatCount(Animation.INFINITE);
						arrowani.setDuration(500);
						arrow.startAnimation(arrowani);
						btn = (ImageButton)mDialog.findViewById(R.id.btn_childmode);
						btn.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								runKidsLauncher();
							}
						});

						mDialog.setOnDismissListener(new OnDismissListener() {
							
							@Override
							public void onDismiss(DialogInterface dialog) {
								// TODO Auto-generated method stub
								//btn.removeCallbacks(runnable);
								mBtnMain.setText(R.string.next_button_label);
								mDialog = null;
							}
						});

						StateListDrawable states;
				        states = new StateListDrawable();
				        states.addState(new int[] {android.R.attr.state_pressed},
				            getApplicationContext().getResources().getDrawable(R.drawable.selected_child));
				        states.addState(new int[] {android.R.attr.state_focused},
				        	getApplicationContext().getResources().getDrawable(R.drawable.unselected_child));
				        states.addState(new int[] { },
				        	getApplicationContext().getResources().getDrawable(R.drawable.unselected_child));
				        btn.setImageDrawable(states);

						mBtnMain.setText(R.string.cancel_button);
					}
				}
				else {

				}
			}
		};

		mTabPageIndicator.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int newPosition) {			
				FragmentLifecycle fragmentToHide = (FragmentLifecycle)mSectionsPagerAdapter.getItem(mCurrentPosition);
				fragmentToHide.onPauseFragment();

				FragmentLifecycle fragmentToShow = (FragmentLifecycle)mSectionsPagerAdapter.getItem(newPosition);
				fragmentToShow.onResumeFragment();
				
				mCurrentPosition = newPosition;

				RelativeLayout relative = (RelativeLayout)findViewById(R.id.layoutBtn);
				RelativeLayout gradient = (RelativeLayout)findViewById(R.id.gradient);

				if(mDialog != null && mDialog.isShowing()) {
					mDialog.hide();
					mBtnMain.setText(R.string.next_button_label);
				}
				
				if(!NesterUtils.isDebuggable(getApplicationContext())) {
					if(mLastScreen != null) {
						FlurryAgent.endTimedEvent(mLastScreen);
						Userhabit.closeSubview();
						mLastScreen = null;
					}
				}
				
				switch(mCurrentPosition) {
				case 0:
					mLastScreen = "Featured";
					relative.setVisibility(View.GONE);
					gradient.setVisibility(View.GONE);
					break;
				case 1:
					relative.setVisibility(View.VISIBLE);
					gradient.setVisibility(View.VISIBLE);
					mBtnMain = (Button)findViewById(R.id.mainButton);
					mBtnMain.setBackgroundDrawable(getResources().getDrawable(R.drawable.selector_main_handpick));
					mBtnMain.setText(R.string.next_button_label);
					mBtnMain.setOnClickListener(listener);
					mLastScreen = "Handpick";
					break;
				case 2:
					relative.setVisibility(View.VISIBLE);
					gradient.setVisibility(View.GONE);
					mBtnMain = (Button)findViewById(R.id.mainButton);
					mBtnMain.setBackgroundDrawable(getResources().getDrawable(R.drawable.selector_main_timer));
					mBtnMain.setText(R.string.next_button_label);
					mBtnMain.setOnClickListener(listener);
					mLastScreen = "Timer";
					break;
				case 3:
					mLastScreen = "Item";
					relative.setVisibility(View.VISIBLE);
					gradient.setVisibility(View.GONE);
					mBtnMain = (Button)findViewById(R.id.mainButton);
					mBtnMain.setBackgroundDrawable(getResources().getDrawable(R.drawable.selector_main_item));
					if(mDialog != null && mDialog.isShowing()) {
						mDialog.show();
						mBtnMain.setText(R.string.cancel_button);
						TextView apps = (TextView)mDialog.findViewById(R.id.selected_num);
						apps.setText(Html.fromHtml(String.format(getResources().getString(R.string.selected_apps_info), preference.getSelectedApps().size())));
					}
					break;
				case 4:
					mLastScreen = "Setting";
				default:
					relative.setVisibility(View.GONE);
					gradient.setVisibility(View.GONE);
					break;
				}
				if(!NesterUtils.isDebuggable(getApplicationContext())) {
					mTracker.setScreenName(mLastScreen);
					mTracker.send(new HitBuilders.AppViewBuilder().build());
					FlurryAgent.logEvent(mLastScreen, true);
					Userhabit.openSubview(mLastScreen);
				}
				//mTracker.setScreenName(null);
			}

			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onPageScrollStateChanged(int state) {
				// TODO Auto-generated method stub
			}
		});

		typeface = Typeface.createFromAsset(getAssets(), "fonts/MyriadPro-Semibold.otf");
		setGlobalFont(mTabPageIndicator);

		mBtnMain = (Button)findViewById(R.id.mainButton);
		mBtnMain.setTypeface(typeface);

		mBtnMain.setOnClickListener(listener);
	}
	
	void drawTouchImage() {
	    ActionBar actionBar = getActionBar();
	    if(actionBar.getCustomView() == null) {
		    actionBar.setDisplayOptions(actionBar.getDisplayOptions()
		            | ActionBar.DISPLAY_SHOW_CUSTOM);
		    imageView = new ImageView(actionBar.getThemedContext());
		    imageView.setScaleType(ImageView.ScaleType.CENTER);
		    imageView.setImageResource(R.drawable.actionbar_animation);

			AnimationDrawable frameAnimation = (AnimationDrawable)imageView.getDrawable();
		    frameAnimation.start();
		    
		    ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(
		            ActionBar.LayoutParams.WRAP_CONTENT,
		            ActionBar.LayoutParams.WRAP_CONTENT, Gravity.RIGHT
		                    | Gravity.CENTER_VERTICAL);
		    layoutParams.rightMargin = Math.round(10 / getResources().getDisplayMetrics().density);
		    imageView.setLayoutParams(layoutParams);
		    actionBar.setCustomView(imageView);
	    }
	}

	void setGlobalFont(ViewGroup root) {
        for (int i = 0; i < root.getChildCount(); i++) {
            View child = root.getChildAt(i);
            if (child instanceof TextView)
                ((TextView)child).setTypeface(typeface);
            if (child instanceof Button)
            	((Button)child).setTypeface(typeface);
            else if (child instanceof ViewGroup)
                setGlobalFont((ViewGroup)child);
        }
    }

	@Override
	protected void onStart() {
		super.onStart();
		if(!NesterUtils.isDebuggable(getApplicationContext())) {
			//GoogleAnalytics.getInstance(this).reportActivityStart(this);
			FlurryAgent.onStartSession(this, "RQFC8PFP7RVPZFFMZ8KG");
			Userhabit.activityStart(this);
		}
	}

	@Override
	protected void onStop() {
		if(!NesterUtils.isDebuggable(getApplicationContext())) {
			//GoogleAnalytics.getInstance(this).reportActivityStop(this);
			FlurryAgent.onEndSession(this);
			Userhabit.activityStop(this);
		}
		super.onStop();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		preference.savePackages();
	}

	@Override
	protected void onResume() {
		//preference.setParentMode( true );
		//preference.setScreenLocked(false);

		super.onResume();
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		if(!NesterUtils.isDebuggable(getApplicationContext())) {
			if(mLastScreen != null) {
				FlurryAgent.endTimedEvent(mLastScreen);
				Userhabit.closeSubview();
				mLastScreen = null;
			}
		}

		mDialog = null;
		if(touchRunnable != null && imageView != null) {
			imageView.removeCallbacks(touchRunnable);
			imageView = null;
		}
		mTracker = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        final MenuItem item1= menu.findItem(R.id.action_kids_launcher);
        MenuItemCompat.setActionView(item1, R.layout.custom_action_view);
        View vItem1= MenuItemCompat.getActionView(item1);

        final ImageView customActionItem= (ImageView) vItem1.findViewById(R.id.customActionItem);
        customActionItem.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
            	runKidsLauncher();
            }
        });

		StateListDrawable states;
        states = new StateListDrawable();
        states.addState(new int[] {android.R.attr.state_pressed},
            getApplicationContext().getResources().getDrawable(R.drawable.childmode_selected_botton));
        states.addState(new int[] {android.R.attr.state_focused},
        	getApplicationContext().getResources().getDrawable(R.drawable.childmode_unselected_botton));
        states.addState(new int[] { },
        	getApplicationContext().getResources().getDrawable(R.drawable.childmode_unselected_botton));
        customActionItem.setImageDrawable(states);

        return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		if( item.getItemId() == R.id.action_kids_launcher ){
			runKidsLauncher();
		}
		else if(item.getItemId() == android.R.id.home) {
			Intent intent = new Intent(MainActivity.this, ModeSelection.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
		return false;
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		int position = mViewPager.getCurrentItem();
		if( position != 0 ){
			super.onBackPressed();
			return;
		}
		OWLMarketFragment fragment = (OWLMarketFragment) mSectionsPagerAdapter.getItem( mViewPager.getCurrentItem() );
		if(fragment.onBackPressed() == false) {
			super.onBackPressed();
		}
	}

	/******************
	 * Events
	 ******************/
	//2.19 김태호 alert창 내용
	
	public void runKidsLauncher(){
		if(preference.getSelectedApps().size() == 0) {
			final Dialog dialog = new Dialog(this);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
			dialog.setContentView(R.layout.dialog_noapp_selected);
			dialog.show();
			Button btn = (Button)dialog.findViewById(R.id.okBtn);
			typeface = Typeface.createFromAsset(getAssets(), "fonts/MyriadPro-Semibold.otf");
			btn.setTypeface(typeface);
			btn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
		}
		else {
			Intent i = new Intent("android.intent.action.MAIN");
			i.addCategory("android.intent.category.HOME");
			List<ResolveInfo> homeList = getPackageManager().queryIntentActivities(i, 0);
			if (!homeList.isEmpty() && NesterUtils.checkLauncherSelctor(getApplicationContext())) {
				Intent intent = new Intent(getApplicationContext(), LauncherGuideActivity.class);
				intent.putExtra("EXIT", 0);
				startActivity(intent);
			}
			else {
				preference.setParentMode( false );
				preference.setActiveState( true );
				NesterUtils.makePrefered(getApplicationContext(), false);
			}
			finish();
		}
	}
	

	/******************
	 * FragmentPagerAdapter
	 ******************/
	public class SectionsPagerAdapter extends FragmentStatePagerAdapter  implements ColorPagerAdapter{
		final static int NUM_OF_PAGES = 5;
		final int[] COLORS = new int[ NUM_OF_PAGES ];

		private final Fragment selectionFragment = new OWLAppSelectionFragment();
		private final Fragment marketFragment = new OWLMarketFragment();
		private final Fragment timerFragment = new OWLTimerFragment();
		private final Fragment itemFragment = new OWLItemFragment();
		private final Fragment preferenceFragment = new OWLPreferenceFragment();

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
			COLORS[ 0 ] = getResources().getColor(R.color.step_1_color);
			COLORS[ 1 ] = getResources().getColor(R.color.step_2_color);
			COLORS[ 2 ] = getResources().getColor(R.color.step_3_color);
			COLORS[ 3 ] = getResources().getColor(R.color.step_4_color);
			COLORS[ 4 ] = getResources().getColor(R.color.step_5_color);
		}
		@Override
		public Fragment getItem(int position) {
			if( position == 0 ){
				return marketFragment;
			}
			else if( position == 1 ){
				return selectionFragment;
			}
			else if( position == 2 ){
				return timerFragment;
			}
			else if( position == 3 ){
				return itemFragment;
			}
			else if( position == 4 ){
				return preferenceFragment;
			}
			return null;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return NUM_OF_PAGES;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.featured).toLowerCase(l);
			case 1:
				return getString(R.string.handpick).toLowerCase(l);
			case 2:
				return getString(R.string.timer).toLowerCase(l);
			case 3:
				return getString(R.string.item).toLowerCase(l);
			case 4:
				return getString(R.string.preference).toLowerCase(l);
			}
			return null;
		}

		@Override
		public int getColor(int index) {
			// TODO Auto-generated method stub
			return COLORS[ index ];
		}
	}
}
