package com.yannick.location;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
//import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
//import android.widget.TextView;
import android.widget.Toast;
//import com.yannick.diary.R;

public class GetLocation extends Activity implements LocationListener {
  private LocationManager locationManager;
  private String provider;

  
  public double getLatitude() {
	  locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	  locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);
	  Location location = locationManager.getLastKnownLocation(provider);
	  return (double) (location.getLatitude());
  };
  
  public double getLongitude() {
	  locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	  locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);
	  Location location = locationManager.getLastKnownLocation(provider);
	  return (double) (location.getLongitude());  
  };
  
/** Called when the activity is first created. */

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //setContentView(R.layout.activity_show_location);
    //mlatituteField = (TextView) findViewById(R.id.locEdt_et_lat);
    //mlongitudeField = (TextView) findViewById(R.id.locEdt_et_lon);
    //providerField = (TextView) findViewById(R.id.GPSprov1);

    // Get the location manager
    //locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    // Define the criteria how to select the location provider -> use
    // default
    //Criteria criteria = new Criteria();
    //provider = locationManager.getBestProvider(criteria, false);
    //Location location = locationManager.getLastKnownLocation(provider);

    // Initialize the location fields
    //if (location != null) {
    //  System.out.println("Provider " + provider + " has been selected.");
    //  onLocationChanged(location);
    //} else {
    //  mlatituteField.setText("Location not available");
    // mlongitudeField.setText("Location not available");
    //  mproviderField.setText("Provider Not Available");
   // }
  }

  /* Request updates at startup */
  @Override
  protected void onResume() {
    super.onResume();
    locationManager.requestLocationUpdates(provider, 400, 1, this);
  }

  /* Remove the locationlistener updates when Activity is paused */
  @Override
  protected void onPause() {
    super.onPause();
    locationManager.removeUpdates(this);
  }

  @TargetApi(Build.VERSION_CODES.GINGERBREAD)
//@Override
  public void onLocationChanged(Location location) {
	locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);
    //double lat = (double) (location.getLatitude());
    //double lng = (double) (location.getLongitude());
    //String prov = (String) (provider);
    //mlatituteField.setText(String.valueOf(lat));
    //mlongitudeField.setText(String.valueOf(lng));
    //mproviderField.setText(String.valueOf(prov));
  }

  //@Override
  public void onStatusChanged(String provider, int status, Bundle extras) {
    // TODO Auto-generated method stub

  }

  //@Override
  public void onProviderEnabled(String provider) {
    Toast.makeText(this, "Enabled new provider " + provider,
        Toast.LENGTH_SHORT).show();

  }

  //@Override
  public void onProviderDisabled(String provider) {
    Toast.makeText(this, "Disabled provider " + provider,
        Toast.LENGTH_SHORT).show();
  }
} 