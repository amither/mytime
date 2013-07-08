package com.example.mytime;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

public class RefreshTimeService extends Service {
	private final String tag = "RefreshTimeService";
	private final IBinder mBinder = new LocalBinder();
	private Handler mHandler = new Handler();
	private Runnable updateUITime = null;
	private Runnable runUpdateTime = new Runnable() {
		@Override
		public void run() {
			/* update UI time */
			Log.d(tag, "runUpdateTime");
			updateUITime.run();
			mHandler.postDelayed(this, 1000);
		}
	};
	private boolean isRefreshing = false;
	
	public class LocalBinder extends Binder {
		public RefreshTimeService getService() {
			return RefreshTimeService.this;
		}
	}

	@Override
	public void onCreate() {
		Log.d(tag, "onCreate");

	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(tag, "onBind");
		return mBinder;
	}

	@Override
	public void onDestroy() {
		Log.d(tag, "onDestroy");
	}

	public void startRefreshTime(Runnable run) {
		Log.d(tag, "startRefreshTime");
		updateUITime = run;
		mHandler.postDelayed(runUpdateTime, 1000);
		isRefreshing = true;
	}

	public void stopRefreshTime() {
		if (isRefreshing) {
			Log.d(tag, "stopRefreshTime");
			mHandler.removeCallbacks(runUpdateTime);
			isRefreshing = false;
		}
	}
}