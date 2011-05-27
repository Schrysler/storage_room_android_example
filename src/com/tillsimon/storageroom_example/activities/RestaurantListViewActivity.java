package com.tillsimon.storageroom_example.activities;

import java.io.IOException;
import java.net.URI;

import org.json.JSONException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;

import com.commonsware.cwac.thumbnail.ThumbnailAdapter;
import com.tillsimon.storageroom_example.R;
import com.tillsimon.storageroom_example.application.AppConfigInterface;
import com.tillsimon.storageroom_example.application.RestaurantApplication;
import com.tillsimon.storageroom_example.model.database.RestaurantsDatabaseHelper;
import com.tillsimon.storageroom_example.model.database.RestaurantsTableInterface;
import com.tillsimon.storageroom_example.model.webconnect.AnnouncementDownloader;
import com.tillsimon.storageroom_example.model.webconnect.AnnouncementDownloaderDelegate;
import com.tillsimon.storageroom_example.model.webconnect.RestaurantsDownloader;
import com.tillsimon.storageroom_example.model.webconnect.RestaurantsDownloaderDelegate;

public class RestaurantListViewActivity extends Activity implements OnItemClickListener, OnClickListener, RestaurantsDownloaderDelegate, AnnouncementDownloaderDelegate {
	
	private static final String[] LOADED_DB_ATTRIBUTES = {"_id", RestaurantsTableInterface.COLUMN_NAME, RestaurantsTableInterface.COLUMN_TEXT, RestaurantsTableInterface.COLUMN_IMAGE_PREVIEW_URL};
	private static final String QUERY_ORDERBY_ARGUMENT = RestaurantsTableInterface.COLUMN_NAME + " ASC";
	private static final String[] ADAPTER_DB_ATTRIBUTES = {RestaurantsTableInterface.COLUMN_NAME, RestaurantsTableInterface.COLUMN_TEXT, RestaurantsTableInterface.COLUMN_IMAGE_PREVIEW_URL};
	private static final int[] ADAPTER_LISTVIEW_WIDGETS = {R.id.listItemName, R.id.listItemDesc, R.id.listItemPic};
	private static final int[] THUMBNAIL_ADAPTER_IMAGE_IDS={R.id.listItemPic};
	
	private RestaurantApplication mApp;
	private RestaurantsDatabaseHelper mRestaurantsDatabaseHelper;
	private SQLiteDatabase mDatabase;
	private RestaurantsDownloader mRestaurantsDownloader;
	private AnnouncementDownloader mAnnouncementDownloader;
	private ThumbnailAdapter mThumbnailAdapter;
	private SimpleCursorAdapter mSimpleCursorAdapter;
	private Cursor mCursor = null;
	
	private ListView mListView;
	private MenuInflater mMenuInflater;
	private ProgressDialog mDownloadProgressDialog;
	private LinearLayout mAnnouncementLayout;
	private TextView mAnnouncementText;
	private ImageView mAnnouncementCloseButtonImageView;
	private Animation mAnnouncementFadeInAnimation;
	private Animation mAnnouncementFadeOutAnimation;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.restaurantslistview);
		mApp = (RestaurantApplication) getApplication();
		mRestaurantsDatabaseHelper = new RestaurantsDatabaseHelper(this);
		mThumbnailAdapter = null;
		mSimpleCursorAdapter = null;
		
		mListView = (ListView) findViewById(R.id.restaurants_listView);
		mListView.setOnItemClickListener(this);
		mListView.setTextFilterEnabled(true);
		mAnnouncementLayout = (LinearLayout) findViewById(R.id.announcement_view);
		mAnnouncementText = (TextView) findViewById(R.id.announcement_text);
		mAnnouncementCloseButtonImageView = (ImageView) findViewById(R.id.announcement_close);
		mAnnouncementCloseButtonImageView.setOnClickListener(this);
		
		//at initial launch download should be started
		if (mApp.mIsInitialLaunch) {
			Log.d(AppConfigInterface.TAG,"Initial app launch");
			try {
				mRestaurantsDownloader = new RestaurantsDownloader(this);
				mAnnouncementDownloader = new AnnouncementDownloader(this);
				mRestaurantsDownloader.execute();
				mAnnouncementDownloader.execute();
    		}
			catch (IllegalStateException e) {
    			Toast.makeText(this, getResources().getString(R.string.error_asyncTaskExecuteException), Toast.LENGTH_LONG).show();
    			Log.e(AppConfigInterface.TAG, "IllegalStateException" ,e);	    			
    		}
			mApp.mIsInitialLaunch=false;
		}		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		loadRestaurantsToListView();
		Log.d(AppConfigInterface.TAG, "onStart");
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mRestaurantsDownloader!=null) {
			mRestaurantsDownloader.cancel(true);
			mRestaurantsDownloader=null;
		}
		if (mAnnouncementDownloader!=null) {
			mAnnouncementDownloader.cancel(true);
			mAnnouncementDownloader = null;
		}
		if ((mDownloadProgressDialog != null) && (mDownloadProgressDialog.isShowing())) {
			mDownloadProgressDialog.dismiss();
		}
		mRestaurantsDatabaseHelper.close();
		Log.d(AppConfigInterface.TAG,"Activity stopped");
	}
	
    @Override
	public void onDestroy() {
		super.onDestroy();		
		mThumbnailAdapter.close();
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	mMenuInflater = getMenuInflater();
    	mMenuInflater.inflate(R.menu.menulist, menu);
    	return true;
    }
    
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
	    	case R.id.gotomap:
	    		startActivity(new Intent(this,RestaurantMapActivity.class));
	    		return true;
	    	case R.id.listrefresh:
	    		if (mRestaurantsDownloader==null) {
	    			try {
	    				mRestaurantsDownloader = new RestaurantsDownloader(this);
	    				mRestaurantsDownloader.execute();
		    		}
	    			catch (IllegalStateException e) {		    			
		    			Toast.makeText(this, getResources().getString(R.string.error_asyncTaskExecuteException), Toast.LENGTH_LONG).show();
		    			Log.e(AppConfigInterface.TAG, "IllegalStateException", e);	    			
		    		}
	    		}	    		
	    		return true;
    	}
    	return false;
    }

	@Override
	public void onItemClick(AdapterView<?> listview, View rowview, int viewposition, long rowid) {
		Log.d(AppConfigInterface.TAG, String.valueOf(rowid));
		Intent intentDetails = new Intent(this, RestaurantDetailActivity.class);
		intentDetails.putExtra("clickedItemId", (int)rowid);
		startActivity(intentDetails);
	}	

	@Override
	public void onClick(View closeView) {
		hideAnnouncement();
	}	

	@Override
	public void onAnnouncementDownloadStart(AnnouncementDownloader downloader) {}

	@Override
	public void onAnnouncementDownloadProgress(AnnouncementDownloader downloader, Integer... progress) {}

	@Override
	public void onAnnouncementDownloadSuccess(AnnouncementDownloader downloader, String announcementText, String announcementUrl) {
		if ((mAnnouncementLayout!=null) && (mAnnouncementText!=null) && (announcementText!="null")) {
			showAnnouncement(announcementText, announcementUrl);
		}
		mAnnouncementDownloader = null;
	}

	@Override
	public void onAnnouncementDownloadFailure(AnnouncementDownloader downloader, Exception exception) {
		mAnnouncementDownloader = null;
		Log.e(AppConfigInterface.TAG, exception.toString());		
	}

	@Override
	public void onRestaurantDownloadStart(RestaurantsDownloader downloader) {
		mDownloadProgressDialog = new ProgressDialog(this);
		mDownloadProgressDialog.setOwnerActivity(this);
		mDownloadProgressDialog.setMessage(getResources().getString(R.string.progressDialogMessage));
		mDownloadProgressDialog.setCancelable(false);
		mDownloadProgressDialog.show();
		Log.d(AppConfigInterface.TAG,"start delegated");		
	}

	@Override
	public void onRestaurantDownloadProgress(RestaurantsDownloader downloader, Integer... progress) {}

	@Override
	public void onRestaurantDownloadSuccess(RestaurantsDownloader downloader, Integer numberOfLoadedItems) {
		String toastSuccessMessage;
		
		mRestaurantsDownloader = null;
		if (numberOfLoadedItems == 1) {
			toastSuccessMessage = String.valueOf(numberOfLoadedItems) + " " + getResources().getString(R.string.toastInsertSingle);
		}
		else {
			toastSuccessMessage = String.valueOf(numberOfLoadedItems)+ " " + getResources().getString(R.string.toastInsertMultiple);
		}
		Toast.makeText(this, toastSuccessMessage, Toast.LENGTH_LONG).show();
		mDownloadProgressDialog.dismiss();
		mCursor.requery();
		mSimpleCursorAdapter.notifyDataSetChanged();
	}

	@Override
	public void onRestaurantDownloadFailure(RestaurantsDownloader downloader, Exception exception) {
		String toastErrorString;
		
		mRestaurantsDownloader = null;
		mDownloadProgressDialog.dismiss();
		
		if (exception instanceof IOException) {
			toastErrorString = getResources().getString(R.string.error_webException);
		}
		else if (exception instanceof SQLiteException) {
			toastErrorString = getResources().getString(R.string.error_sqliteException);
		}
		else if (exception instanceof SQLException) {
			toastErrorString = getResources().getString(R.string.error_sqlException);
		}
		else if (exception instanceof JSONException) {
			toastErrorString = getResources().getString(R.string.error_jsonException);
		}		
		else {
			toastErrorString = getResources().getString(R.string.error_unknown);
		}		
		Toast.makeText(this, toastErrorString, Toast.LENGTH_LONG).show();
		Log.e(AppConfigInterface.TAG, exception.toString());
	}
	
    private void loadRestaurantsToListView() {
    	try {
			mDatabase = mRestaurantsDatabaseHelper.getReadableDatabase();
			mCursor = mDatabase.query(RestaurantsTableInterface.TABLE_NAME, LOADED_DB_ATTRIBUTES, null, null, null, null, QUERY_ORDERBY_ARGUMENT);
			startManagingCursor(mCursor);
		}
		catch (SQLiteException e) {
			Log.e(AppConfigInterface.TAG,"SQLiteException", e);
		}

		mSimpleCursorAdapter = new SimpleCursorAdapter(this, R.layout.listview_item, mCursor, ADAPTER_DB_ATTRIBUTES, ADAPTER_LISTVIEW_WIDGETS);
		mSimpleCursorAdapter.setViewBinder(VIEW_BINDER);
		mThumbnailAdapter = new ThumbnailAdapter(this, mSimpleCursorAdapter, ((RestaurantApplication)getApplication()).getCache(), THUMBNAIL_ADAPTER_IMAGE_IDS);
		mListView.setAdapter(mThumbnailAdapter);
	}

	static final ViewBinder VIEW_BINDER = new ViewBinder() {
		@Override
		public boolean setViewValue(View view, Cursor cursor, int column) {
			if (view.getId() != R.id.listItemPic) {
				return false;
			}			
			URI url = URI.create(cursor.getString(column));
			view.setTag(url);
			return true;
		}		
	};
	
	private void hideAnnouncement() {
		mAnnouncementFadeOutAnimation = new AlphaAnimation(1, 0);
		mAnnouncementFadeOutAnimation.setDuration(800);
		mAnnouncementLayout.startAnimation(mAnnouncementFadeOutAnimation);
		mAnnouncementLayout.setVisibility(View.GONE);
	}
	
	private void showAnnouncement(String announcementText, String announcementUrl) {
		if (announcementUrl != "null") {
			announcementText = announcementText.concat("\n" + announcementUrl);	
		}		
		mAnnouncementText.setText(announcementText);
		mAnnouncementFadeInAnimation = new AlphaAnimation(0,1);
		mAnnouncementFadeInAnimation.setDuration(800);
		mAnnouncementLayout.startAnimation(mAnnouncementFadeInAnimation);
		mAnnouncementLayout.setVisibility(View.VISIBLE);
	}

} //class
