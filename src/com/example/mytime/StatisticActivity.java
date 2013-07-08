package com.example.mytime;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class StatisticActivity extends SherlockFragmentActivity {
	private final String TAG = "StatisticActivity";
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final ActionBar bar = getSupportActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		bar.setDisplayShowHomeEnabled(false);
        bar.setDisplayShowTitleEnabled(false);
        
        bar.addTab(bar.newTab()
        				.setText(R.string.graph)
        				.setTabListener(new TabListener<GraphStatisticFragment>(
        						this, getString(R.string.graph), GraphStatisticFragment.class)));
        bar.addTab(bar.newTab()
        		.setText(R.string.list)
        		.setTabListener(new TabListener<ListStatisticFragment>(
        				this, getString(R.string.list), ListStatisticFragment.class)));
        if (savedInstanceState != null) {
            bar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
        }
        
	}
	
	 @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("tab", getSupportActionBar().getSelectedNavigationIndex());
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public static class TabListener<T extends Fragment> implements ActionBar.TabListener {
		private final SherlockFragmentActivity mActivity;
		private final String mTag;
		private final Class<T> mClass;
		private final Bundle mArgs;
		private Fragment mFragment;
		
		public TabListener(SherlockFragmentActivity activity, String tag, Class<T> clz) {
			this(activity, tag, clz, null);
		}
		
		public TabListener(SherlockFragmentActivity activity, String tag, Class<T>clz, Bundle args) {
			mActivity = activity;
			mTag = tag;
			mClass = clz;
			mArgs = args;
			
			 // Check to see if we already have a fragment for this tab, probably
            // from a previously saved state.  If so, deactivate it, because our
            // initial state is that a tab isn't shown.
            mFragment = mActivity.getSupportFragmentManager().findFragmentByTag(mTag);
            if (mFragment != null && !mFragment.isDetached()) {
                FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
                ft.detach(mFragment);
                ft.commit();
            }
		}
		
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			if (mFragment == null) {
				mFragment = Fragment.instantiate(mActivity, mClass.getName(), mArgs);
				ft.add(android.R.id.content, mFragment, mTag);
			} else {
				ft.attach(mFragment);
			}
		}
		
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			if (mFragment != null)
				ft.detach(mFragment);
		}
		
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
            Toast.makeText(mActivity, "Reselected!", Toast.LENGTH_SHORT).show();
        }
	}
	
	@Override
	public void onBackPressed() {
		Log.d(TAG, "onBackPressed");
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addCategory(Intent.CATEGORY_HOME);
		startActivity(intent);
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_task:
			Log.d(TAG, "action_task selected");
			//TaskFragment taskFragment = new TaskFragment();
			//getSupportFragmentManager().beginTransaction().replace(R.id.activity_main, taskFragment).commit();
			Intent intent = new Intent(this, MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
			break;
			
		case R.id.action_statistics:
			Log.d(TAG, "action_statistics selected");
			//StatisticFragment statisticFragment = new StatisticFragment();
			//getSupportFragmentManager().beginTransaction().replace(R.id.activity_main, statisticFragment).commit();
			//Intent intent = new Intent(this, StatisticActivity.class);
			//startActivity(intent);
		}
		return false;
	}
}
