package roadtrippr.roadtrippr.googlePlaces;

/**
 * Created by sungholee on 4/22/16.
 */
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import roadtrippr.roadtrippr.MainActivity;
import roadtrippr.roadtrippr.R;

public class GooglePlacesReadTask extends AsyncTask<Object, Integer, String> {
    GooglePlacesActivity googlePlacesActivity;
    String googlePlacesData = null;
    GoogleMap googleMap;
    ListView listView;
    int operation;
    String favName = null;
    int index;
    AppCompatActivity activity = null;
    ArrayAdapter<String> favoritesAdapter = null;

    public static final int OP_NEARBY = 100;
    public static final int OP_FAVORITE = 200;

    @Override
    protected String doInBackground(Object... inputObj) {
        try {
            googleMap = (GoogleMap) inputObj[0];
            String googlePlacesUrl = (String) inputObj[1];
            Http http = new Http();
            operation = (int) inputObj[2];
            if (operation == OP_FAVORITE) {
                favName = (String) inputObj[3];
                index = (int) inputObj[4];
                activity = (AppCompatActivity) inputObj[5];
                favoritesAdapter = (ArrayAdapter<String>)inputObj[6];
            }
            googlePlacesData = http.read(googlePlacesUrl);
        } catch (Exception e) {
            Log.d("Google Place Read Task", e.toString());
        }
        return googlePlacesData;
    }

    @Override
    protected void onPostExecute(String result) {

        PlacesDisplayTask placesDisplayTask = new PlacesDisplayTask();

        placesDisplayTask.googleMap = googleMap;
        placesDisplayTask.result = result;

        List<HashMap<String, String>> nearbyList = placesDisplayTask.getPlacesResult();

        Log.d("NEARBY COUNT", "" + nearbyList.size());
        ArrayList<HashMap<String, String>> removeList = new ArrayList<>();
        for (HashMap<String, String> googlePlace : nearbyList) {
            double lat = Double.parseDouble(googlePlace.get("lat"));
            double lng = Double.parseDouble(googlePlace.get("lng"));
            if (!MainActivity.isEnroute(new LatLng(lat, lng))) {
                Log.d("ENROUTE FILTER", "" + "removing" + googlePlace.get("place_name"));
                removeList.add(googlePlace);
            } else {
                Log.d("ENROUTE FILTER", "" + "not removing" + googlePlace.get("place_name"));
            }
        }
        nearbyList.removeAll(removeList);

        String[] values = new String[nearbyList.size()];
        Log.d("NEARBY COUNT", "" + nearbyList.size());
        for (int i = 0; i < nearbyList.size(); i++) {
            MarkerOptions markerOptions = new MarkerOptions();
            HashMap<String, String> googlePlace = nearbyList.get(i);
            double lat = Double.parseDouble(googlePlace.get("lat"));
            double lng = Double.parseDouble(googlePlace.get("lng"));
            String placeName = googlePlace.get("place_name");
            String id = googlePlace.get("id");
            String vicinity = googlePlace.get("vicinity");
            LatLng latLng = new LatLng(lat, lng);
            String distance = googlePlace.get("distance");
            markerOptions.position(latLng);
            markerOptions.title(placeName + " : " + vicinity);
            //googleMap.addMarker(markerOptions);
            Log.d("nearby restaurant", placeName + ", id: " + id + ", distance: " + distance);

            values[i] = placeName;

        }

        if (operation == OP_NEARBY) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(googlePlacesActivity,
                    android.R.layout.simple_list_item_1, android.R.id.text1, values);
            listView.setAdapter(adapter);
        } else if (operation == OP_FAVORITE) {
            favoritesAdapter.add(favName + " (" + (double)Math.round(Double.parseDouble(nearbyList.get(0).get("distance"))*10)/10.0 + " miles)");
            ListView userFavoriteRestaurants = (ListView) activity.findViewById(R.id.userFavoriteRestaurants);
            userFavoriteRestaurants.setAdapter(favoritesAdapter);
        }
    }
}
