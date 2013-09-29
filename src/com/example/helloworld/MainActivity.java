package com.example.helloworld;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		LocationManager LocationManager =
		        (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
		LocationManager.requestLocationUpdates(android.location.LocationManager.NETWORK_PROVIDER,
		        10000,          // 10-second interval.
		        10,             // 10 meters.
		        listener);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	/** Called when the user clicks the Send button */
	public void sendMessage(View view)
	{
		//Intent intent = new Intent(this, DisplayMessageActivity.class);
		TextView locView = (TextView) findViewById(R.id.locationView);
		String locText = locView.getText().toString();
		//intent.putExtra(EXTRA_MESSAGE, message);
		//startActivity(intent);
      Toast.makeText(this, "sendMessage v4", Toast.LENGTH_LONG).show();
//		new LocSender().execute(locText);
	}
	
	//public final static String EXTRA_MESSAGE = "com.example.helloworld.MESSAGE";
	private final LocationListener listener = new LocationListener() {
		public void onLocationChanged(Location location) {
	        // Bypass reverse-geocoding if the Geocoder service is not available on the
	        // device. The isPresent() convenient method is only available on Gingerbread or above.
	        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD /*&& Geocoder.isPresent()*/) {
	            // Since the geocoding API is synchronous and may take a while.  You don't want to lock
	            // up the UI thread.  Invoking reverse geocoding in an AsyncTask.
	        (new ReverseGeocodingTask(MainActivity.this)).execute(new Location[] {location});
	        if (location != null){
	        	TextView textView = (TextView)findViewById(R.id.locationView);
	        	textView.setText(location.toString());
	        }
	    }

		@Override
		public void onProviderDisabled(String arg0) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};
	

	// AsyncTask encapsulating the reverse-geocoding API.  Since the geocoder API is blocked,
	// we do not want to invoke it from the UI thread.
	private class ReverseGeocodingTask extends AsyncTask<Location, Void, String> {
	    Context mContext;

	    public ReverseGeocodingTask(Context context) {
	        super();
	        mContext = context;
	    }

	    @Override
	    protected String doInBackground(Location... params) {
	        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());

	        Location loc = params[0];
	        List<Address> addresses = null;
	        String addressText = null;
	        try {
	            // Call the synchronous getFromLocation() method by passing in the lat/long values.
	            addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
	        } catch (IOException e) {
	            e.printStackTrace();
	            // Update UI field with the exception.
	            //Message.obtain(mHandler, UPDATE_ADDRESS, e.toString()).sendToTarget();
	        }
	        if (addresses != null & addresses.size() > 0) {
	            Address address = addresses.get(0);
	            // Format the first line of address (if available), city, and country name.
	            addressText = String.format("%s, %s, %s",
	                    address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
	                    address.getLocality(),
	                    address.getCountryName());
	            // Update the UI via a message handler.
	            //Message.obtain(mHandler, UPDATE_ADDRESS, addressText).sendToTarget();
	        }
	        return addressText;
	    }
	    @Override
	    protected void onPostExecute(String result){
	    	TextView textView = (TextView)findViewById(R.id.locationView);
	    	textView.setText(result);
	    }
	}
	
	private class LocSender extends AsyncTask<String, String, String>{

		@Override
		protected String doInBackground(String... loc) {
			DataInputStream dis = null;
			ObjectOutputStream oos = null;
			JSONObject obj = new JSONObject();
			StringBuffer response = new StringBuffer();
			byte[] buffer = new byte[100];
			try {
				URL url = new URL("http://59.78.23.215:8080/helloweb/HelloServlet");
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("POST");
				conn.setDoInput(true);
				conn.setDoOutput(true);
				conn.setUseCaches(false);
				conn.setRequestProperty("Content-Type", "text/json");
				
				obj.put("loc", loc[0]);
				conn.connect();
				oos = new ObjectOutputStream(conn.getOutputStream());
				oos.writeObject(obj.toString());
				oos.flush();
				oos.close();
	
				dis = new DataInputStream(conn.getInputStream());
				while(dis.read(buffer)>=0)
				{
					response.append(new String(buffer));
				}
				dis.close();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			} finally{
				
			}
			return response.toString();
		}
		@Override
		protected void onPostExecute(String result){
			TextView textView = (TextView)findViewById(R.id.responseView);
	    	textView.setText(result);
		}
	}

}
