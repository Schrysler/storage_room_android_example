package com.tillsimon.storageroom_example.model.database;

public interface RestaurantsTableInterface {
	
	public static final String TABLE_NAME = "restaurants";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_TEXT = "text";
	public static final String COLUMN_ADDRESS = "address";
	public static final String COLUMN_WEBSITE = "website";
	public static final String COLUMN_LAST_VISIT = "lastvisit";
	public static final String COLUMN_LOCATION_LAT = "locationlat";
	public static final String COLUMN_LOCATION_LNG = "locationlng";
	public static final String COLUMN_PRICE_RANGE = "pricerange";
	public static final String COLUMN_RATING = "rating";
	public static final String COLUMN_VEGETARIAN = "vegetarian";
	public static final String COLUMN_IMAGE_URL = "imageurl";
	public static final String COLUMN_IMAGE_PREVIEW_URL = "imagepreviewurl";
	public static final String COLUMN_ENTRY_CREATED = "entrycreated";
	
	public static final String SQL_CREATE =
			"CREATE TABLE " + TABLE_NAME + " (" +
			COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			COLUMN_NAME + " TEXT NOT NULL, " +
			COLUMN_TEXT + " TEXT NOT NULL, " +
			COLUMN_ADDRESS + " TEXT NOT NULL, " +
			COLUMN_WEBSITE + " TEXT NOT NULL, " +
			COLUMN_LAST_VISIT + " TEXT NOT NULL, " +
			COLUMN_LOCATION_LAT + " DOUBLE, " +
			COLUMN_LOCATION_LNG + " DOUBLE, " +
			COLUMN_PRICE_RANGE + " INTEGER, " +
			COLUMN_RATING + "  INTEGER, " +
			COLUMN_VEGETARIAN + " BOOLEAN, " +
			COLUMN_IMAGE_URL + " TEXT NOT NULL, " +
			COLUMN_IMAGE_PREVIEW_URL+ " TEXT NOT NULL, " +
			COLUMN_ENTRY_CREATED + " TEXT NOT NULL" +
			");";
	
	public static final String INSERT_STATEMENT =
		"INSERT INTO " + TABLE_NAME + " (" +
		COLUMN_NAME +", " +
		COLUMN_TEXT +", " +
		COLUMN_ADDRESS +", " +
		COLUMN_WEBSITE +", " +
		COLUMN_LAST_VISIT +", " +
		COLUMN_LOCATION_LAT +", " +
		COLUMN_LOCATION_LNG +", " +
		COLUMN_PRICE_RANGE +", " +
		COLUMN_RATING +", " +
		COLUMN_VEGETARIAN +", " +
		COLUMN_IMAGE_URL +", " +
		COLUMN_IMAGE_PREVIEW_URL +", " +
		COLUMN_ENTRY_CREATED +
		") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)"
		;
	
}
