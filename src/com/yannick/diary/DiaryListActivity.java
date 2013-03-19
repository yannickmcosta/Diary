package com.yannick.diary;

import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.yannick.diary.data.Diary;
import com.yannick.diary.R;


public class DiaryListActivity extends ListActivity {

	private static final String TAG = "DiaryListActivity";
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Get the Intent associated with this Activity
        Intent intent = getIntent();

        // If there is no data associated with the Intent, sets the data to the default URI, which
        // accesses a list of expenses.
        // If no Data was passed in the launching Intent, then default it
        // by calling setData with the value Expense.ExpenseItem.CONTENT_URI
        if (intent.getData() == null) {
            intent.setData(Diary.DiaryItem.CONTENT_URI);
        }   		
        
        // Call the queryExpenses method on the DO you just created.
        // The first parameter is an array representing the columns you wish to be returned
        // Use Expense.ExpenseItem.LIST_PROJECTION for this
        // We want to return all the expenses so selection and selectionArgs can both be null
        	
        
        // Create a new cursor by calling managedQuery.
        // The first parameter is the URI for the ContentProvider which is accessed via intent.getData()
        // The remaining parameters are the same as the ones passed into queryExpenses with the addition 
        // of a sort order parameter (ascending / descending)
		Log.v("DiaryListActivity", "Just Before The Projections");
		//Log.i("SQL Log",com.yannick.diary.data.DatabaseHelper.DIARY_TABLE_CREATE); Need to make diary_table_create public for this to work
        Cursor cursor = managedQuery(
            	intent.getData(), 						// Use the default content URI for the provider.
                Diary.DiaryItem.LIST_PROJECTION,    	// Return the expense description and amount
                null,                             		// No where clause, return all records.
                null,                             		// No where clause, therefore no where column values.
                Diary.DiaryItem.DEFAULT_SORT_ORDER  // Use the default sort order.
            );        
		Log.v("DiaryListActivity", "Just After The Projections");

        
		// The names of the cursor columns to display in the view, initialized
		// to the description column
        // Note the creation of these two arrays specifying the columns to be displayed        
        // and the resource ID's to use to display them
        // android.R.id.text1 is a built in resource to show simple text
        // Note that this is the same as when we used the DAO 
        String[] dataColumns = { Diary.DiaryItem.COLUMN_NAME_TITLE } ;
        int[] viewIDs = { android.R.id.text1 };
		Log.v("DiaryListActivity", "Just After The dataColumns");

        // Create a new SimpleCursorAdapter
        // Creates the backing adapter for the ListView.
        // Note that this is the same as when we used the DAO
        SimpleCursorAdapter adapter
            = new SimpleCursorAdapter(
                      this,                             // The Context for the ListView
                      R.layout.diary_list_item,      // Points to the XML for a list item
                      cursor,                           // The cursor to get items from
                      dataColumns,						// The array of Strings holding the names of the data columns to display	
                      viewIDs							// The array of resource ids used to display the data 
              );
        // set the new adapter as the list adapter
        setListAdapter(adapter);
        
        // Register for a context menu
        registerForContextMenu(getListView());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_sync:
			startSync();
			return true;
		case R.id.menu_add:
			addDiaryItem();
			return true;
			// TODO Remove the comments around the next three lines
		case R.id.menu_export:
			startExport();
			return true;			
		default:
			return super.onOptionsItemSelected(item);
		}
	}

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
      super.onCreateContextMenu(menu, v, menuInfo);
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.context_menu, menu);
    }
    
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	
        // The data from the menu item.
        AdapterView.AdapterContextMenuInfo info;

        /*
         * Gets the extra info from the menu item. When an expense in the Expense list is long-pressed, a
         * context menu appears. The menu items for the menu automatically get the data
         * associated with the note that was long-pressed. The data comes from the provider that
         * backs the list.
         *
         */
        try {
            // Casts the data object in the item into the type for AdapterView objects.
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {

            // If the object can't be cast, logs an error
            Log.e(TAG, "bad menuInfo", e);

            // Triggers default processing of the menu item.
            return false;
        }
        // Appends the selected note's ID to the URI sent with the incoming Intent.
        Uri diaryUri = ContentUris.withAppendedId(getIntent().getData(), info.id);  	
        Log.i(TAG, "ID to delete=" + info.id);
	      switch (item.getItemId()) {
	      case R.id.delete:        
	    	  deleteDiaryItem(diaryUri); 
	        return true;
	      default:
	        return super.onContextItemSelected(item);
	      }
    }	
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		
	  	  Intent intent = new Intent(this, DiaryEntryActivity.class);
	  	  // When launching the ExpenseEntryActivity, we must supply the URI of the ContentProvider
	  	  // Do this by adding it as the Data of the Intent
	  	  // Set the Intent Data (you stored it in the Intent for this Activity a little while ago)
	  	  intent.setData(getIntent().getData()); 
	  	  // Put the ID of the expense item into the Intent as extra data ( this is used to
	  	  // notify the ExpenseEntryActivity which expense it is working on) Note: the id you
	  	  // need has been passed into this method as one of the method arguments
	  	  intent.putExtra(Constants.DIARY_ID, id);
	  	  startActivity(intent);
		
	}


	private void deleteDiaryItem(Uri diaryUri) {
		getContentResolver().delete(diaryUri, null, null);	
	}	

	private void addDiaryItem() {

		Intent intent = new Intent(this, DiaryEntryActivity.class);
	  	  intent.setData(getIntent().getData()); 
	  	  intent.putExtra(Constants.DIARY_ID, Constants.DIARY_ITEM_UNDEFINED);		
		startActivity(intent);

	}

	private void startSync() {
		Toast toast = Toast.makeText(this, R.string.toast_str_start_sync, Toast.LENGTH_LONG);
		toast.show();

		Intent startSyncService = new Intent(this, SyncService.class);
		startService(startSyncService);

	}
	
	private void startExport() {
		Toast toast = Toast.makeText(this, R.string.toast_str_exporting_csv, Toast.LENGTH_LONG);
		toast.show();
		
		// TODO Create an Intent with SyncService as the target
		Intent startSyncService = new Intent(this, SyncService.class );
		// Set a flag to notify the service that we want to export to a CSV file
		startSyncService.putExtra(Constants.DIARY_EXTRA_EXPORT_TO_CSV, true);
		
		// TODO Use the Intent to start the service
		startService(startSyncService);

	}		


}