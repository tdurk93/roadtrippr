package roadtrippr.roadtrippr;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    Button saveButton;
    MultiAutoCompleteTextView favRestaurants, favTypes, noRestaurants;
    NumberPicker timeWindow;

    protected GoogleApiClient mGoogleApiClient;
    private PlaceAutocompleteAdapter mAdapter;
    private AutocompleteFilter mAutocompleteFilter;
    private static final LatLngBounds BOUNDS_ATLANTA = new LatLngBounds(
            new LatLng(33.749249, -84.387314), new LatLng(33.749249, -84.387314));
    private static final String TAG = "PlaceAutocompleteAdapter";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Hide soft keyboard by default
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        saveButton = (Button) findViewById(R.id.saveButton);

        final SharedPreferences sharedPref = getSharedPreferences("roadtrippr.roadtrippr", Context.MODE_PRIVATE);
        String favRestaurantsString = sharedPref.getString("favRestaurants", "");
        String favRestaurantsTypesString = sharedPref.getString("favRestaurantsTypes", "");
        String noRestaurantsString = sharedPref.getString("noRestaurants", "");
        int timeWindowInt = sharedPref.getInt("timeWindow", 1);

        favRestaurants = (MultiAutoCompleteTextView)findViewById(R.id.restaurants_field);
        favTypes = (MultiAutoCompleteTextView)findViewById(R.id.restaurant_types_field);
        noRestaurants = (MultiAutoCompleteTextView)findViewById(R.id.unacceptable_restaurants_field);

        timeWindow = (NumberPicker) findViewById(R.id.eating_window_picker);
        timeWindow.setMinValue(0);
        timeWindow.setMaxValue(11);
        timeWindow.setDisplayedValues(new String[]{"0", "5", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55"});


        // Construct a GoogleApiClient for the {@link Places#GEO_DATA_API} using AutoManage
        // functionality, which automatically sets up the API client to handle Activity lifecycle
        // events. If your activity does not extend FragmentActivity, make sure to call connect()
        // and disconnect() explicitly.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0 /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .build();

        // Set up the adapter that will retrieve suggestions from the Places Geo Data API that cover
        // the entire world.
        mAutocompleteFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ESTABLISHMENT)
                .build();
        mAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, BOUNDS_ATLANTA,
                mAutocompleteFilter, true);

        favRestaurants = (MultiAutoCompleteTextView)findViewById(R.id.restaurants_field);
        favTypes = (MultiAutoCompleteTextView)findViewById(R.id.restaurant_types_field);
        noRestaurants = (MultiAutoCompleteTextView)findViewById(R.id.unacceptable_restaurants_field);

        favRestaurants.setOnItemClickListener(mAutocompleteViewClickListener);
        favRestaurants.setAdapter(mAdapter);
        favRestaurants.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        ArrayAdapter favTypesAdapter = ArrayAdapter.createFromResource(this, R.array.restaurants_types, android.R.layout.simple_list_item_1);
        favTypes.setAdapter(favTypesAdapter);
        favTypes.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        noRestaurants.setOnItemClickListener(mAutocompleteViewClickListener);
        noRestaurants.setAdapter(mAdapter);
        noRestaurants.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        favRestaurants.setText(favRestaurantsString);
        favTypes.setText(favRestaurantsTypesString);
        noRestaurants.setText(noRestaurantsString);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str1 = favRestaurants.getText().toString();
                String str2 = favTypes.getText().toString();
                String str3 = noRestaurants.getText().toString();
                int int1 = timeWindow.getValue();

                sharedPref.edit().putString("favRestaurants", str1).apply();
                sharedPref.edit().putString("favRestaurantsTypes", str2).apply();
                sharedPref.edit().putString("noRestaurants", str3).apply();
                sharedPref.edit().putInt("timeWindow", int1).apply();
                finish();

            }
        });

        if (!Objects.equals(favRestaurantsString, "")) {
            favRestaurants.setText(favRestaurantsString);
        }

        if (!Objects.equals(favRestaurantsTypesString, "")) {
            favTypes.setText(favRestaurantsTypesString);
        }

        if (!Objects.equals(noRestaurantsString, "")) {
            noRestaurants.setText(noRestaurantsString);
        }

        timeWindow.setValue(timeWindowInt);
    }

    // ------------------- Google Places API -------------------

    /**
     * Listener that handles selections from suggestions from the AutoCompleteTextView that
     * displays Place suggestions.
     * Gets the place id of the selected item and issues a request to the Places Geo Data API
     * to retrieve more details about the place.
     *
     * @see com.google.android.gms.location.places.GeoDataApi#getPlaceById(com.google.android.gms.common.api.GoogleApiClient,
     * String...)
     */
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

            roadtrippr.roadtrippr.logger.Log.i(TAG, "Autocomplete item selected: " + primaryText);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
             details about the place.
              */
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);


            Toast.makeText(getApplicationContext(), "Clicked: " + primaryText,
                    Toast.LENGTH_SHORT).show();
            roadtrippr.roadtrippr.logger.Log.i(TAG, "Called getPlaceById to get Place details for " + placeId);
        }
    };

    /**
     * Callback for results from a Places Geo Data API query that shows the first place result in
     * the details view on screen.
     */
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                roadtrippr.roadtrippr.logger.Log.e(TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
            }
        }
    };

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        roadtrippr.roadtrippr.logger.Log.e(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());

        // TODO(Developer): Check error code and notify the user of error state and resolution.
        Toast.makeText(this,
                "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
    }
}
