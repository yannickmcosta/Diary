package com.yannick.diary.data;

import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.text.TextUtils;
import android.util.Log;


/**
 * Data Access Object supporting the Expenses Application
 * @author Development Team
 *
 */
public class DAO {

	private static final String TAG="DAO";
	private DatabaseHelper mDbHelper;
    /**
     * A projection map used to select columns from the database
     */
    private static HashMap<String, String> sDiaryProjectionMap;   
    
    /*
     * Static initializer block used to configure the projection map (column mappings)
     */
    static {
        // Creates a new projection map instance. The map returns a column name
        // given a string. The two are usually equal.
        sDiaryProjectionMap = new HashMap<String, String>();

        // Maps the string "_ID" to the column name "_ID"
        sDiaryProjectionMap.put(Diary.DiaryItem.COLUMN_NAME_ID, Diary.DiaryItem.COLUMN_NAME_ID);     
        sDiaryProjectionMap.put(Diary.DiaryItem.COLUMN_NAME_DESCRIPTION, Diary.DiaryItem.COLUMN_NAME_DESCRIPTION);  
        sDiaryProjectionMap.put(Diary.DiaryItem.COLUMN_NAME_TITLE, Diary.DiaryItem.COLUMN_NAME_TITLE);  
        sDiaryProjectionMap.put(Diary.DiaryItem.COLUMN_NAME_DIARY_DATE, Diary.DiaryItem.COLUMN_NAME_DIARY_DATE);  
            	
    }
    
    /**
     * Normal constructor for the DAO
     * @param context
     */
	public DAO(Context context) {
	
		mDbHelper = new DatabaseHelper(context);
	}
	
	/**
	 * Special constructor to support unit testing
	 * @param context
	 * @param testMode if true then an in-memory DB will be created which avoids breaking the real db
	 * during testing
	 */
	protected DAO(Context context, boolean testMode) {
		
		if(testMode){
			mDbHelper = new DatabaseHelper(context, testMode);
		} else {
			mDbHelper = new DatabaseHelper(context);
		}
	}	
	
	/**
	 * Deletes expenses by the id of the expense
	 * @param id
	 * @return number of rows deleted - should be 1 for success
	 */
	public int deleteDiaryById(int id) {
        String finalWhere =
            Diary.DiaryItem.COLUMN_NAME_ID +                              // The ID column name
            " = " +                                          // test for equality
            id;
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
	    // Performs the delete.
	    int count = db.delete(
	        Diary.DiaryItem.TABLE_NAME,  // The database table name.
	        finalWhere,                // The final WHERE clause
	        null                  // The incoming where clause values.
	    );
		return count;
	}
	
	/**
	 * Deletes expenses as specified by the where clause composed of where and whereArgs
     * @param where where clause - if null, matches all rows
     * @param whereArgs values for ? place holders in the where clause
	 * @return number of rows deleted
	 */
    public int deleteDiary(String where, String[] whereArgs) {

        // Opens the database object in "write" mode.
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int count = db.delete(
                    Diary.DiaryItem.TABLE_NAME,  // The database table name
                    where,                     // The incoming where clause column names
                    whereArgs                  // The incoming where clause values
                );
        // Returns the number of rows deleted.
        return count;
    }	
	
    /**
     * Inserts an Expense item which is expressed in expenseValues 
     * 
     * @param expenseValues the values to populate the Expense item with. Any that are missing will
     * be defaulted
     * @return the id of the expense added. This is in fact the SQLite ROWID value
     */
	public long insertDiary(ContentValues diaryValues) {


     // If the incoming values map is not null, uses it for the new values.
     if (diaryValues != null) {
         diaryValues = new ContentValues(diaryValues);

     } else {
         // Otherwise, create a new value map
         diaryValues = new ContentValues();
     }

     // Gets the current system time in milliseconds
     Long now = Long.valueOf(System.currentTimeMillis());

     // If the values map doesn't contain the diary date, sets the value to the current
     // time.
     if (diaryValues.containsKey(Diary.DiaryItem.COLUMN_NAME_DIARY_DATE) == false) {
         diaryValues.put(Diary.DiaryItem.COLUMN_NAME_DIARY_DATE, now);
     }        

     // If the values map doesn't contain a description, sets the value to the default description.
     if (diaryValues.containsKey(Diary.DiaryItem.COLUMN_NAME_DESCRIPTION) == false) {
         Resources r = Resources.getSystem();
         diaryValues.put(Diary.DiaryItem.COLUMN_NAME_DESCRIPTION, r.getString(android.R.string.untitled));
     }

     // If the values map doesn't contain note text, sets the amount to 0.0f.
     if (diaryValues.containsKey(Diary.DiaryItem.COLUMN_NAME_TITLE) == false) {
    	 Resources r = Resources.getSystem();
         diaryValues.put(Diary.DiaryItem.COLUMN_NAME_TITLE, r.getString(android.R.string.untitled));
     }
     
     // Opens the database object in "write" mode.
     SQLiteDatabase db = mDbHelper.getWritableDatabase();

     // Performs the insert and returns the ID of the new expense item.
     long rowId = db.insert(
         Diary.DiaryItem.TABLE_NAME,        // The table to insert into.
         Diary.DiaryItem.COLUMN_NAME_DESCRIPTION,  // A hack, SQLite sets this column value to null
                                          // if values is empty.
         diaryValues                           // A map of column names, and the values to insert
                                          // into the columns.
     );
     //Log.d(TAG, "Insert ROWID= " + rowId);
     if(rowId < 0){
    	 throw new SQLException("Failed to insert diary entry.");
     }
     return rowId;
	}	
	
	
	/**
	 * Queries the database for a single expense item based on it's ID
	 * @param expenseId the ID of the expense item to return
	 * @param projection the columns to populate in the cursor
	 * @return a Cursor containing the data for the Expense item
	 */
	public Cursor queryDiaryById(int expenseId, String[] projection)
	{
	       // Constructs a new query builder and sets its table name
	       SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
	       qb.setTables(Diary.DiaryItem.TABLE_NAME);
	       qb.appendWhere(Diary.DiaryItem.COLUMN_NAME_ID +    // the name of the ID column
	    	        "=" +
	    	        expenseId);
	       return performQuery(qb, projection, null, null, null);		
	}
	
	/**
	 * General query method for expenses. Allows where clause and sort order to be 
	 * specified @see {@link android.database.sqlite.SQLiteQueryBuilder#query(SQLiteDatabase, String[], String, String[], String, String, String)}
	 * @param projection
	 * @param selection A filter declaring which rows to return
	 * @param selectionArgs You may include ?s in selection, which will be replaced by the values from selectionArgs
	 * @param sortOrder How to order the rows, formatted as an SQL ORDER BY
	 * @return
	 */
	public Cursor queryDiary(String[] projection, String selection, String[] selectionArgs, String sortOrder)
	{
	       // Constructs a new query builder and sets its table name
	       SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
	       qb.setTables(Diary.DiaryItem.TABLE_NAME);
	       
	       return performQuery(qb, projection, selection, selectionArgs, sortOrder);
	}
	
	/**
	 * Helper method to map public query methods onto the SQLQueryBuilder class methods
	 * @param qb
	 * @param projection
	 * @param selection
	 * @param selectionArgs
	 * @param sortOrder
	 * @return
	 */
	private Cursor performQuery(SQLiteQueryBuilder qb, String[] projection, String selection, String[] selectionArgs, String sortOrder)
	{

	       
	       // Opens the database object in "read" mode, since no writes need to be done.
	       SQLiteDatabase db = mDbHelper.getReadableDatabase();
	       
	       qb.setProjectionMap(sDiaryProjectionMap);
	       
	       String orderBy;
	       // If no sort order is specified, uses the default
	       if (TextUtils.isEmpty(sortOrder)) {
	           orderBy = Diary.DiaryItem.DEFAULT_SORT_ORDER;
	       } else {
	           // otherwise, uses the incoming sort order
	           orderBy = sortOrder;
	       }
	       
	       Log.i(TAG, "performQuery. Projection: " + projection);
	       /*
	        * Performs the query. If no problems occur trying to read the database, then a Cursor
	        * object is returned; otherwise, the cursor variable contains null. If no records were
	        * selected, then the Cursor object is empty, and Cursor.getCount() returns 0.
	        */
	       Cursor c = qb.query(
	           db,            // The database to query
	           projection,    // The columns to return from the query
	           selection,     // The columns for the where clause
	           selectionArgs, // The values for the where clause
	           null,          // don't group the rows
	           null,          // don't filter by row groups
	           orderBy        // The sort order
	       );
	       return c;
	}
	
	/**
	 * Updates the expense item specified by the expenseId parameter. If an ID parameter is also specified in the values
	 * parameter it will be ignored.
	 * @param expenseId
	 * @param values
	 * @return
	 */
    public int updateDiaryById( int expenseId, ContentValues values) {

        // Opens the database object in "write" mode.
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count;
        String where =
                        Diary.DiaryItem.COLUMN_NAME_ID +                              // The ID column name
                        " = " +  expenseId;                                        // test for equality

        // Does the update and returns the number of rows updated.
        count = db.update(
            Diary.DiaryItem.TABLE_NAME, // The database table name.
            values,                   // A map of column names and new values to use.
            where,               // The final WHERE clause to use
                                 // placeholders for whereArgs
            null                 // The where clause column values to select on, or
                                 // null if the values are in the where argument.
        );
 
        // Returns the number of rows updated.
        return count;
    }
    
    /**
     * Updates all of the expenses matching where and whereArgs with the values supplied. 
     * @param values
     * @param where where clause - if null, matches all rows
     * @param whereArgs values for ? place holders in the where clause
     * @return
     */
    public int updateDiary( ContentValues values, String where, String[] whereArgs) {

        // Opens the database object in "write" mode.
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count;

        // Does the update and returns the number of rows updated.
        count = db.update(
            Diary.DiaryItem.TABLE_NAME, // The database table name.
            values,                   // A map of column names and new values to use.
            where,                    // The where clause column names.
            whereArgs                 // The where clause column values to select on.
        );
        return count;
    }	

    /**
     * Used by test classes to directly access the database helper
     * @return a handle to the database helper object for the provider's data.
     */
    
    DatabaseHelper getDbHelperForTest() {
        return mDbHelper;
    }    
    
}
