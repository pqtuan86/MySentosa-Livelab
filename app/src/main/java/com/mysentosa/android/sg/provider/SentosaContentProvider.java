package com.mysentosa.android.sg.provider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure;
import com.mysentosa.android.sg.utils.LogHelper;

public class SentosaContentProvider extends ContentProvider implements
SentosaDatabaseStructure {
	// SentosaDatabaseStructure contains all the table columns and constant
	// declarations
	private DatabaseHelper mDbHelper;
	public static final String DATABASE_NAME = "sentosadb.db";

	// related to db versioning
	private static final int DATABASE_VERSION = 14;
	public static final String DB_PREFS = "DB_PREFS",
			DB_VERSION = "DB_VERSION";

	private static final UriMatcher sUriMatcher;

	public static boolean IS_PRELOAD_DATABASE_CREATION = false;

//		 private class DatabaseHelper extends SQLiteOpenHelper { // IS_PRELOAD_DATABASE_CREATION
	private static class DatabaseHelper extends SQLiteOpenHelper {
		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, 1);
		}

		@Override
		public void onOpen(SQLiteDatabase db) {
			super.onOpen(db);
			// Enable foreign key constraints
			db.execSQL("PRAGMA foreign_keys=ON;");
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			if (IS_PRELOAD_DATABASE_CREATION) {
				db.execSQL(TABLE_NODES_CREATE);
				db.execSQL(TABLE_EDGES_CREATE);
				db.execSQL(TABLE_EDGE_OVERLAYS_CREATE);
				db.execSQL(TABLE_NODE_DETAILS_CREATE);
				db.execSQL(TABLE_NODES_TEMP_CREATE);
				db.execSQL(TABLE_EDGES_TEMP_CREATE);
				db.execSQL(TABLE_EDGE_OVERLAYS_TEMP_CREATE);
				db.execSQL(TABLE_NODE_DETAILS_TEMP_CREATE);
				db.execSQL(TABLE_THINGS_TO_DO_CREATE);
				db.execSQL(TABLE_EVENTS_CREATE);
				db.execSQL(TABLE_PROMOTIONS_CREATE);
				db.execSQL(TABLE_CART_TEMP_CREATE);
//								 (new PreloadedDatabaseBuilder(getContext(), db)).execute();
//				 IS_PRELOAD_DATABASE_CREATION
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if (IS_PRELOAD_DATABASE_CREATION) {
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_NODES);
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_EDGES);
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_EDGE_OVERLAYS);
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_THINGS_TO_DO);
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_NODE_DETAILS);
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROMOTIONS);
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_EDGES_TEMP);
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_NODE_DETAILS_TEMP);
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_NODES_TEMP);
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_EDGE_OVERLAYS_TEMP);
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
				onCreate(db);
			}
		}

	}

	@Override
	public boolean onCreate() {
		mDbHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		int match = sUriMatcher.match(uri);
		int count = db.delete(getTables(match), selection, selectionArgs);
		if (count > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return count;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {		
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		int match = sUriMatcher.match(uri);	
		long rowId = db.insert(getTables(match), null, values);	
		if (rowId >= 0) {
			Uri insertUri = ContentUris.withAppendedId(uri, rowId);
			getContext().getContentResolver().notifyChange(insertUri, null);
			return insertUri;
		}
		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String rawQuery,
			String[] selectionArgs, String sortOrder) {
		int match = sUriMatcher.match(uri);		
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = null;

		if (match == ContentURIs.SENTOSA_CODE
				|| match == ContentURIs.EVENTS_CODE
				|| match == ContentURIs.PROMOTIONS_CODE
				|| sortOrder.equals("MANUAL")) { //The "MANUAL" option is a small hack to enable us to put in a manual query with regular URIs
			LogHelper.d("TAG", "Raw query:"+rawQuery);
			if (selectionArgs != null) {
				for (int i = 0; i < selectionArgs.length; i++) {
					LogHelper.d("TAG", "Raw query args :" + i + " : "+ selectionArgs[i]);
				}
			} else {
				LogHelper.d("TAG", "select args was null");
			}
			cursor = db.rawQuery(rawQuery, selectionArgs);
			LogHelper.d("TAG", "Total result: " + cursor.getCount());
			cursor.setNotificationUri(getContext().getContentResolver(), uri);
		} else {
			SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
			qb.setTables(getTables(match));
			cursor = qb.query(db, projection, rawQuery, selectionArgs, null,
					null, sortOrder);
			cursor.setNotificationUri(getContext().getContentResolver(), uri);
		}

		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		int match = sUriMatcher.match(uri);		
		long rowCount = db.update(getTables(match), values, selection,
				selectionArgs);		
		if (rowCount > 0) {
			Uri insertUri = ContentUris.withAppendedId(uri, rowCount);
			getContext().getContentResolver().notifyChange(insertUri, null);
			return 1;
		}
		return 0;
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] valuesArray) {
		int count = 0;
		boolean update_success;
		Cursor cursor = null;
		int match = sUriMatcher.match(uri);
		String table = getTables(match);
		String id_col_name = _ID;
		String location_col_name = null;
		String select = null;

		SQLiteDatabase db = mDbHelper.getWritableDatabase();

		db.beginTransaction();
		try {
			if (match == ContentURIs.PROMOTIONS_CODE
					|| match == ContentURIs.EVENTS_CODE) {
				for (ContentValues values : valuesArray) {
					select = String.format(Locale.US, "%s = %d AND %s = %d", id_col_name,
							values.getAsLong(id_col_name), location_col_name,
							values.getAsLong(location_col_name));
					cursor = db.query(table, new String[] { id_col_name,
							location_col_name }, select, null, null,
							null, null);

					if (cursor.getCount() == 0)
						update_success = (db.insert(table, null, values) >= 0);
					else
						update_success = (db.update(table, values, select, null) > 0);

					if (update_success)
						count++;
					cursor.close();
				}
			} 
			else {
				if(match == ContentURIs.THINGS_TO_DO_CODE) 
					db.delete(table,null,null);
				for (ContentValues values : valuesArray) {
					update_success = (db.insert(table, null, values) >= 0);
					if (update_success) {
						count++;
						LogHelper.d("test", "update success: "+count);
					}
				}
			}
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			count = 0;
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
		if (count > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return count;
	}

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(AUTHORITY, ContentURIs.SENTOSA_PATH,
				ContentURIs.SENTOSA_CODE);
		sUriMatcher.addURI(AUTHORITY, ContentURIs.EVENTS_PATH,
				ContentURIs.EVENTS_CODE);
		sUriMatcher.addURI(AUTHORITY, ContentURIs.PROMOTIONS_PATH,
				ContentURIs.PROMOTIONS_CODE);
		sUriMatcher.addURI(AUTHORITY, ContentURIs.NODE_DETAILS_PATH,
				ContentURIs.NODE_DETAILS_CODE);
		sUriMatcher.addURI(AUTHORITY, ContentURIs.THINGS_TO_DO_PATH,
				ContentURIs.THINGS_TO_DO_CODE);

		sUriMatcher.addURI(AUTHORITY, ContentURIs.NODE_DETAILS_TEMP_PATH,
				ContentURIs.NODE_DETAILS_TEMP_CODE);
		sUriMatcher.addURI(AUTHORITY, ContentURIs.NODES_TEMP_PATH,
				ContentURIs.NODES_TEMP_CODE);
		sUriMatcher.addURI(AUTHORITY, ContentURIs.EDGES_TEMP_PATH,
				ContentURIs.EDGES_TEMP_CODE);
		sUriMatcher.addURI(AUTHORITY, ContentURIs.EDGE_OVERLAYS_TEMP_PATH,
				ContentURIs.EDGE_OVERLAYS_TEMP_CODE);		
		sUriMatcher.addURI(AUTHORITY, ContentURIs.CART_PATH,
				ContentURIs.CART_CODE);

	}

	static private String getTables(int match) {
		switch (match) {
		case ContentURIs.EVENTS_CODE:
			return TABLE_EVENTS;
		case ContentURIs.THINGS_TO_DO_CODE:
			return TABLE_THINGS_TO_DO;
		case ContentURIs.PROMOTIONS_CODE:
			return TABLE_PROMOTIONS;
		case ContentURIs.NODE_DETAILS_CODE:
			return TABLE_NODE_DETAILS;
		case ContentURIs.NODES_TEMP_CODE:
			return TABLE_NODES_TEMP;
		case ContentURIs.NODE_DETAILS_TEMP_CODE:
			return TABLE_NODE_DETAILS_TEMP;
		case ContentURIs.EDGES_TEMP_CODE:
			return TABLE_EDGES_TEMP;
		case ContentURIs.EDGE_OVERLAYS_TEMP_CODE:
			return TABLE_EDGE_OVERLAYS_TEMP;
		case ContentURIs.CART_CODE:
			return TABLE_CART;
		default:
			throw new IllegalArgumentException("Unhandled match: " + match);
		}
	}

	// copy the pre-stored database from assets into sentosadb.db
	private static void copyDataBase(Context c, String outFileName)
			throws IOException {
		LogHelper.d("test", "test database writing the new database");
		InputStream myInput = c.getAssets().open(DATABASE_NAME);
		OutputStream myOutput = new FileOutputStream(outFileName);

		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}

		myOutput.flush();
		myOutput.close();
		myInput.close();
	}

	// delete db if the DATABASE_VERSION is increased or if it does not exist
	public static void setupDatabase(Context c) {
		if (IS_PRELOAD_DATABASE_CREATION)
			return;

		SharedPreferences mPrefs;
		mPrefs = c.getSharedPreferences(DB_PREFS, Context.MODE_PRIVATE);

		// first check
		int DB_VERSION_NUMBER = mPrefs.getInt(DB_VERSION, -1);

		// second check
		String databasePath = c.getDatabasePath(DATABASE_NAME).getPath();
		File f = new File(databasePath);
		boolean isDBExists = f.exists();

		LogHelper.d("db log", "test database file exists: " + isDBExists
				+ " db version stored: " + DB_VERSION_NUMBER);
		if (DB_VERSION_NUMBER < DATABASE_VERSION || !isDBExists) {
			// delete db if it exists
			if (isDBExists) {
				LogHelper.d("db log", "test database deleting from splash");
				f.delete();
			}

			// write the database over from assets
			try {
				// we use the getreadabledatabase method below as for some
				// reason, we cannot directly write to database directory

				DatabaseHelper dbHelper = new DatabaseHelper(c);
//								 DatabaseHelper dbHelper = null; // IS_PRELOAD_DATABASE_CREATION
				SQLiteDatabase db = dbHelper.getReadableDatabase();
				db.close();
				copyDataBase(c, databasePath);
			} catch (IOException e) {
				e.printStackTrace();
			}

			// commit database version to shared prefs
			final SharedPreferences.Editor edit = mPrefs.edit();
			edit.clear();
			edit.putInt(DB_VERSION, DATABASE_VERSION);
			edit.commit();
		}
	}

	public boolean updateTableNodes() {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NODES);
			db.execSQL("ALTER TABLE " + TABLE_NODES_TEMP + " RENAME TO "
					+ TABLE_NODES);
			db.execSQL(TABLE_NODES_TEMP_CREATE);
			db.setTransactionSuccessful();
			return true;
		} catch (SQLException e) {
			return false;
		} finally {
			db.endTransaction();
		}
	}

	public boolean updateTableEdges() {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_EDGES);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_EDGE_OVERLAYS);
			db.execSQL("ALTER TABLE " + TABLE_EDGES_TEMP + " RENAME TO "
					+ TABLE_EDGES);
			db.execSQL("ALTER TABLE " + TABLE_EDGE_OVERLAYS_TEMP
					+ " RENAME TO " + TABLE_EDGE_OVERLAYS);
			db.execSQL(TABLE_EDGES_TEMP_CREATE);
			db.execSQL(TABLE_EDGE_OVERLAYS_TEMP_CREATE);
			db.setTransactionSuccessful();
			return true;
		} catch (SQLException e) {
			return false;
		} finally {
			db.endTransaction();
		}
	}

	public boolean updateTableDetails() {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			db.execSQL("UPDATE "+TABLE_NODE_DETAILS_TEMP+" SET "+NodeDetailsData.IS_BOOKMARKED_COL+" = (SELECT "+TABLE_NODE_DETAILS+"."+NodeDetailsData.IS_BOOKMARKED_COL+
					" FROM "+TABLE_NODE_DETAILS+" WHERE "+TABLE_NODE_DETAILS+"."+NodeDetailsData.ID_COL+" = "+TABLE_NODE_DETAILS_TEMP+"."+NodeDetailsData.ID_COL+
					") WHERE EXISTS (select * FROM "+TABLE_NODE_DETAILS+" WHERE "+TABLE_NODE_DETAILS+"."+NodeDetailsData.ID_COL+" = "+TABLE_NODE_DETAILS_TEMP+"."+NodeDetailsData.ID_COL+")");
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NODE_DETAILS);
			db.execSQL("ALTER TABLE " + TABLE_NODE_DETAILS_TEMP + " RENAME TO "
					+ TABLE_NODE_DETAILS);
			db.execSQL(TABLE_NODE_DETAILS_TEMP_CREATE);
			db.setTransactionSuccessful();
			return true;
		} catch (SQLException e) {
			return false;
		} finally {
			db.endTransaction();
		}
	}

	public SQLiteDatabase getDBHandle() {
		return mDbHelper.getWritableDatabase();
	}
	
	// Copy to sdcard for debug use
		public static void copyDatabase(Context c) {
			String databasePath = c.getDatabasePath(DATABASE_NAME).getPath();
			File f = new File(databasePath);
			OutputStream myOutput = null;
			InputStream myInput = null;
			// LogHelper.d("testing", " testing db path " + databasePath);
			// LogHelper.d("testing", " testing db exist " + f.exists());

			if (f.exists()) {
				try {

					File directory = new File("/mnt/sdcard/SENTOSA_DEBUG");
					if (!directory.exists())
						directory.mkdir();

					myOutput = new FileOutputStream(directory.getAbsolutePath()
							+ "/" + DATABASE_NAME);
					myInput = new FileInputStream(databasePath);

					byte[] buffer = new byte[1024];
					int length;
					while ((length = myInput.read(buffer)) > 0) {
						myOutput.write(buffer, 0, length);
					}

					myOutput.flush();
				} catch (Exception e) {
				} finally {
					try {
						if (myOutput != null) {
							myOutput.close();
							myOutput = null;
						}
						if (myInput != null) {
							myInput.close();
							myInput = null;
						}
					} catch (Exception e) {
					}
				}
			}
		}

}

