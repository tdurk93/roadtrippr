package roadtrippr.roadtrippr.googlePlaces;

/**
 * Created by sungholee on 4/22/16.
 */
import android.os.AsyncTask;
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

public class GooglePlacesReadTask extends AsyncTask<Object, Integer, String> {
    GooglePlacesActivity googlePlacesActivity;
    String googlePlacesData = null;
    GoogleMap googleMap;
    ListView listView;

    @Override
    protected String doInBackground(Object... inputObj) {
        try {
            googleMap = (GoogleMap) inputObj[0];
            String googlePlacesUrl = (String) inputObj[1];
            Http http = new Http();
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

        List<HashMap<String, String>> nearbyList = placesDisplayTask.getNearbyPlaces();

        Log.d("NEARBY COUNT", "" + nearbyList.size());
        ArrayList<HashMap<String, String>> removeList = new ArrayList<>();
        for (HashMap<String, String> googlePlace : nearbyList) {
            double lat = Double.parseDouble(googlePlace.get("lat"));
            double lng = Double.parseDouble(googlePlace.get("lng"));
            if (!MainActivity.isEnroute(new LatLng(lat, lng))) {
                Log.d("ENROUTE FILTER", "" + "removing");
                removeList.add(googlePlace);
            }
            Log.d("ENROUTE FILTER", "" + "not removing");
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

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(googlePlacesActivity,
                android.R.layout.simple_list_item_1, android.R.id.text1, values);
        listView.setAdapter(adapter);
    }
}
