package com.yannick.diary.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "expenses.db";	// Should this be qualified?

	private static final int DATABASE_VERSION = 1;

    private static final String EXPENSES_TABLE_CREATE =
                "CREATE TABLE " + Diary.DiaryItem.TABLE_NAME + " (" +
                Diary.DiaryItem.COLUMN_NAME_ID + " INTEGER PRIMARY KEY," + 
                Diary.DiaryItem.COLUMN_NAME_DESCRIPTION + " TEXT, " +
                Diary.DiaryItem.COLUMN_NAME_AMOUNT + " REAL, " + 
                Diary.DiaryItem.COLUMN_NAME_DIARY_DATE + " REAL " +
                ");";
    
    public DatabaseHelper(Context context) {
    	// If the 2nd parameter is null then the database is held in memory -- this form creates a file backed database
    	super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}
    
    /**
     * Alternative constructor for test mode
     * @param context
     * @param testMode state of flag is irrelevant. The presence of the 2nd argument causes the in-memory db to be created
     */
    public DatabaseHelper(Context context, boolean testMode) {
    	// If the 2nd parameter is null then the database is held in memory -- this form creates an in memory database
    	super(context, null, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}
    
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		// NOTE: the db passed in here seems to be linked to the file system
		// The exec does effect the database.
		db.execSQL(EXPENSES_TABLE_CREATE);
	}

	/**
	 * Not sure what to do with this. Could ignore for the course...
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

}
