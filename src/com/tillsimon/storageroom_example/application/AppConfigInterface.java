package com.tillsimon.storageroom_example.application;

public interface AppConfigInterface {
	
	public static final String TAG = "RestaurantGuide";
	
	public static final String STORAGEROOM_RESTAURANTS_URL  =	"https://api.storageroomapp.com/accounts/4d13574cba05613d25000004/collections/4d960916ba05617333000005/entries.json?auth_token=DZHpRbsJ7VgFXhybKWmT";
	public static final String STORAGEROOM_ANNOUNCEMENTS_URL = "https://api.storageroomapp.com/accounts/4d13574cba05613d25000004/collections/4d96091dba0561733300001b/entries.json?auth_token=DZHpRbsJ7VgFXhybKWmT&per_page=1&sort=updated_at&order=desc";
	public static final int timeoutConnection = 5000;
	public static final int timeoutSocket = 5000;
	public static final int initialMapZoom = 3;

}
