package com.tillsimon.storageroom_example.model.webconnect;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONObject;

import com.tillsimon.storageroom_example.application.AppConfigInterface;
import com.tillsimon.storageroom_example.model.database.RestaurantsDatabaseHelper;
import com.tillsimon.storageroom_example.model.database.RestaurantsTableInterface;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.util.Log;

public class RestaurantsDownloader extends AsyncTask<Void, Integer, Boolean> {
	
	private RestaurantsDownloaderDelegate mDelegate;
	private HttpGet mHttpRequest;
	private RestaurantsDatabaseHelper mRestaurantsDatabaseHelper;
	private Exception mThrownException;
	private int mNumberOfSuccessfulDownloads;
	
	public RestaurantsDownloader(RestaurantsDownloaderDelegate restaurantDownloaderDelegate) {
		super();		
		mDelegate = restaurantDownloaderDelegate;
		mRestaurantsDatabaseHelper = new RestaurantsDatabaseHelper((Context) restaurantDownloaderDelegate);
	}

	@Override
	protected void onPreExecute() {
		mDelegate.onRestaurantDownloadStart(this);
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {
		boolean success = downloadRestaurants();
		return success;
	}
	
	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		mDelegate.onRestaurantDownloadProgress(this, values);
	}
	
	@Override
	protected void onPostExecute(Boolean success) {
		if (success) {
			mDelegate.onRestaurantDownloadSuccess(this, mNumberOfSuccessfulDownloads);
		}
		else {
			mDelegate.onRestaurantDownloadFailure(this, mThrownException);
		}
	}
	
	@Override
	protected void onCancelled() {
		if (mHttpRequest != null) mHttpRequest.abort();
		mRestaurantsDatabaseHelper.close();
		Log.d(AppConfigInterface.TAG,"Task cancelled");
	}

	private boolean downloadRestaurants() {
        HttpResponse httpResponse;
        HttpEntity httpEntity;
        BufferedReader bufferedReader;
        StringBuilder jsonResponseStringBuilder;
        String bufferedReaderLine;
        String jsonResponseString = "";
        mNumberOfSuccessfulDownloads = 0;
        mThrownException = null;
        
		HttpParams httpParameters = new BasicHttpParams();			
		HttpConnectionParams.setConnectionTimeout(httpParameters, AppConfigInterface.timeoutConnection);			
		HttpConnectionParams.setSoTimeout(httpParameters, AppConfigInterface.timeoutSocket);
		HttpClient httpClient = new DefaultHttpClient(httpParameters);			
        mHttpRequest = new HttpGet(AppConfigInterface.STORAGEROOM_RESTAURANTS_URL);
        
        if (!isCancelled()) {
        	try {
	        	httpResponse = httpClient.execute(mHttpRequest);
	            httpEntity = httpResponse.getEntity();
	            
	            bufferedReader = new BufferedReader(new InputStreamReader(httpEntity.getContent(), "UTF-8"));          
	            jsonResponseStringBuilder = new StringBuilder();	            
	            while (((bufferedReaderLine = bufferedReader.readLine()) != null) && (!isCancelled())) {
	                jsonResponseStringBuilder.append(bufferedReaderLine);
	            }
	            bufferedReader.close();            
	            jsonResponseString = jsonResponseStringBuilder.toString();
	            
				Log.d(AppConfigInterface.TAG, jsonResponseString);
	        }
        	catch (Exception e) {
	        	Log.e(AppConfigInterface.TAG,"IOException", e);
	        	mThrownException = e;
	        	return false;
	        }
        }	        
        
        if (!isCancelled()) {
        	try {
	        	SQLiteDatabase restaurantsDatabase = mRestaurantsDatabaseHelper.getWritableDatabase();
	    		SQLiteStatement precompiledDbInsertStatement = restaurantsDatabase.compileStatement(RestaurantsTableInterface.INSERT_STATEMENT);
	    		restaurantsDatabase.delete(RestaurantsTableInterface.TABLE_NAME, null, null);
	    		
				JSONObject restaurantsJsonObject = new JSONObject(jsonResponseString);
				JSONObject restaurantsJsonArrayObject = restaurantsJsonObject.getJSONObject("array");
				JSONArray restaurantsJsonArray = restaurantsJsonArrayObject.getJSONArray("resources");
				for (int jsonArrayCounter = 0; jsonArrayCounter < restaurantsJsonArray.length(); jsonArrayCounter++)
				{
					JSONObject restaurantsJsonArrayEntry = restaurantsJsonArray.getJSONObject(jsonArrayCounter);
					precompiledDbInsertStatement.bindString(1, restaurantsJsonArrayEntry.getString("name"));
					precompiledDbInsertStatement.bindString(2, restaurantsJsonArrayEntry.getString("text"));
					precompiledDbInsertStatement.bindString(3, restaurantsJsonArrayEntry.getString("address"));
					precompiledDbInsertStatement.bindString(4, restaurantsJsonArrayEntry.getString("website"));
					precompiledDbInsertStatement.bindString(5, restaurantsJsonArrayEntry.getString("last_visit"));
					JSONObject restaurantLocationJsonObject = restaurantsJsonArrayEntry.getJSONObject("location");
					precompiledDbInsertStatement.bindDouble(6, restaurantLocationJsonObject.getDouble("lat"));
					precompiledDbInsertStatement.bindDouble(7, restaurantLocationJsonObject.getDouble("lng"));
					precompiledDbInsertStatement.bindLong(8, restaurantsJsonArrayEntry.getInt("price_range"));
					precompiledDbInsertStatement.bindLong(9, restaurantsJsonArrayEntry.getInt("stars"));
					precompiledDbInsertStatement.bindString(10, String.valueOf(restaurantsJsonArrayEntry.getBoolean("vegetarian_menu")));
					JSONObject restaurantImageJsonObject = restaurantsJsonArrayEntry.getJSONObject("image");
					precompiledDbInsertStatement.bindString(11, restaurantImageJsonObject.getString("@url"));
					JSONObject restaurantImagePreviewJsonObject = restaurantsJsonArrayEntry.getJSONObject("preview_image");
					precompiledDbInsertStatement.bindString(12, restaurantImagePreviewJsonObject.getString("@url"));
					precompiledDbInsertStatement.bindString(13, restaurantsJsonArrayEntry.getString("@created_at"));
					
					long rowIdOfDbInsert = precompiledDbInsertStatement.executeInsert();
					if (rowIdOfDbInsert != -1) {
						mNumberOfSuccessfulDownloads++;
					}
				}				
	            
	        }
        	catch (Exception e) {
	        	mThrownException = e;
	        	return false;
			}
        	finally {
				mRestaurantsDatabaseHelper.close();
			}
        }
    return true;
	}	
}
