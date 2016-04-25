package roadtrippr.roadtrippr.googlePlaces;

/**
 * Created by sungholee on 4/22/16.
 */
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import roadtrippr.roadtrippr.MainActivity;
import roadtrippr.roadtrippr.R;

public class GooglePlacesActivity extends FragmentActivity implements LocationListener {
    private static final String GOOGLE_API_KEY = "AIzaSyB1cWnsuuiVHmlzwEDPos8efzlM9QOQNxI";
    GoogleMap googleMap;
    EditText placeText;
    private int PROXIMITY_RADIUS = 3300; // 3.3km = 2 miles
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //show error dialog if GoolglePlayServices not available
        if (!isGooglePlayServicesAvailable()) {
            finish();
        }
        setContentView(R.layout.activity_google_places);

        setupListView();

        //String type = placeText.getText().toString();
        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + MainActivity.CURRENT_LOCATION.getLatitude() + "," + MainActivity.CURRENT_LOCATION.getLongitude());
        googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlacesUrl.append("&types=" + "restaurant");
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=" + GOOGLE_API_KEY);

        GooglePlacesReadTask googlePlacesReadTask = new GooglePlacesReadTask();
        googlePlacesReadTask.listView = listView;
        googlePlacesReadTask.googlePlacesActivity = this;
        Object[] toPass = new Object[3];
        toPass[0] = googleMap;
        toPass[1] = googlePlacesUrl.toString();
        toPass[2] = GooglePlacesReadTask.OP_NEARBY;
        googlePlacesReadTask.execute(toPass);
    }

    private void setupListView() {
        listView = (ListView) findViewById(R.id.nearbyListView);

        // ListView Item Click Listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item index
                int itemPosition     = position;

                // ListView Clicked item value
                String  itemValue    = (String) listView.getItemAtPosition(position);

                // Show Alert
                Toast.makeText(getApplicationContext(),
                        "Position :" + itemPosition + "  ListItem : " + itemValue, Toast.LENGTH_LONG)
                        .show();

            }

        });
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }



    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(MainActivity.CURRENT_LOCATION.getLatitude(), MainActivity.CURRENT_LOCATION.getLongitude());
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(12));
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }
}