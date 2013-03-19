package com.yannick.diary.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

public final class Diary implements java.io.Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String AUTHORITY = "com.yannick.diary.Diary";	// How to explain?
	
	/** Key properties for the Expense object. Note they are public to avoid the overhead of virtual method invocation
	 * (Android docs say this is up to 7 times faster!)
	 */
	public String description;
	public String title;
	public long diaryDate;	

	/**
	 * Create an Expenses instance from a ContentValues object
	 * @param values
	 */
	public Diary(ContentValues values){
		description = values.getAsString(Diary.DiaryItem.COLUMN_NAME_DESCRIPTION );
		title = values.getAsString(Diary.DiaryItem.COLUMN_NAME_TITLE);
		diaryDate = values.getAsLong(Diary.DiaryItem.COLUMN_NAME_DIARY_DATE);
	}
	
	public Diary(String description, String title, long date ){
		this.description = description;
		this.title = title;
		diaryDate = date;

	}	
	
    /*
     * Returns a ContentValues instance (a map) for this expenseInfo instance. This is useful for
     * inserting a expenseInfo into a database.
     */
    public ContentValues getContentValues() {
        // Gets a new ContentValues object
        ContentValues v = new ContentValues();

        // Adds map entries for the user-controlled fields in the map
        v.put(Diary.DiaryItem.COLUMN_NAME_DESCRIPTION, description);
        v.put(Diary.DiaryItem.COLUMN_NAME_TITLE, title);
        v.put(Diary.DiaryItem.COLUMN_NAME_DIARY_DATE, diaryDate);
        return v;

    }    	
	
	/**
	 * Constant definition to define the mapping of an Expense to the underlying database
	 * Also provides constants to help define the Content Provider
	 * @author Development Team
	 *
	 */
	public static final class DiaryItem implements BaseColumns {
		
        // This class cannot be instantiated
        private DiaryItem() {}

        /**
         * The table name offered by this provider
         */
        public static final String TABLE_NAME = "diary";

        /*
         * URI definitions
         */

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        /**
         * Path parts for the URIs
         */

        /**
         * Path part for the expenses URI
         */
        private static final String PATH_DIARY = "/diary";

        /**
         * Path part for the expense ID URI
         */
        private static final String PATH_DIARY_ID = "/diary/";

        /**
         * 0-relative position of a expense item ID segment in the path part of a expense item ID URI
         */
        public static final int DIARY_ID_PATH_POSITION = 1;


        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =  Uri.parse(SCHEME + AUTHORITY + PATH_DIARY);
        
        /**
         * The content URI base for a single expense item. Callers must
         * append a numeric expense id to this Uri to retrieve an expense item
         */
        public static final Uri CONTENT_ID_URI_BASE
            = Uri.parse(SCHEME + AUTHORITY + PATH_DIARY_ID);        
        
        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = Diary.DiaryItem.COLUMN_NAME_DIARY_DATE + " ASC";        
        
        
        /*
         * MIME type definitions
         */

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of expenses.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.yannick.diary.diary";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single expense item
         * 
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.yannick.diary.diary";
        
        /*
         * Column definitions
         */

        /**
         * Column name for the description of the expense item
         * <P>Type: TEXT</P>
         */
        public static final String COLUMN_NAME_DESCRIPTION = "description";

        /**
         * Column name of the expense item amount
         * <P>Type: REAL</P>
         */
        public static final String COLUMN_NAME_TITLE = "title";
        
        /**
         * Column name for the creation timestamp
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String COLUMN_NAME_DIARY_DATE = "incurred";       
        
        
        public static final String COLUMN_NAME_ID = BaseColumns._ID;
        
        /** Projection holding all the columns required to populate and Expense item */
        public static final String[] FULL_PROJECTION = {
                COLUMN_NAME_DESCRIPTION,
                COLUMN_NAME_TITLE,
                COLUMN_NAME_DIARY_DATE
            };        
        
        public static final String[] LIST_PROJECTION =
            new String[] {
                Diary.DiaryItem._ID,
                Diary.DiaryItem.COLUMN_NAME_DESCRIPTION,
                Diary.DiaryItem.COLUMN_NAME_TITLE
        };            

	}

	public static final class Helper {
		/**
		 * Converts a cursor to an array of Expense 
		 * Note that this method is "mean and lean" with little error checking.
		 * It assumes that the projection used is ExpenseItem.FULL_PROJECTION
		 * @param cursor A cursor loaded with Expense data
		 * @return populated array of Expense
		 */
		public static final Diary[] getDiaryFromCursor(Cursor cursor){
			Diary[] diary = null;
			int rows = cursor.getCount();
			if(rows > 0){
				diary = new Diary[rows];
				int i=0;
				while(cursor.moveToNext()){
					diary[i++] = new Diary( cursor.getString(0), cursor.getString(1), cursor.getLong(2));
				}
			}
			return diary;
		}
		
		public static final JSONObject diaryToJSON(Diary e) throws JSONException{
			JSONObject jObj = new JSONObject();
			jObj.put("description", e.description);
			jObj.put("title", e.title);
			jObj.put("diaryDate", e.diaryDate);
			return jObj;
		}
		
		public static final JSONArray diaryArrayToJSON(Diary[] diary)  throws JSONException{
			JSONArray jArray = new JSONArray();
			System.out.println("Converting  " + diary.length + " diary to JSON");
			for(Diary e: diary){
				jArray.put(diaryToJSON(e));
			}
			return jArray;
		}	
		public static final StringBuilder diaryArrayToCsv(Diary[] diary){
			StringBuilder result= new StringBuilder();
			for(Diary e: diary){
				result.append(diaryToCsv(e));
			}
			return result;
		}		
		
		public static final StringBuilder  diaryToCsv(Diary e){
			StringBuilder builder = new StringBuilder();
			builder.append(e.description);
			builder.append(',');
			builder.append(e.title);
			builder.append(',');
			builder.append(e.diaryDate);
			builder.append('\n');
			return builder;
		}	
				
	}
	

	
}
