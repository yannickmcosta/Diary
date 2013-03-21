package com.yannick.diary;

import java.io.File;
import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.yannick.diary.data.Diary;
import com.yannick.diary.R;
import com.yannick.location.*;

public class DiaryEntryActivity extends Activity {

	private static final String TAG = "DiaryEntryActivity";
	public static final int CAM_REQUEST_CODE = 103;	
	
	private TextView mDateDisplay;
	private Button mSaveButton;
	private TextView mDescription;
	private TextView mTitle;
	@SuppressWarnings("unused")
	private TextView mDiaryDate;
	private ImageButton mReceiptButton;
	private ImageView mReceiptImg;
	
	private File mReceiptFile;
	
	/** Fields for managing the date control */
	private int mYear;
	private int mMonth;
	private int mDay;	
	
	/** Fields relating to GPS and Location */
	
	private TextView mLatitude;
	private TextView mLongitude;
		
	private long mDiaryId; // Diary item associated with this activity
	private Uri mUri;
	private Cursor mCursor;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.i(TAG, "onCreate");
		setContentView(R.layout.diary_form);
		
		// Set up references to the view items.
		mDateDisplay = (TextView) findViewById(R.id.expEdt_et_date);
		mSaveButton = (Button) findViewById(R.id.expEdt_bt_save);
		mDescription = (EditText) findViewById(R.id.expEdt_et_description);
		mTitle = (TextView) findViewById(R.id.expEdt_et_title);
		mDiaryDate = (TextView) findViewById(R.id.expEdt_et_date);
		mReceiptButton = (ImageButton) findViewById(R.id.expEdt_ib_receipt);
		mReceiptImg = (ImageView) findViewById(R.id.expEdt_im_receipt);
		
		mLatitude = (TextView) findViewById(R.id.locEdt_et_lat);
		mLongitude = (TextView) findViewById(R.id.locEdt_et_lon);
		

		// Call local helper method configureCursorForActivity()
		configureCursorForActivity();
		
		mSaveButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// Do something to save the data
				Intent resultIntent = new Intent();
				resultIntent.putExtra(Constants.DIARY_DESCRIPTION, mDescription.getText().toString());

				setResult(Activity.RESULT_OK, resultIntent); 
				finish();
			}
		});	  
		mDateDisplay.setOnClickListener(new DateClickListener());
		
		// TODO 21 Set up the handler for the receipt button (use the class
		// you just created
		mReceiptButton.setOnClickListener(new ReceiptButtonListener());
		
	}

	private void configureCursorForActivity() {
		// Get the launching intent from the Activity
		Intent launchingIntent = getIntent();
		
		// Get the If of the expense to be edited from the intent (-1 (Constants.DIARY_ITEM_UNDEFINED) 
		// indicates that we should create a new one)
		// Get the id of the expense to be edited from the intent
		// It's stored as a long in Extras with a name of Constants.DIARY_ID
		mDiaryId = launchingIntent.getLongExtra(Constants.DIARY_ID, Constants.DIARY_ITEM_UNDEFINED);
		if (Constants.DIARY_ITEM_UNDEFINED == mDiaryId) {
			// As the expenseItem was set to -1, create a new expense item 
			// Insert a new (empty) expense item into the database (Hint: the Uri for the ContentProvider is in the Intent)
			mUri = getContentResolver().insert(launchingIntent.getData(), null);
			// Get the id of the new expense from the uri returned
			mDiaryId = ContentUris.parseId(mUri);
		}

		// Build the full URI for the Content Provider associated with this activity
		// Append the id of the expense associated with this activity to the content provider url
		mUri = ContentUris.withAppendedId(launchingIntent.getData(), mDiaryId);
		
		
		// Load a cursor holding the Diary we are working on
		// Complete the code to create a new managed query for the expense on which we are working
		mCursor = managedQuery(mUri, // The URI that gets multiple expenses from
										// the provider.
				Diary.DiaryItem.FULL_PROJECTION, // Projection containing all Diary items
				null, // No "where" clause selection criteria.
				null, // No "where" clause selection values.
				null // Use the default sort order as there is only one item!
		);
		
	}
	
    @Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG, "onPause");
		/*
		 * Tests to see that the query operation didn't fail (see onCreate()).
		 * The Cursor object will exist, even if no records were returned,
		 * unless the query failed because of some exception or error.
		 */
		if (mCursor != null) {
			// Call the local helper method saveDiaryItem()
			saveDiaryItem();
		}	
	}

	private void saveDiaryItem() {
		
		ContentValues values = new ContentValues();

		// Adds map entries for the user-controlled fields in the map
		// Note that we are not checking the field content here but the provider
		// code does do the checking
		values.put(Diary.DiaryItem.COLUMN_NAME_DIARY_DATE, getDateInMillisFromDateFields());
		values.put(Diary.DiaryItem.COLUMN_NAME_TITLE, mTitle.getText()
				.toString());
		
		// Create a new element in the values object with a key of Diary.DiaryItemCOLUMN_NAME_DESCRIPTION
		// and the value from the mDescription view element (see the lines above for a template)
		values.put(Diary.DiaryItem.COLUMN_NAME_DESCRIPTION, mDescription
				.getText().toString());

		// Provider URL sent with the original intent
		// Call the update method on the ContentResolver to update the data with the
		// modified form elements (use null as the where clause parameters)
		// Important: use the provider URL stored in mUri as it represents the 
		// expense item associated with this Activity instance
		getContentResolver().update(mUri, values, null, null);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG, "onResume");
		// Call local helper method loadDiaryDataFromCursor() 
		loadDiaryDataFromCursor();
	}

	private void loadDiaryDataFromCursor() {
		/*
		 * mCursor is initialized, since onCreate() always precedes onResume for
		 * any running process. This tests that it's not null, since it should
		 * always contain data.
		 */		
		if (mCursor != null) {
			// Re-query in case something changed while paused
			// requery the cursor
			mCursor.requery();
			if (mCursor.moveToFirst()) {
				// populate the dialog fields

				// Note how the view elements are populated from the cursor content
				mDescription.setText(mCursor.getString(0));
				mTitle.setText(mCursor.getString(1));
				//mDiaryDate.setText(mCursor.getLong(2) + "");	// Coerce the long to a String with  + ""
				getDateFieldsFromMillis(mCursor.getLong(2));
				updateDateDisplay();
			}
		}
	}	
	
	
	@Override
	protected void onStart() {
		super.onStart();
		Log.i(TAG, "onStart");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.i(TAG, "onStop");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "onDestroy");
	}		
	
	/** 
	 * @see android.app.Activity#onCreateDialog(int)
	 * Method called by Android to allow the creation of a dialog
	 * Use this to launch the Date dialog
	 */
	@Override	
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case Constants.DATE_DIALOG_ID:
			return new DatePickerDialog(this, mDateSetListener, mYear, mMonth,
					mDay);
		}
		return null;
	}	
	
	/**
	 *  The callback received when the user "sets" the date in the dialog
	 */
	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			mYear = year;
			mMonth = monthOfYear;
			mDay = dayOfMonth;
			updateDateDisplay();
		}
	};	
	
	/** 
	 * Updates the mDateDisplay TextView with a formatted data based on the values of
	 * year, month and day member fields
	 */
	private void updateDateDisplay() {
		mDateDisplay.setText(new StringBuilder()
				// Month is 0 based so add 1
				.append(mMonth + 1).append("-").append(mDay).append("-")
				.append(mYear).append(" "));
	}
	
	/**
	 * Converts the year, month and day member fields to a time in milli-seconds
	 * @return
	 */
	private long getDateInMillisFromDateFields() {
		Calendar c= Calendar.getInstance();
		c.set(mYear, mMonth, mDay);
		long date = c.getTimeInMillis();
		return date;
	}	

	/**
	 * Populates the mYear, mMonth and mDay fields from the supplied time in millis
	 * @param time
	 */
	private void getDateFieldsFromMillis(long time) {
		final Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time);
		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);
	}		

	// add a click listener to the date entry field
	private class DateClickListener implements View.OnClickListener {
		public void onClick(View v) {
			showDialog(Constants.DATE_DIALOG_ID);
		}
	};	

	// TODO 1 Declare a class called ReceiptButtonListener which implements View.OnClickListener
	 class ReceiptButtonListener implements View.OnClickListener
	 {

		// TODO 2 Override the onClick method
		 public void onClick(View v) { {
			// TODO 3 Create an Intent to start the Camera app (MediaStore.ACTION_IMAGE_CAPTURE)
			//Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			Intent intent = new Intent(DiaryEntryActivity.this, com.yannick.camera.CameraEmulatorActivity.class);
				
			// Set up receiptFile to specify where the image should be stored
			// Need to pass in the ID of the expenseItem so we can
			// create the file name here.
			// TODO 4 Get the File object representing the External Public Pictures area
			File baseDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
				
			// TODO 5 append the "receipts" path to the directory
			// Hint: you are just creating a sub-directory called receipts
			File receiptsDir = new File(baseDir, "reciepts");
			
			// Create the directories if they do not yet exist
			if(!receiptsDir.exists()){
				// TODO 7 Create the directories if they do not yet exist
				// Embed the creation in the if test so a failure gets logged
				if(!receiptsDir.mkdirs()){
					Log.e(TAG, "Failed to create directory to save receipts");
					return;  // There is not point continuing if we can't save the file
				}					
			}
			// TODO 8 add the Id of the current expense into the file name 
			// Hint: mDiaryId
			mReceiptFile = new File(receiptsDir, "receipt" + mDiaryId + ".jpg");
			Log.i(TAG, "Capturing recipt and storing in : " + mReceiptFile.getAbsolutePath());			
			// Now convert the fileName into a Provider URI so it can be written
			// to.
			// TODO 9 Convert the file path to a URI
			Uri uri = Uri.fromFile(mReceiptFile);
			// TODO 10 Add the uri to the Intent as Extra data with a name of MediaStore.EXTRA_OUTPUT
			intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
			// TODO 11 Set the Extra data MediaStore.EXTRA_VIDEO_QUALITY to 1 (high quality)
			intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
			// TODO 12 Start the Activity using startActivityForResult with a resultCode of CAM_REQUEST_CODE
			startActivityForResult(intent, CAM_REQUEST_CODE);
		}
	
			
		 }
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
	 	// Check the resultCode is OK and 
		// correlate the requestCode
	    if (resultCode == Activity.RESULT_OK && 
	    		requestCode == CAM_REQUEST_CODE) {
	    	
	        if (resultCode == RESULT_OK) {
	            //use imageUri here to access the image
		    	// Process result
		    	Drawable camImage = BitmapDrawable.createFromPath(mReceiptFile.getAbsolutePath());
		    	mReceiptImg.setImageDrawable(camImage);	        	

	        } else if (resultCode == RESULT_CANCELED) {
	            Toast nottaken = Toast.makeText(this, "Picture was not taken", Toast.LENGTH_SHORT);
	            nottaken.show();
	        } else {
	            Toast nottaken = Toast.makeText(this, "Picture was not taken", Toast.LENGTH_SHORT);
	            nottaken.show();
	        }
	    }
	}
	
	
}

