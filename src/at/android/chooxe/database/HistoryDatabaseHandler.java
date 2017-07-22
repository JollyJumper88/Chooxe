package at.android.chooxe.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HistoryDatabaseHandler extends SQLiteOpenHelper {

	// Database Version
	private static final int DATABASE_VERSION = 1;

	// Database Name
	private static final String DATABASE_NAME = "historymanager.db";

	// Table History
	private static final String TABLE_HISTORY = "history";
	private static final String KEY_ID = "id";
	private static final String KEY_NAME = "name";
	private static final String KEY_URL = "url";
	private static final String KEY_DATETIME = "datetime";

	private boolean mDebugDatabase = false;

	public HistoryDatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// create database with columns
		// ID, NAME, URL, DATETIME

		String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_HISTORY + "("
				+ KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
				+ KEY_URL + " TEXT," + KEY_DATETIME + " TEXT" + ")";
		db.execSQL(CREATE_CONTACTS_TABLE);

		if (mDebugDatabase)
			System.out.println("create new database");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);

		// Create tables again
		onCreate(db);

		if (mDebugDatabase)
			System.out.println("updating the database and create new one");
	}

	public void addHistoryItem(HistoryItem historyItem) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_NAME, historyItem.getName());
		values.put(KEY_URL, historyItem.getUrl());
		values.put(KEY_DATETIME, historyItem.getDateTime());

		db.insert(TABLE_HISTORY, null, values);
		db.close();
	}

	public int getHistoryCount() {
		String countQuery = "SELECT  * FROM " + TABLE_HISTORY;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		cursor.close();

		return cursor.getCount();
	}

	// public int updateContact(Contact contact) {
	// SQLiteDatabase db = this.getWritableDatabase();
	//
	// ContentValues values = new ContentValues();
	// values.put(KEY_NAME, contact.getName());
	// values.put(KEY_PH_NO, contact.getPhoneNumber());
	//
	// // updating row
	// return db.update(TABLE_CONTACTS, values, KEY_ID + " = ?",
	// new String[] { String.valueOf(contact.getID()) });
	// }

	public List<HistoryItem> getAllHistoryItems() {
		List<HistoryItem> historyItemList = new ArrayList<HistoryItem>();

		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_HISTORY;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// Ascending sort order
		// cursor.moveToFirst();
		// while (!cursor.isAfterLast()) {
		// HistoryItem historyItem = new HistoryItem(cursor.getString(1),
		// cursor.getString(2), cursor.getString(3));
		// historyItemList.add(historyItem);
		// cursor.moveToNext();
		// }

		// Descending Sort order
		cursor.moveToLast();
		while (!cursor.isBeforeFirst()) {
			HistoryItem historyItem = new HistoryItem(cursor.getString(1),
					cursor.getString(2), cursor.getString(3));
			historyItemList.add(historyItem);
			cursor.moveToPrevious();
		}

		// Make sure to close the cursor
		cursor.close();
		db.close();

		return historyItemList;
	}

	public void clearAll() {
		SQLiteDatabase db = this.getWritableDatabase();

		db.delete(TABLE_HISTORY, null, null);

		db.close();
	}
	// public void deleteContact(Contact contact) {
	// SQLiteDatabase db = this.getWritableDatabase();
	// db.delete(TABLE_HISTORY, KEY_ID + " = ?",
	// new String[] { String.valueOf(contact.getID()) });
	// db.close();
	// }

}
