package com.tillsimon.storageroom_example.application;

import android.app.Application;
import android.util.Log;

import com.commonsware.cwac.cache.SimpleWebImageCache;
import com.commonsware.cwac.thumbnail.ThumbnailBus;
import com.commonsware.cwac.thumbnail.ThumbnailMessage;

public class RestaurantApplication extends Application {

	public boolean mIsInitialLaunch;
	private ThumbnailBus mBus = new ThumbnailBus();
	private SimpleWebImageCache<ThumbnailBus, ThumbnailMessage> mCache = new SimpleWebImageCache<ThumbnailBus, ThumbnailMessage>(null, null, 101, mBus);
	    
	@Override
	public void onCreate() {
		super.onCreate();
		
		mIsInitialLaunch = true;
		Log.i(AppConfigInterface.TAG,"App created");
	}

	public ThumbnailBus getBus() {
		return(mBus);
	}
	
	public SimpleWebImageCache<ThumbnailBus, ThumbnailMessage> getCache() {
		return(mCache);
	}
	

}
