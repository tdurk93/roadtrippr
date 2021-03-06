package roadtrippr.roadtrippr;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.DialogFragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ViewFlipper;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import android.widget.TimePicker;
import android.widget.TextView;

import roadtrippr.roadtrippr.googlePlaces.GooglePlacesActivity;
import roadtrippr.roadtrippr.googlePlaces.GooglePlacesReadTask;
import roadtrippr.roadtrippr.logger.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        CancelNavigationFragment.CancelNavigationListener,
        StatusMapFragment.OnFragmentInteractionListener {
    private ViewFlipper viewFlipper;
    private CountDownTimer countdown;
    private LocationManager locationManager;
    private String provider;
    private boolean isRunning;
    private boolean isNotified;
    private int hourPicked;
    private int minPicked;
    private static final int LOCATION_REQUEST_CODE = 1;
    private static final int SETUP_LOCATION_CODE = 2;
    private String destination = "Atlanta, GA";
    static ArrayList<String> currentFavRestaurants = new ArrayList<>(),
            currentFavTypes = new ArrayList<>(),
            currentNoRestaurants = new ArrayList<>();

    public static GoogleMap GOOGLE_MAP = null;
    public static ArrayList<MarkerOptions> nearbyMarkers = new ArrayList<>();
    public static HashMap<Integer, MarkerOptions> favoriteMarkers = new HashMap<>();


    AutoCompleteTextView endLocationTextView;

    protected GoogleApiClient mGoogleApiClient;

    private PlaceAutocompleteAdapter mAdapter;

    public static Location CURRENT_LOCATION;

    private static final LatLngBounds BOUNDS_ATLANTA = new LatLngBounds(
            new LatLng(33.749249, -84.387314), new LatLng(33.749249, -84.387314));

    private static final String TAG = "PlaceAutocompleteAdapter";

    static LatLng destinationLatLng = null;

    public void onContinueClicked(View view) {
        Intent i = new Intent(getApplicationContext(), PageTwoActivity.class);
        if (!endLocationTextView.getText().toString().equals("")) {
            destination = endLocationTextView.getText().toString();
        }
        i.putExtra("destination", destination);
        isRunning = true;
        startTimer();
        startActivity(i);
    }

    public void cancelButton(View view) {
        if (isRunning) {
            isNotified = false;
            countdown.cancel();
        }

        DialogFragment newFragment = new CancelNavigationFragment();
        newFragment.show(getFragmentManager(), "cancel");
    }

    public void viewRestaurants(View view) {
        Intent i = new Intent(getApplicationContext(), GooglePlacesActivity.class);
        startActivity(i);
    }

    @Override
    public void onCancelNavigation(DialogFragment dialog) {
        final SharedPreferences sharedPref = getSharedPreferences("roadtrippr.roadtrippr", Context.MODE_PRIVATE);
        sharedPref.edit().putBoolean("navigating", false).apply();
        viewFlipper = (ViewFlipper) findViewById(R.id.viewflipper);
        viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left));
        viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right));
        viewFlipper.showPrevious();
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewFlipper = (ViewFlipper) findViewById(R.id.viewflipper);

        // TODO load currentFavRestaurants from sharedPrefs

        final SharedPreferences sharedPref = getSharedPreferences("roadtrippr.roadtrippr", Context.MODE_PRIVATE);
        Boolean navigating = sharedPref.getBoolean("navigating", false);
        if (navigating) {
            viewFlipper.showNext();
        }

        // Construct a GoogleApiClient for the {@link Places#GEO_DATA_API} using AutoManage
        // functionality, which automatically sets up the API client to handle Activity lifecycle
        // events. If your activity does not extend FragmentActivity, make sure to call connect()
        // and disconnect() explicitly.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0 /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .build();

        setupLocation(true);

        TimePicker tp = (TimePicker) findViewById(R.id.mealTimePicker);
        assert tp != null;
        tp.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                hourPicked = hourOfDay;
                minPicked = minute;
            }
        });

        final Button continueButton;
        continueButton = (Button) findViewById(R.id.continueButton);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    123);
            return;
        }
        endLocationTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                assert continueButton != null;
                continueButton.setEnabled(endLocationTextView.getText().length() != 0);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(i);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        viewFlipper = (ViewFlipper) findViewById(R.id.viewflipper);
        final SharedPreferences sharedPref = getSharedPreferences("roadtrippr.roadtrippr", Context.MODE_PRIVATE);
        Boolean toggleMainScreen = sharedPref.getBoolean("toggleMainScreen", false);
        Boolean navigating = sharedPref.getBoolean("navigating", false);
        // Switch to status screen if navigating
        if (toggleMainScreen) { // I think this should occur once after returning from navigation
            viewFlipper.showNext();
            sharedPref.edit().putBoolean("toggleMainScreen", false).apply();
            // TODO is there a better place to put the following 2 statements?
            StatusMapFragment myGMapFragment = StatusMapFragment.newInstance(destination);
            ((MapFragment) getFragmentManager().findFragmentById(R.id.nearbyMap)).getMapAsync(myGMapFragment);

        }
        if (navigating) {
            ArrayAdapter<String> favoritesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
            for(int i = 0; i < currentFavRestaurants.size(); i++) {

                GooglePlacesReadTask googlePlacesReadTask = new GooglePlacesReadTask();
                Object[] toPass = new Object[7];
                toPass[0] = null;
                toPass[1] =
                        "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                        "location=" + MainActivity.CURRENT_LOCATION.getLatitude() + "," + MainActivity.CURRENT_LOCATION.getLongitude() +
                        "&sensor=true" +
                        "&key=AIzaSyB1cWnsuuiVHmlzwEDPos8efzlM9QOQNxI" +
                        "&rankby=distance" +
                        "&name=" + currentFavRestaurants.get(i);
                toPass[2] = GooglePlacesReadTask.OP_FAVORITE;
                toPass[3] = currentFavRestaurants.get(i);
                toPass[4] = i;
                toPass[5] = this;
                toPass[6] = favoritesAdapter;
                googlePlacesReadTask.execute(toPass);
            }
        }
        ListView userFavoriteRestaurants = (ListView) findViewById(R.id.userFavoriteRestaurants);
        assert userFavoriteRestaurants != null;
        userFavoriteRestaurants.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (favoriteMarkers.get(position) != null) {
                    GOOGLE_MAP.clear();
                    GOOGLE_MAP.addMarker(favoriteMarkers.get(position));
                    GOOGLE_MAP.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(CURRENT_LOCATION.getLatitude(), CURRENT_LOCATION.getLongitude()), 14));
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(favoriteMarkers.get(position).getPosition())
                            .zoom(14)
                            .build();                   // Creates a CameraPosition from the builder
                    GOOGLE_MAP.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    // ------------------- Google Places API -------------------

    public void setupAutocompleteTextViews() {


        // Set up the adapter that will retrieve suggestions from the Places Geo Data API that cover
        // the entire world.
        mAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, BOUNDS_ATLANTA,
                null, false);

        endLocationTextView = (AutoCompleteTextView)findViewById(R.id.endLocationAutoCompleteTextView);

        assert endLocationTextView != null;
        endLocationTextView.setOnItemClickListener(mAutocompleteViewClickListener);
        endLocationTextView.setAdapter(mAdapter);

    }

    private void setupLocation(boolean askPermission) {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
            if (askPermission) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        },
                        SETUP_LOCATION_CODE
                );
            }
            return;
        }
        locationManager.requestLocationUpdates(provider, 400, 1, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {}
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override
            public void onProviderEnabled(String provider) {}
            @Override
            public void onProviderDisabled(String provider) {}
        });
        CURRENT_LOCATION = locationManager.getLastKnownLocation(provider);
        if (CURRENT_LOCATION != null) {
            Log.i("Location Info", CURRENT_LOCATION.toString());
        } else {
            Log.i("Location Info", "Location failed to be found");
        }
        setupAutocompleteTextViews();
    }

    private AdapterView.OnItemClickListener mAutocompleteViewClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a AutocompletePrediction from which we
             read the place ID and title.
              */
            final AutocompletePrediction item = mAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            final CharSequence primaryText = item.getPrimaryText(null);

            Log.i(TAG, "Autocomplete item selected: " + primaryText);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
             details about the place.
              */
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

            //Toast.makeText(getApplicationContext(), "Clicked: " + primaryText,
              //      Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Called getPlaceById to get Place details for " + placeId);
        }
    };

    /**
     * Callback for results from a Places Geo Data API query that shows the first place result in
     * the details view on screen.
     */
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                Log.e(TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
            } else {
                destinationLatLng = places.get(0).getLatLng();
                places.release();
            }
        }
    };

    /**
     * Called when the Activity could not connect to Google Play services and the auto manager
     * could resolve the error automatically.
     * In this case the API is not available and notify the user.
     *
     * @param connectionResult can be inspected to determine the cause of the failure
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Log.e(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());

        Toast.makeText(this,
                "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        if (requestCode == SETUP_LOCATION_CODE || requestCode == LOCATION_REQUEST_CODE) {
            boolean accepted = true;
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    accepted = false;
                }
            }
            if (!accepted) {
                Toast.makeText(
                        getApplicationContext(),
                        "Roadtrippr cannot function without your location. " +
                                "Please grant it permission to use it to continue.",
                        Toast.LENGTH_LONG
                ).show();
            } else if (requestCode == SETUP_LOCATION_CODE){
                setupLocation(false);
            } else {
                toastLocation();
            }
        }
    }

    private void toastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_REQUEST_CODE
            );
            return;
        }
        if (provider == null) {
            provider = locationManager.getBestProvider(new Criteria(), false);
        }
        CURRENT_LOCATION = locationManager.getLastKnownLocation(provider);
        if (CURRENT_LOCATION != null) {
            Toast.makeText(getApplicationContext(), CURRENT_LOCATION.toString(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "No location found", Toast.LENGTH_SHORT).show();
        }
    }

    public class RemainingTime extends CountDownTimer {

        TextView countdown = (TextView) findViewById(R.id.countdown);

        public RemainingTime(long start, long interval) {
            super(start, interval);
        }

        public void onTick(long millisUntilFinished) {

            int seconds = (int) (millisUntilFinished / 1000) % 60;
            int minutes = (int) ((millisUntilFinished / (1000*60)) % 60);
            int hours   = (int) ((millisUntilFinished / (1000*60*60)) % 24);

            if (minutes == 0 && hours != 0) {
                hours--;
                minutes = 59;
            }

            if (hours == 0) {
                countdown.setText(String.format("%02d:%02d", minutes, seconds));
            } else {
                countdown.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
            }

            if (millisUntilFinished <= 300000 && !isNotified) {
                isNotified = true;
                showNotification();
            }
        }

        public void onFinish() {
            countdown.setText("Searching...");
        }
    }

    public void startTimer(){
        final SharedPreferences sharedPref = getSharedPreferences("roadtrippr.roadtrippr", Context.MODE_PRIVATE);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourPicked);
        calendar.set(Calendar.MINUTE, minPicked);

        Calendar calendar2 = Calendar.getInstance();
        long currentTime = System.currentTimeMillis();
        long diffTime = calendar.getTimeInMillis()- currentTime;

        if (hourPicked == 0 && minPicked == 0) {
            sharedPref.edit().putBoolean("searching", true).apply();
            TextView countdown = (TextView) findViewById(R.id.countdown);
            countdown.setText("Searching...");
        } else {
            int hourDiff = (24 - (calendar2.get(Calendar.HOUR_OF_DAY) - hourPicked))*60*60*1000;
            int minDiff = Math.abs(calendar2.get(Calendar.MINUTE) - minPicked)*60*1000;
            if (diffTime >= 0) {
                countdown = new RemainingTime(diffTime, 1000).start();
            } else if (hourPicked == calendar2.get(Calendar.HOUR_OF_DAY)) {
                countdown = new RemainingTime(hourDiff-minDiff, 1000).start();
            } else {
                countdown = new RemainingTime(hourDiff+minDiff, 1000).start();
            }
        }
    }

    public static boolean isEnroute(LatLng stop) {

        double currLon = CURRENT_LOCATION.getLongitude();
        double currLat = CURRENT_LOCATION.getLatitude();
        double stopLon = stop.longitude;
        double stopLat = stop.latitude;
        double destLon = destinationLatLng.longitude;
        double destLat = destinationLatLng.latitude;
        double trajectoryX = destLon - currLon;
        double trajectoryY = destLat - currLat;
        double detourX = stopLon - currLon;
        double detourY = stopLat - currLat;

        // Check cos ( angle between detour and trajectory ) > 0
        return (detourX * trajectoryX + detourY * trajectoryY) / (
                Math.sqrt(detourX * detourX + detourY * detourY) *
                        Math.sqrt(trajectoryX * trajectoryX + trajectoryY * trajectoryY)
        ) > 0; // Considered enroute if route is less than 90 degrees in the wrong direction

    }

    public void showNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,intent, 0);

        Notification.InboxStyle inboxStyle = new Notification.InboxStyle();
        inboxStyle.addLine("Nearest Restaurant Details");
        inboxStyle.addLine("Click here to view all nearby Restaurant");
        inboxStyle.setBigContentTitle("RoadTrippr");
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("RoadTrippr")
                .setStyle(inboxStyle)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSound(alarmSound)
                .build();

        notificationManager.notify(0, notification);

    }

}


