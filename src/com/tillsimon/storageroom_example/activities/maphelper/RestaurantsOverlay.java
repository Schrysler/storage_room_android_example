package com.tillsimon.storageroom_example.activities.maphelper;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;

import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.readystatesoftware.mapviewballoons.BalloonItemizedOverlay;
import com.tillsimon.storageroom_example.activities.RestaurantDetailActivity;

public class RestaurantsOverlay extends BalloonItemizedOverlay<OverlayItem> {
	
	private ArrayList<OverlayItem> mOverlayItemsArrayList = new ArrayList<OverlayItem>();
	private Context mContext;

	public RestaurantsOverlay(Context context, Drawable defaultMarker, MapView mapView) {
		super(boundCenter(defaultMarker), mapView);
		this.mContext = context;
	}

	@Override
	protected boolean onBalloonTap(int index, OverlayItem item) {
	  Intent intent = new Intent(mContext, RestaurantDetailActivity.class);
	  intent.putExtra("clickedItemId", ((RestaurantOverlayItem)item).getId());
	  mContext.startActivity(intent);
	  return true;
	}
	@Override
	protected OverlayItem createItem(int i) {
		return mOverlayItemsArrayList.get(i);
	}
	
	@Override
	public int size() {
		return mOverlayItemsArrayList.size();
	}	
		
	@Override
	public void hideBalloon() {
		super.hideBalloon();
	}

	public void addOverlayItem(OverlayItem overlay) {
		mOverlayItemsArrayList.add(overlay);
		populate();
	}

}
