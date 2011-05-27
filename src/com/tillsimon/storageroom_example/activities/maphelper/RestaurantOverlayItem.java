package com.tillsimon.storageroom_example.activities.maphelper;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class RestaurantOverlayItem extends OverlayItem {	
	private int id;

	public RestaurantOverlayItem(GeoPoint point, String title, String snippet, int id) {	
		super(point, title, adjustSnippet(snippet));
		this.id = id;
	}

	private static String adjustSnippet(String snippet) {
		if (snippet.length() >= 120) {
			snippet = snippet.substring(0, 100).concat(" ...");
		}
		snippet = snippet.concat("  (click for details)");
		return snippet;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}	

}
