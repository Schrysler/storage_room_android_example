package com.tillsimon.storageroom_example.model.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RestaurantsDatabaseHelper extends SQLiteOpenHelper {	
	
	private static final String DB_NAME = "restaurants.db";
	private static final int DB_VERSION = 1;
		
	public RestaurantsDatabaseHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(RestaurantsTableInterface.SQL_CREATE);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldDb, int newDb) {
		db.execSQL("DROP TABLE IF EXISTS " + RestaurantsTableInterface.TABLE_NAME);
		onCreate(db);
	}

}
