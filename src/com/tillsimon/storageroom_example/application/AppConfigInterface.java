package com.tillsimon.storageroom_example.application;

public interface AppConfigInterface {
	
	//To get Google Maps working, insert your own Google Maps Key in res/layout/restaurantsmap.xml ! 
	
	public static final String TAG = "RestaurantGuide";
		
	public static final String BASE_URL = "https://api.storageroomapp.com/";
	public static final String ACCOUNT_ID = "accounts/4d13574cba05613d25000004/";
	public static final String JSON_FILENAME = "entries.json";
	public static final String PARAM_AUTH_TOKEN = "?auth_token=DZHpRbsJ7VgFXhybKWmT";	

	public static final String COLLECTIONS_ID_RESTAURANTS = "collections/4d960916ba05617333000005/";
	public static final String STORAGEROOM_RESTAURANTS_URL  = BASE_URL + ACCOUNT_ID + COLLECTIONS_ID_RESTAURANTS + JSON_FILENAME + PARAM_AUTH_TOKEN;
	
	public static final String COLLECTIONS_ID_ANNOUNCEMENTS = "collections/4d96091dba0561733300001b/";
	public static final String PARAM_ANNOUNCEMENTS_PERPAGE = "&per_page=1";
	public static final String PARAM_ANNOUNCEMENTS_SORT = "&sort=@updated_at";
	public static final String PARAM_ANNOUNCEMENTS_ORDER = "&order=desc";
	public static final String STORAGEROOM_ANNOUNCEMENTS_URL = BASE_URL + ACCOUNT_ID + COLLECTIONS_ID_ANNOUNCEMENTS + JSON_FILENAME + PARAM_AUTH_TOKEN + PARAM_ANNOUNCEMENTS_PERPAGE + PARAM_ANNOUNCEMENTS_SORT + PARAM_ANNOUNCEMENTS_ORDER;
	public static final int timeoutConnection = 5000;
	public static final int timeoutSocket = 5000;
	public static final int initialMapZoom = 3;

}
