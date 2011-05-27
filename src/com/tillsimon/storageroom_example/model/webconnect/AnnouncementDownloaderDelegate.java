package com.tillsimon.storageroom_example.model.webconnect;

public interface AnnouncementDownloaderDelegate {
	
	public abstract void onAnnouncementDownloadStart(AnnouncementDownloader downloader);
	public abstract void onAnnouncementDownloadProgress(AnnouncementDownloader downloader, Integer... progress);
	public abstract void onAnnouncementDownloadSuccess(AnnouncementDownloader downloader, String announcementText, String announcementUrl);
	public abstract void onAnnouncementDownloadFailure(AnnouncementDownloader downloader, Exception exception);
}
