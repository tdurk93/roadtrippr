package roadtrippr.roadtrippr;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import roadtrippr.roadtrippr.logger.Log;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StatusMapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link StatusMapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StatusMapFragment extends MapFragment implements OnMapReadyCallback {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_DESTINATION = "destination";
    private Context myContext;
    private static Context staticContext;

    public StatusMapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param destination The destination, as a string
     * @return A new instance of fragment StatusMapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StatusMapFragment newInstance(String destination) {
        StatusMapFragment fragment = new StatusMapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DESTINATION, destination);
        fragment.setArguments(args);
        fragment.myContext = staticContext;
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            staticContext = context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
    }

    public void onMapReady(GoogleMap map) {
        centerMapOnCurrentLocation(map);
        MainActivity.GOOGLE_MAP = map;
    }

    public void centerMapOnCurrentLocation(GoogleMap map) {
        try {
            map.setMyLocationEnabled(true);
            LocationManager locationManager = (LocationManager) myContext.getSystemService(Context.LOCATION_SERVICE);

            Criteria criteria = new Criteria();
            Location location = locationManager.getLastKnownLocation(locationManager
                    .getBestProvider(criteria, false));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()), 14));
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                    .zoom(14)
                    .build();                   // Creates a CameraPosition from the builder
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        } catch (java.lang.SecurityException e) {
            Log.e("StatusMapFragment", "Need permission to do that!");
        } catch (java.lang.Exception e) {
            Log.e("StatusMapFragment", "Unable to zoom in to user's location.");
        }
    }
}
