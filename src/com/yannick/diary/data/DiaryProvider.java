package com.yannick.diary.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
/**
 * 
 * @author Development Team
 *
 */
public class DiaryProvider extends ContentProvider {
	
	/** The DAO providing access to the SQLite database */
	private DAO mDAO;
    /**
     * UriMatcher used to determine which of the possible URL patterns is being used to address the ContentProvider
     * The URI matcher converts URL patterns to int values which are then used in switch statements
     */
    private static final UriMatcher sUriMatcher;
    /*
     * Constants used by the Uri matcher to choose an action based on the pattern
     * of the incoming URI
     */
    // The incoming URI matches the URI for a list of expenses
    private static final int DIARY = 1;

    // The incoming URI matches the URI for a single expense specified by ID
    private static final int DIARY_ID = 2;
    	
   
    // TODO 1 - Briefly examine the code in the static block below 
    static {
    	// Note that the static block is run only once during the application life-cycle
    	// Thus the setup is done once for all instances of the class
        /*
         * Creates and initializes the URI matcher
         */
        // Create a new instance
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        // Add the pattern matching the URI ending with expenses
        // Used to route the request to operations not specifiying an expense Id value
        sUriMatcher.addURI(Diary.AUTHORITY, "diary", DIARY);
        // Add the pattern matching the URI ending with expenses plus an integer
        // representing the ID of an expense
        sUriMatcher.addURI(Diary.AUTHORITY, "diary/#", DIARY_ID);      

    }
        
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
        int count;

        // Does the delete based on the incoming URI pattern.
        switch (sUriMatcher.match(uri)) {

            // If the incoming pattern matches the general pattern for expenses, does a delete
            // based on the incoming "where" columns and arguments.
            case DIARY:
            	count = mDAO.deleteDiary(where, whereArgs);
            	break;
                // If the incoming URI matches a single expense ID, does the delete based on the
                // incoming data, but modifies the where clause to restrict it to the
                // particular expense ID.
            case DIARY_ID:
            	int diaryId = Integer.parseInt(uri.getPathSegments().get(Diary.DiaryItem.DIARY_ID_PATH_POSITION));
            	count = mDAO.deleteDiaryById(diaryId);
                break;

            // If the incoming pattern is invalid, throws an exception.
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        /*Gets a handle to the content resolver object for the current context, and notifies it
         * that the incoming URI changed. The object passes this along to the resolver framework,
         * and observers that have registered themselves for the provider are notified.
         */
        getContext().getContentResolver().notifyChange(uri, null);

        // Returns the number of rows deleted.
        return count;

	}

	@Override
	public String getType(Uri uri) {
        /**
         * Chooses the MIME type based on the incoming URI pattern
         */
		// TODO MAtch on the url
        switch (sUriMatcher.match(uri)) {
            // If the pattern is for expenses returns the returns the expense ID content type.
            case DIARY:
            	// TODO Return the content type
                return Diary.DiaryItem.CONTENT_TYPE;
            // If the pattern is for expense IDs, returns the expense ID content type.
            case DIARY_ID:
            	// TODO Return the content type
                return Diary.DiaryItem.CONTENT_ITEM_TYPE;
            // If the URI pattern doesn't match any permitted patterns, throws an exception.
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
	       // Validates the incoming URI. Only the full provider URI is allowed for inserts.
        if (sUriMatcher.match(uri) != DIARY) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        long rowId = mDAO.insertDiary(values);

        // If the insert succeeded, the row ID exists.
        if (rowId > 0) {
            // Creates a URI with the expense ID pattern and the new row ID appended to it.
            Uri diaryUri = ContentUris.withAppendedId(Diary.DiaryItem.CONTENT_ID_URI_BASE, rowId);

            // Notifies observers registered against this provider that the data changed.
            getContext().getContentResolver().notifyChange(diaryUri, null);
            return diaryUri;
        }

        // If the insert didn't succeed, then the rowID is <= 0. Throws an exception.
        throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	
	public boolean onCreate() {
		// TODO 2 Create an instance of the DAO
		mDAO = new DAO(getContext());
		// Return true to indicate the ContentProvider is loaded OK
		return true;
	}


	@Override
	/**
	 * Queries the database and returns a cursor containing the results.
	 * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
	 * @param projection The columns to return
	 * @param selection A filter declaring which rows to return
	 * @param selectionArgs You may include ?s in selection, which will be replaced by the values from selectionArgs
	 * @param sortOrder How to order the rows, formatted as an SQL ORDER BY
	 */
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		   Cursor cursor = null;

	       /**
	        * Choose the projection and adjust the "where" clause based on URI pattern-matching.
	        */
	       switch (sUriMatcher.match(uri)) {
	           
	           case DIARY:
	        	   // If the incoming URI is for expenses, call the queryExpenses method on the DAO	        	   
	        	   // TODO call queryExpenses. The arguments you need were all passed into this method
	               cursor = mDAO.queryDiary(projection, selection, selectionArgs, sortOrder);
	               break;
	           case DIARY_ID:
	               // If the incoming URI is for a single expense identified by its ID, call the queryExpenseById() method on the DAO
	        	   // TODO Retrieve the ID of the expense to search for from the uri passed into this method
	        	   int expenseId = Integer.parseInt(uri.getPathSegments().get(Diary.DiaryItem.DIARY_ID_PATH_POSITION));
	        	   // TODO Call queryExpenseById on the DAO to perform the query
	        	   cursor = mDAO.queryDiaryById(expenseId, projection);
	               break;

	           default:
	               // If the URI doesn't match any of the known patterns, throw an exception.
	               throw new IllegalArgumentException("Unknown URI " + uri);
	       }
	       
	       // Tells the Cursor what URI to watch, so it knows when its source data changes
	       // TODO Configure notification on the cursor
	       cursor.setNotificationUri(getContext().getContentResolver(), uri);
	       // TODO return the newly retrieved cursor
	       return cursor;

	}

	@Override
	public int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {
        int count;
        // Does the update based on the incoming URI pattern
        switch (sUriMatcher.match(uri)) {

            // If the incoming URI matches the general expenses pattern, does the update based on
            // the incoming data.
            case DIARY:

                // Does the update and returns the number of rows updated.
                count = mDAO.updateDiary(values, where, whereArgs);
                break;

            // If the incoming URI matches a single expense ID, does the update based on the incoming
            // data, but modifies the where clause to restrict it to the particular expense ID.
            case DIARY_ID:
                // From the incoming URI, get the expense ID
                int expenseId = Integer.parseInt(uri.getPathSegments().get(Diary.DiaryItem.DIARY_ID_PATH_POSITION));
                count = mDAO.updateDiaryById(expenseId, values);
                break;
            // If the incoming pattern is invalid, throws an exception.
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        /*Gets a handle to the content resolver object for the current context, and notifies it
         * that the incoming URI changed. The object passes this along to the resolver framework,
         * and observers that have registered themselves for the provider are notified.
         */
        getContext().getContentResolver().notifyChange(uri, null);

        // Returns the number of rows updated.
        return count;
	}
    /**
     * A test package can call this to get a handle to the database underlying Provider,
     * so it can insert test data into the database. 
     *
     * @return a handle to the database helper object for the provider's data.
     */
    public DatabaseHelper getDbHelperForTest() {
        return mDAO.getDbHelperForTest();
    }
}
