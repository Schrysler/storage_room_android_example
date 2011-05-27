package com.tillsimon.storageroom_example.activities;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.commonsware.cwac.cache.SimpleWebImageCache;
import com.commonsware.cwac.thumbnail.ThumbnailBus;
import com.commonsware.cwac.thumbnail.ThumbnailMessage;
import com.tillsimon.storageroom_example.R;
import com.tillsimon.storageroom_example.application.AppConfigInterface;
import com.tillsimon.storageroom_example.application.RestaurantApplication;
import com.tillsimon.storageroom_example.model.database.RestaurantsDatabaseHelper;
import com.tillsimon.storageroom_example.model.database.RestaurantsTableInterface;

public class RestaurantDetailActivity extends Activity{
	
	private static final String[] LOADED_DB_ATTRIBUTES = {RestaurantsTableInterface.COLUMN_NAME, RestaurantsTableInterface.COLUMN_IMAGE_URL, RestaurantsTableInterface.COLUMN_TEXT, RestaurantsTableInterface.COLUMN_ADDRESS, 
		RestaurantsTableInterface.COLUMN_WEBSITE, RestaurantsTableInterface.COLUMN_LAST_VISIT, RestaurantsTableInterface.COLUMN_PRICE_RANGE, RestaurantsTableInterface.COLUMN_RATING, 
		RestaurantsTableInterface.COLUMN_VEGETARIAN, RestaurantsTableInterface.COLUMN_ENTRY_CREATED};
	private static final String[] PRICE_RANGE_MAPPING = {"$", "$$", "$$$", "$$$$", "$$$$$"};
	
	private RestaurantsDatabaseHelper restaurantsDbHelper;
	private SimpleWebImageCache<ThumbnailBus, ThumbnailMessage> cache=null;
	private RestaurantApplication mApp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mApp = (RestaurantApplication) getApplication();
		cache = mApp.getCache();
		cache.getBus().register(getBusKey(), onCache);
		
		restaurantsDbHelper = new RestaurantsDatabaseHelper(this);
		setContentView(R.layout.restaurantsdetails);
		
		TextView name = (TextView)findViewById(R.id.name_text);
		RatingBar rating = (RatingBar)findViewById(R.id.rating_bar);
		ImageView image = (ImageView)findViewById(R.id.detailpic_image);
		TextView description = (TextView)findViewById(R.id.description_text);
		TextView address = (TextView)findViewById(R.id.address_text);
		TextView website = (TextView)findViewById(R.id.website_text);
		TextView lastVisit = (TextView)findViewById(R.id.lastvisit_text);
		TextView priceRange = (TextView)findViewById(R.id.pricerange_text);		
		TextView veggiMenu = (TextView)findViewById(R.id.veggi_text);
		TextView createdAt = (TextView)findViewById(R.id.created_text);
		
		int detailItem = (int)getIntent().getExtras().getInt("clickedItemId");
		try{
			Cursor cursor = loadSingleDatabaseEntry(detailItem);
			
			if (cursor.moveToFirst()) {
				try {
					name.setText(cursor.getString(cursor.getColumnIndexOrThrow(RestaurantsTableInterface.COLUMN_NAME)));
					rating.setRating(cursor.getInt((cursor.getColumnIndexOrThrow(RestaurantsTableInterface.COLUMN_RATING))));
					String url = cursor.getString(cursor.getColumnIndexOrThrow(RestaurantsTableInterface.COLUMN_IMAGE_URL));
					fetchPicture(image, url);
					description.setText(cursor.getString(cursor.getColumnIndexOrThrow(RestaurantsTableInterface.COLUMN_TEXT)));
					address.setText(cursor.getString(cursor.getColumnIndexOrThrow(RestaurantsTableInterface.COLUMN_ADDRESS)));
					website.setText(cursor.getString(cursor.getColumnIndexOrThrow(RestaurantsTableInterface.COLUMN_WEBSITE)));
					lastVisit.setText(parseDateLastVisit(cursor.getString(cursor.getColumnIndexOrThrow(RestaurantsTableInterface.COLUMN_LAST_VISIT))));
					priceRange.setText(PRICE_RANGE_MAPPING[cursor.getInt(cursor.getColumnIndexOrThrow(RestaurantsTableInterface.COLUMN_PRICE_RANGE))]);
					boolean veggi = Boolean.parseBoolean(cursor.getString(cursor.getColumnIndexOrThrow(RestaurantsTableInterface.COLUMN_VEGETARIAN)));
					if (veggi) {
						veggiMenu.setText(getResources().getString(R.string.yes));
					}						
					else {
						veggiMenu.setText(getResources().getString(R.string.no));
					}					
					createdAt.setText("created at "+ parseDateCreated(cursor.getString(cursor.getColumnIndexOrThrow(RestaurantsTableInterface.COLUMN_ENTRY_CREATED))));
				} catch (IllegalArgumentException e) {
					Toast.makeText(this, R.string.error_columnMissingException, Toast.LENGTH_LONG).show();
					Log.e(AppConfigInterface.TAG, "IllegalArgumentException", e);
				}
			}
		} finally {
			restaurantsDbHelper.close();
		}
	}	

	@Override
	protected void onDestroy() {
		super.onDestroy();
		restaurantsDbHelper.close();
		cache.getBus().unregister(onCache);
	}	

	protected Cursor loadSingleDatabaseEntry(int id) {
		Cursor cursor=null;
		try {
			SQLiteDatabase db = restaurantsDbHelper.getReadableDatabase();
			cursor = db.query(RestaurantsTableInterface.TABLE_NAME, LOADED_DB_ATTRIBUTES, "_id="+String.valueOf(id), null, null, null, null);
			startManagingCursor(cursor);
		}
		catch (SQLiteException e) {
			Toast.makeText(this, getResources().getString(R.string.error_sqliteException), Toast.LENGTH_LONG).show();
			Log.e(AppConfigInterface.TAG,"SQLiteException", e);
		}
		return cursor;
	}
	
	public String parseDateLastVisit(String dateLastVisit) {
		SimpleDateFormat sourceFormat = 
			(SimpleDateFormat) new SimpleDateFormat("yyyy-MM-dd");
		try {
			return SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT).format(sourceFormat.parse(dateLastVisit));			
		}
		catch (ParseException e) {
			Log.e(AppConfigInterface.TAG,"ParseException", e);
		}
		return dateLastVisit;		
	}
	
	public String parseDateCreated(String dateCreated) {
		SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		try {
			return SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.MEDIUM).format(sourceFormat.parse(dateCreated.replace("Z", "+0000")));			
		}
		catch (ParseException e) {
			Log.e(AppConfigInterface.TAG,"ParseException", e);
		}
		return dateCreated;		
	}

	private void fetchPicture(ImageView image, String url) {
		if (image!=null) {
			ThumbnailMessage msg=cache.getBus().createMessage(getBusKey());
															
			msg.setImageView(image);
			msg.setUrl(url);
			
			try {
				cache.notify(msg.getUrl(), msg);
			}
			catch (Throwable t) {
				Log.e(AppConfigInterface.TAG, "Exception trying to fetch image", t);
			}
		}
	}
	
	private String getBusKey() {
		return(toString());
	}
	
	private ThumbnailBus.Receiver<ThumbnailMessage> onCache = new ThumbnailBus.Receiver<ThumbnailMessage>() {
		public void onReceive(final ThumbnailMessage message) {
			final ImageView image=message.getImageView();
			
			runOnUiThread(new Runnable() {
				public void run() {					
						image.setImageDrawable(cache.get(message.getUrl()));					
				}
			});
		}
	};
	

}
