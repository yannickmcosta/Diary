package com.yannick.camera;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.Toast;

import com.yannick.diary.R;

public class CameraEmulatorActivity extends Activity {
	
	private static final String TAG="CameraEmulatorActivity";

	private ImageView imgView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_emulator_layout);
		imgView = (ImageView)findViewById(R.id.camEm_img);

		Toast toast = Toast.makeText(this, R.string.str_cam_msg, Toast.LENGTH_LONG);
		toast.show();		
		
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER){
		
			FileOutputStream out=null;
			Intent launchingIntent = getIntent();
			// Get the URI of the file for the result
			try {
				Uri fileUri = (Uri)launchingIntent.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
				out = new FileOutputStream(fileUri.getPath());
				Drawable d = imgView.getDrawable();		
				// This code is not robust! Should work in our pretend camera
				Bitmap bitMap = ((BitmapDrawable)d).getBitmap();
				// Should really be using the MediaStore.EXTRA_VIDEO_QUALITY to decide on compression
				bitMap.compress(Bitmap.CompressFormat.PNG, 90, out);
				out.close();
			} catch (FileNotFoundException e) {
				Log.e(TAG, "Error saving file", e);
			} catch (IOException e) {
				Log.e(TAG, "Error saving file", e);
			} finally {
				if(null != out){
					try {
						out.close();
					} catch (IOException e) {
						Log.e(TAG, "Failed to close stream", e);
					}
				}
			}
			setResult(Activity.RESULT_OK); 		
			finish();
			return true;
		}	
		return super.onKeyUp(keyCode, event);
	}

//	@Override
//	public boolean onKeyUp(int keyCode, KeyEvent event) {
//		// Check for the camera key
//		if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER){
//			// Use this button to emulate taking a picture
//			Drawable d = imgView.getDrawable();
//			// This code is not robust! Should work in our pretend camera
//			Bitmap bitMap = ((BitmapDrawable)d).getBitmap();
//			Intent resultIntent = new Intent();
//			resultIntent.putExtra("data", bitMap);
//			setResult(Activity.RESULT_OK, resultIntent); 
//			finish();
//			return true;
//		} else {
//			return super.onKeyUp(keyCode, event);
//		}
//	}

}
