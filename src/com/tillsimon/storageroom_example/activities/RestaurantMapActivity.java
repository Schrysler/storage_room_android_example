package com.tillsimon.storageroom_example.activities;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.tillsimon.storageroom_example.R;
import com.tillsimon.storageroom_example.activities.maphelper.RestaurantOverlayItem;
import com.tillsimon.storageroom_example.activities.maphelper.RestaurantsOverlay;
import com.tillsimon.storageroom_example.application.AppConfigInterface;
import com.tillsimon.storageroom_example.model.database.RestaurantsDatabaseHelper;
import com.tillsimon.storageroom_example.model.database.RestaurantsTableInterface;
import com.tillsimon.storageroom_example.model.webconnect.RestaurantsDownloader;
import com.tillsimon.storageroom_example.model.webconnect.RestaurantsDownloaderDelegate;

public class RestaurantMapActivity extends MapActivity implements RestaurantsDownloaderDelegate {
	
	private static final String[] LOADED_DB_ATTRIBUTES = {"_id", RestaurantsTableInterface.COLUMN_NAME, RestaurantsTableInterface.COLUMN_TEXT, RestaurantsTableInterface.COLUMN_LOCATION_LAT, RestaurantsTableInterface.COLUMN_LOCATION_LNG};
	private static final String QUERY_ORDERBY_ARGUMENT = RestaurantsTableInterface.COLUMN_NAME + " ASC";
	
	private RestaurantsDownloader mRestaurantsDownloader;
	private RestaurantsDatabaseHelper mRestaurantsDatabaseHelper;
	private SQLiteDatabase mDatabase;
	private Cursor mCursor;
	
	private MenuInflater mMenuInflater;
	private MapView mMap;
	private MapController mMapController;
	private ProgressDialog mDownloadProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.restaurantsmap);
		mMap = (MapView) findViewById(R.id.mapview);
		mMap.setBuiltInZoomControls(true);
		mMapController = mMap.getController();
		mMapController.setZoom(AppConfigInterface.initialMapZoom);		
		mRestaurantsDatabaseHelper = new RestaurantsDatabaseHelper(this);

		displayRestaurantsFromDatabase();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if (mRestaurantsDownloader!=null) {
			mRestaurantsDownloader.cancel(true);
			mRestaurantsDownloader = null;
		}
		if ((mDownloadProgressDialog != null) && (mDownloadProgressDialog.isShowing())) {
			mDownloadProgressDialog.dismiss();
		}
		mRestaurantsDatabaseHelper.close();
		Log.d(AppConfigInterface.TAG,"Activity stopped");
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
		displayRestaurantsFromDatabase();
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
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		mMenuInflater = getMenuInflater();
		mMenuInflater.inflate(R.menu.menumap, menu);
		return true;
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
	    	case R.id.gotolist:
	    		Intent intent = new Intent(this,RestaurantListViewActivity.class);
	    		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    		startActivity(intent);
	    		return true;
	    	case R.id.maprefresh:
	    		if (mRestaurantsDownloader==null) {
	    			try {
	    				((RestaurantsOverlay)mMap.getOverlays().get(0)).hideBalloon();
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
	
	private void displayRestaurantsFromDatabase() {
		loadRestaurantsToCursor();		
		OverlayItem[] overlayItemArray = convertDatabaseCursorToOverlayItems();
		Drawable overlayItemIcon = this.getResources().getDrawable(R.drawable.storageroom_pin);
		RestaurantsOverlay restaurantsOverlay = new RestaurantsOverlay(this, overlayItemIcon, mMap);
		List<Overlay> mapOverlaysList = mMap.getOverlays();

		if (overlayItemArray != null) {
			for (int item = 0; item < overlayItemArray.length; item++) {
				restaurantsOverlay.addOverlayItem(overlayItemArray[item]);
			}
			if (mapOverlaysList.isEmpty()) {
				mapOverlaysList.add(0, restaurantsOverlay);
			}
			else {
				mapOverlaysList.set(0, restaurantsOverlay);
			}			
		}
	}

	private OverlayItem[] convertDatabaseCursorToOverlayItems() {
		int numberOfCursorItems = mCursor.getCount();
		OverlayItem[] overlayItemArray = new OverlayItem[numberOfCursorItems];

		while (mCursor.moveToNext()) {
			try {
				int itemId = mCursor.getInt(mCursor.getColumnIndexOrThrow(RestaurantsTableInterface.COLUMN_ID));
				String itemName = mCursor.getString(mCursor.getColumnIndexOrThrow(RestaurantsTableInterface.COLUMN_NAME));
				String itemDescription = mCursor.getString(mCursor.getColumnIndexOrThrow(RestaurantsTableInterface.COLUMN_TEXT));
				double itemLat = mCursor.getDouble(mCursor.getColumnIndexOrThrow(RestaurantsTableInterface.COLUMN_LOCATION_LAT));
				double itemLng = mCursor.getDouble(mCursor.getColumnIndexOrThrow(RestaurantsTableInterface.COLUMN_LOCATION_LNG));
				GeoPoint itemGeoPoint = new GeoPoint((int) (itemLat * 1E6), (int) (itemLng * 1E6));
				
				int currentCursorIteration = mCursor.getPosition();
				overlayItemArray[currentCursorIteration] = new RestaurantOverlayItem(itemGeoPoint, itemName, itemDescription, itemId);
			}
			catch (IllegalArgumentException e) {
				Toast.makeText(this, R.string.error_columnMissingException, Toast.LENGTH_LONG).show();
				Log.d(AppConfigInterface.TAG, getResources().getString(R.string.error_columnMissingException));
			}
		}
		return overlayItemArray;
	}
	
	private void loadRestaurantsToCursor() {
		try {
			mDatabase = mRestaurantsDatabaseHelper.getReadableDatabase();
			mCursor = mDatabase.query(RestaurantsTableInterface.TABLE_NAME, LOADED_DB_ATTRIBUTES, null, null, null, null, QUERY_ORDERBY_ARGUMENT);
			startManagingCursor(mCursor);
		}
		catch (SQLiteException e) {
			Log.e(AppConfigInterface.TAG,"SQLiteException", e);
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

}
