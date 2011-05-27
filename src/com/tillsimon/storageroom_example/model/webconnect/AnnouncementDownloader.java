package com.tillsimon.storageroom_example.model.webconnect;

import java.io.BufferedReader;
import java.io.IOException;
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

import android.os.AsyncTask;
import android.util.Log;

public class AnnouncementDownloader extends AsyncTask<Void, Integer, Boolean> {

	private AnnouncementDownloaderDelegate mDelegate;
	private Exception mThrownException;
	private HttpGet mHttpRequest;
	private String mAnnouncementText;
	private String mAnnouncementUrl;
	
	public AnnouncementDownloader(AnnouncementDownloaderDelegate delegate) {
		super();
		mDelegate = delegate;
	}

	@Override
	protected void onPreExecute() {
		mDelegate.onAnnouncementDownloadStart(this);
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {
		boolean successful = downloadAnnouncements();
		return successful;
	}
	
	@Override
	protected void onProgressUpdate(Integer... progress) {
		super.onProgressUpdate(progress);
		mDelegate.onAnnouncementDownloadProgress(this, progress);
	}
	
	@Override
	protected void onPostExecute(Boolean successful) {
		if (successful) {
			mDelegate.onAnnouncementDownloadSuccess(this, mAnnouncementText, mAnnouncementUrl);
		}
		else {
			mDelegate.onAnnouncementDownloadFailure(this, mThrownException);
		}
	}
	
	@Override
	protected void onCancelled() {
		mHttpRequest.abort();
		Log.d(AppConfigInterface.TAG,"Task cancelled");
	}
	
	protected boolean downloadAnnouncements() {
		HttpResponse httpResponse;
        HttpEntity httpEntity;
        HttpClient httpClient;
        HttpParams httpParameters;
        BufferedReader bufferedReader;
        String bufferedReaderLine;
        StringBuilder jsonResponseStringBuilder;
        String jsonResponseString = "";
        mThrownException = null;
		
		httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, AppConfigInterface.timeoutConnection);			
		HttpConnectionParams.setSoTimeout(httpParameters, AppConfigInterface.timeoutSocket);

		httpClient = new DefaultHttpClient(httpParameters);
        mHttpRequest = new HttpGet(AppConfigInterface.STORAGEROOM_ANNOUNCEMENTS_URL);
        
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
        	catch (IOException e) {
	        	mThrownException = e;
	        	return false;
	        }
        }	        
        
        if (!isCancelled()) {
        	try{
        		JSONObject announcementsJsonObject = new JSONObject(jsonResponseString);
				JSONObject announcementsJsonArrayObject = announcementsJsonObject.getJSONObject("array");
				JSONArray announcementsJsonArray = announcementsJsonArrayObject.getJSONArray("resources");					
				JSONObject announcementsJsonArrayEntry = announcementsJsonArray.getJSONObject(0);
				mAnnouncementText = announcementsJsonArrayEntry.getString("text");
				mAnnouncementUrl = announcementsJsonArrayEntry.getString("url");
	        }
        	catch (Exception e) {
	        	mThrownException = e;
	        	return false;
			}
        }
        return true;
	}

}
