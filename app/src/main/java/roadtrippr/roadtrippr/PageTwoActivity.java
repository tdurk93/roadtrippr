package roadtrippr.roadtrippr;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import roadtrippr.roadtrippr.logger.Log;

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

import java.util.Arrays;
import java.util.ArrayList;

public class PageTwoActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener {

    Button submitButton;
    MultiAutoCompleteTextView favRestaurants, favTypes, noRestaurants;

    protected GoogleApiClient mGoogleApiClient;

    private PlaceAutocompleteAdapter mAdapter;

    private AutocompleteFilter mAutocompleteFilter;

    private static final LatLngBounds BOUNDS_ATLANTA = new LatLngBounds(
            new LatLng(33.749249, -84.387314), new LatLng(33.749249, -84.387314));

    private static final String TAG = "PlaceAutocompleteAdapter";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SharedPreferences sharedPref = getSharedPreferences("roadtrippr.roadtrippr", Context.MODE_PRIVATE);


        setContentView(R.layout.activity_page_two);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Hide soft keyboard by default
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        submitButton = (Button) findViewById(R.id.submitButton);

        final SharedPreferences sharedPreferences = this.getSharedPreferences("roadtrippr.roadtrippr", Context.MODE_PRIVATE);
        String favRestaurantsString = sharedPreferences.getString("favRestaurants", "");
        String favRestaurantsTypesString = sharedPreferences.getString("favRestaurantsTypes", "");
        String noRestaurantsString = sharedPreferences.getString("noRestaurants", "");

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

        favRestaurants = (MultiAutoCompleteTextView)findViewById(R.id.multiAutoCompleteTextView);
        favTypes = (MultiAutoCompleteTextView)findViewById(R.id.multiAutoCompleteTextView2);
        noRestaurants = (MultiAutoCompleteTextView)findViewById(R.id.multiAutoCompleteTextView3);

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

        favRestaurants.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                submitButton.setEnabled(favRestaurants.getText().toString().length() != 0 ||
                        favTypes.getText().toString().length() != 0);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        favTypes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                submitButton.setEnabled(favRestaurants.getText().toString().length() != 0 ||
                        favTypes.getText().toString().length() != 0);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // TODO: Implement Next Activity & Calculate Trip

                MainActivity.currentFavRestaurants = new ArrayList<>(Arrays.asList(favRestaurants.getText().toString().split(", ")));
                MainActivity.currentFavTypes = new ArrayList<>(Arrays.asList(favTypes.getText().toString().split(", ")));
                MainActivity.currentNoRestaurants = new ArrayList<>(Arrays.asList(noRestaurants.getText().toString().split(", ")));
                // TODO save currentFavRestaurants (and related variables) to SharedPrefs
                // Create a Uri from an intent string. Use the result to create an Intent.
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + Uri.encode(getIntent().getStringExtra("destination")));

                // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                // Make the Intent explicit by setting the Google Maps package
                mapIntent.setPackage("com.google.android.apps.maps");

                // Switch main activity to status screen
                final SharedPreferences sharedPref = getSharedPreferences("roadtrippr.roadtrippr", Context.MODE_PRIVATE);
                sharedPref.edit().putBoolean("navigating", true).apply();
                sharedPref.edit().putBoolean("toggleMainScreen", true).apply();

                // Attempt to start an activity that can handle the Intent
                finish();
                startActivity(mapIntent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        favRestaurants = (MultiAutoCompleteTextView)findViewById(R.id.multiAutoCompleteTextView);
        favTypes = (MultiAutoCompleteTextView)findViewById(R.id.multiAutoCompleteTextView2);

        submitButton.setEnabled(favRestaurants.getText().toString().length() != 0 ||
                favTypes.getText().toString().length() != 0);
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

            Log.i(TAG, "Autocomplete item selected: " + primaryText);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
             details about the place.
              */
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

            Toast.makeText(getApplicationContext(), "Clicked: " + primaryText,
                    Toast.LENGTH_SHORT).show();
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
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                Log.e(TAG, "Place query did not complete. Error: " + places.getStatus().toString());
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
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.e(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());

        // TODO(Developer): Check error code and notify the user of error state and resolution.
        Toast.makeText(this,
                "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
    }
}
