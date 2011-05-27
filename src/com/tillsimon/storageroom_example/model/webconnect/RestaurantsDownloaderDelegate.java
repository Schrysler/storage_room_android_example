package com.tillsimon.storageroom_example.model.webconnect;

public abstract interface RestaurantsDownloaderDelegate {
	
	public abstract void onRestaurantDownloadStart(RestaurantsDownloader downloader);
	public abstract void onRestaurantDownloadProgress(RestaurantsDownloader downloader, Integer... progress);
	public abstract void onRestaurantDownloadSuccess(RestaurantsDownloader downloader, Integer numberOfLoadedItems);
	public abstract void onRestaurantDownloadFailure(RestaurantsDownloader downloader, Exception exception);
}
