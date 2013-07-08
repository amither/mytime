package com.example.mytime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import android.app.ActionBar;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.example.mytime.RefreshTimeService.LocalBinder;

public class MainActivity extends SherlockFragmentActivity 
						  implements NewTaskDialog.NewTaskDialogListener {
	private static final String TAG = "MainActivity"; //debug info
	
	private TextView timeShow;
	private TextView taskView;
	private String taskNow;		
	private long startTimeMillis, stopTimeMillis;	//task start/stop systime
	private boolean isServiceRunning;
	
	private MytimeDatabase db;	//database for task
	
	private SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss"); //task time format 
	private Date date = new Date();
	//the runnable for update the task time
	private Runnable runUpdateTime = new Runnable() {
		public void run() {
			date.setTime(System.currentTimeMillis() - startTimeMillis);
			timeShow.setText(format.format(date));
		}
	};

	private RefreshTimeService refreshTimeService; //service for refresh the UI task time
	private boolean isBound = false; //whether the service bounded
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			LocalBinder localBinder = (LocalBinder) service;
			refreshTimeService = localBinder.getService();
			isBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			isBound = false;
		}
	};
	
	private List<Map<String, Object>> task_list;
	private SimpleAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Sherlock);
		
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		
		Log.d(TAG, "onCreate" + (null == savedInstanceState ? "" : "restore"));
		
		taskNow = null;
		timeShow = (TextView) findViewById(R.id.top_text);
		taskView = (TextView) findViewById(R.id.text_task);
		
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		db = new MytimeDatabase(this);
		MytimeDatabase.taskNamesCursor cursor = db.getTaskName();
		task_list = new ArrayList<Map<String, Object>>();

		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("title", cursor.getColTaskName());
			task_list.add(map);
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("title", "New");
		task_list.add(map);
		
		adapter = new SimpleAdapter(this, task_list,
				R.layout.task_gridview, new String[] { "title" },
				new int[] { R.id.griditem_title });
		GridView gridview = (GridView) findViewById(R.id.task_gridView);
		gridview.setAdapter(adapter);
		gridview.setOnItemClickListener(mMessageClickedHandler);
		
		// Bind to RefreshTimeService
		Intent intent = new Intent(this, RefreshTimeService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		isServiceRunning = false;
		/*
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		for (int i = 1; i <= 3; i++) {
			ActionBar.Tab tab = getSupportActionBar().newTab();
			tab.setText("Tab " + i);
			tab.setTabListener(this);
			getSupportActionBar().addTab(tab);
		}
		*/
	}

	private OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {
		public void onItemClick(AdapterView parent, View v, int position,
				long id) {
			if (position != parent.getLastVisiblePosition()){
				
				stopRefreshTime();
				//zero time
				date.setTime(0);
				timeShow.setText(format.format(date));
				
				// Do something in response to the click
				Log.d(TAG, "id="+id + " position="+position);
				Map<String, Object> map = (Map<String, Object>) parent
						.getItemAtPosition(position);
				String item = (String) map.get("title");
				taskNow = item;
				taskView.setText(item);
				//((TextView)findViewById(R.id.abs_title)).setText(item);
				
				startTimeMillis = System.currentTimeMillis();
				refreshTimeService.startRefreshTime(runUpdateTime);
				isServiceRunning = true;
				
				Toast.makeText(getBaseContext(), "start thread on " + item,
						Toast.LENGTH_SHORT).show();
			}else{
				//add a new task class
				DialogFragment newTaskDialog = new NewTaskDialog();
				newTaskDialog.show(getSupportFragmentManager(), "newTaskDialog");
			}
		}
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.main, menu);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		//getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		//getSupportActionBar().setCustomView(R.layout.abs_title);
		return true;
	}

	public void actionButtonOnClick(View view) {
		stopRefreshTime();
	}
	
	private void stopRefreshTime() {
		refreshTimeService.stopRefreshTime();
		isServiceRunning = false;
		stopTimeMillis = System.currentTimeMillis();
		// write to database
	}
	
	public void onDialogPositiveClick(DialogFragment dialog) {
		
		EditText editText= ((EditText)(dialog.getDialog().findViewById(R.id.newtask_title)));
		if(null == editText) Log.e(TAG, "editText null");
		String newTaskName = editText.getText().toString();
		if(null == newTaskName) Log.e(TAG, "newTaskName null");
		
//		String newTaskName = bundle.getString("title");
		Log.d(TAG, "add new task " + null == newTaskName ? "null" : newTaskName);
		
		// add new task title to database
		db.addTaskName(newTaskName);
		
		// update the gridview
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("title", newTaskName);
		task_list.add(task_list.size()-1, map);
		adapter.notifyDataSetChanged();
		
	}
	
	public void onDialogNegativeClick(DialogFragment dialog) {
		return;
	}
	/*
	 * @Override public void onTabReselected(Tab tab, FragmentTransaction
	 * transaction) { }
	 * 
	 * @Override public void onTabSelected(Tab tab, FragmentTransaction
	 * transaction) { //timeShow.setText(tab.getText()); }
	 * 
	 * @Override public void onTabUnselected(Tab tab, FragmentTransaction
	 * transaction) { }
	 */
	@Override
	protected void onRestart() {
		super.onRestart();
		Log.d(TAG, "onRestart");
		
		if (isServiceRunning == true) {
			refreshTimeService.startRefreshTime(runUpdateTime);
		}
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "onStart");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "onPause");
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "onStop");
		refreshTimeService.stopRefreshTime();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");
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
			break;
			
		case R.id.action_statistics:
			Log.d(TAG, "action_statistics selected");
			//StatisticFragment statisticFragment = new StatisticFragment();
			//getSupportFragmentManager().beginTransaction().replace(R.id.activity_main, statisticFragment).commit();
			Intent intent = new Intent(this, StatisticActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
		}
		return false;
	}
}
