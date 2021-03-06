package com.yannick.diary;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.yannick.diary.data.Diary;

/**
 * 
 * @author Development Team
 *
 */
public class SyncService extends IntentService {
	private static final String TAG="IntentService";
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "onCreate() called");

	
	}

	public SyncService()
	{
		super("SyncService");
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.i(TAG, "Processing started");
		
		/** Default action is to save to the Web service using JSON */
		boolean exportToCsvFile = false;
		
		// Plan was to have them use a Toast here but the Toast gets stuck.
		// The only solution is to Launch the Toast in a thread which is way to complex at this point.
		Log.i(TAG, "Service Started");
		
        // If there is no data associated with the Intent, sets the data to the default URI, which
        // accesses a list of expenses.
        if (intent.getData() == null) {
            intent.setData(Diary.DiaryItem.CONTENT_URI);
        } 		
        
        Bundle extras = intent.getExtras();
        if(null != extras){
        	// TODO Note that the flag indicating what action the service should take is retrieved here (nothing to change)
        	exportToCsvFile = extras.getBoolean(Constants.DIARY_EXTRA_EXPORT_TO_CSV);
        	
        }
		
		ContentResolver resolver =getContentResolver();
        Cursor cursor = resolver.query(
            	intent.getData(), // Use the default content URI for the provider.
                Diary.DiaryItem.FULL_PROJECTION, //PROJECTION,                       // Return the note ID and title for each note.
                null,                             // No where clause, return all records.
                null,                             // No where clause, therefore no where column values.
                Diary.DiaryItem.DEFAULT_SORT_ORDER  // Use the default sort order.
            );
        
        // TODO Call the helper method in Expense.Helper which converts the cursor to an array of expenses
        Diary[] expenses =  Diary.Helper.getDiaryFromCursor(cursor);
        
        
        if(exportToCsvFile){
    		// Work out where to write the file (from the Intent Extras)        
            String fileName = resolveFileNameForDiary(intent);
            // TODO pass the array of expenses to the writeExpensesToCsvFile helper method
            writeDiaryToCsvFile(fileName, expenses);
        }
        if(null != cursor){
        		// Close the cursor to release any resources
        	cursor.close();
        }
		Log.i(TAG, "Processing finished");	
	}
	
	
	private boolean writeDiaryToCsvFile(String fileName, Diary[] diary)
	{
		boolean success = false;
		PrintWriter pw = null;

		// TODO Get the state of the external storage (remove the null!)
		String state = Environment.getExternalStorageState(); 

		// TODO Test that the state is Environment.MEDIA_MOUNTED (accessible and writable) 
		if(Environment.MEDIA_MOUNTED.equals(state)){
			try {
				// TODO Get the File object representing the Public Downloads area
				File baseDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
				// TODO Create the file name we will use to write the CSV file
				// by appending the fileName to the baseDir
				File fullFileName = new File(baseDir, fileName);
				Log.d(TAG, "Outputting csv to: " + fullFileName.getAbsolutePath());
				// TODO Create a new PrintWriter to write the file (use the fullFileName )
				pw = new PrintWriter(fullFileName);
				// TODO Pass the expenses array to Expense.Helper.expenseArrayToCsv then
				// use the PrintWriter to write the data to the CSV file
				// Hint: you may need to convert the returned StringBuilder to a string
				pw.write(Diary.Helper.diaryArrayToCsv(diary).toString());
				
				success = true;
			} catch (IOException e) {
				e.printStackTrace();
				Log.e(TAG, "Failed to save diary in service. Details:", e);
			} finally {
				if(null != pw){
					// TODO close the PrintWriter
					pw.close();
				}
			}
		}
		return success;
	}	

	/**
	 * Helper method to set either a default file name or use one passed in with the intent
	 * @param intent
	 * @return the file name to write to
	 */
	private static String resolveFileNameForDiary(Intent intent) {
		String fileName = Constants.DEFAULT_CSV_FILENAME; // set a default file name
        Bundle extras = intent.getExtras();
        if(null != extras ){
	        String tmpFName = extras.getString(Constants.DIARY_EXTRA_FILENAME);
	        if(null != tmpFName){
	        	fileName = tmpFName;
	        }
        }
		return fileName;
	}	
}
