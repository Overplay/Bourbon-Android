package tv.ourglass.alyssa.bourbon_android.Scenes.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Response;
import tv.ourglass.alyssa.bourbon_android.Model.BourbonNotification;
import tv.ourglass.alyssa.bourbon_android.Model.OGVenue.OGVenue;
import tv.ourglass.alyssa.bourbon_android.Model.OGVenue.OGVenueListAdapter;
import tv.ourglass.alyssa.bourbon_android.Model.OGVenue.OGVenueType;
import tv.ourglass.alyssa.bourbon_android.Model.StateController;
import tv.ourglass.alyssa.bourbon_android.Networking.OGCloud;
import tv.ourglass.alyssa.bourbon_android.R;

public class MapFragment extends Fragment implements GoogleMap.OnInfoWindowClickListener {

    String TAG = "MapFragment";

    //ArrayList<OGVenue> locationList = new ArrayList<>();

    MapView mMapView = null;

    private GoogleMap googleMap;

    OGVenueListAdapter mVenueListAdapter = null;

    BroadcastReceiver mBroadcastReceiver;

    OGCloud.HttpCallback venueCallback = new OGCloud.HttpCallback() {

        @Override
        public void onSuccess(final Response response) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mVenueListAdapter != null) {
                        mVenueListAdapter.notifyDataSetChanged();
                        displayVenuesOnMap();
                    }
                }
            });
            response.body().close();
        }

        @Override
        public void onFailure(Call call, final IOException e, OGCloud.OGCloudError error) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), "Error retrieving venues", Toast.LENGTH_SHORT)
                            .show();
                }
            });
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mVenueListAdapter != null) {
                            mVenueListAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        };

        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(mBroadcastReceiver,
                        new IntentFilter(BourbonNotification.allVenuesUpdated.name()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_locations, container, false);

        // check if google play services is available
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int code = api.isGooglePlayServicesAvailable(getActivity());

        if (code != ConnectionResult.SUCCESS) {
            Log.d(TAG, "Google Play Services unavailable.");
            TextView tv = (TextView) rootView.findViewById(R.id.error);
            tv.setVisibility(View.VISIBLE);

        } else {
            mVenueListAdapter = new OGVenueListAdapter(getActivity(), OGVenueType.ALL,
                    new OGVenueListAdapter.OnClickVenue() {
                        @Override
                        public void onClick(View view, OGVenue venue) {
                            Animation clickAnimation = new AlphaAnimation(1.0f, 0.3f);
                            clickAnimation.setDuration(300);
                            view.startAnimation(clickAnimation);

                            if (venue.marker != null) {
                                venue.marker.showInfoWindow();
                            }
                        }
                    });

            // attach the adapter to the list view
            ListView listView = (ListView) rootView.findViewById(R.id.locationList);
            listView.setAdapter(mVenueListAdapter);

            // Set up map
            mMapView = (MapView) rootView.findViewById(R.id.mapView);
            mMapView.onCreate(savedInstanceState);

            mMapView.onResume(); // needed to get the map to display immediately

            try {
                MapsInitializer.initialize(getActivity().getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }

            StateController.getInstance().findAllVenues(venueCallback);
        }

        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("Map Fragment")
                .putContentType("Fragment"));

        return rootView;
    }

    public void displayVenuesOnMap() {
        if (mMapView != null && mVenueListAdapter != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMapView.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap mMap) {
                            googleMap = mMap;

                            googleMap.setOnInfoWindowClickListener(MapFragment.this);

                            // for showing a "move to my location" button
                            try {
                                googleMap.setMyLocationEnabled(true);
                            } catch (SecurityException e) {
                                Log.d(TAG, e.getLocalizedMessage());
                            }

                            // show markers for the venues
                            for (OGVenue venue : StateController.getInstance().getAllVenues()) {

                                LatLng markerLoc = new LatLng(venue.latitude, venue.longitude);
                                venue.marker = googleMap.addMarker(new MarkerOptions()
                                        .position(markerLoc)
                                        .title(venue.name)
                                        .snippet(venue.getAddress()));

                                // because we added the marker
                                mVenueListAdapter.notifyDataSetChanged();
                            }

                            // try to center map on current location
                            LocationManager locationManager = (LocationManager) getContext()
                                    .getSystemService(Context.LOCATION_SERVICE);
                            Criteria criteria = new Criteria();

                            try {
                                Location location = locationManager.getLastKnownLocation(
                                        locationManager.getBestProvider(criteria, false));

                                if (location != null) {
                                    LatLng pos = new LatLng(
                                            location.getLatitude(),
                                            location.getLongitude());

                                    CameraPosition cameraPosition = new CameraPosition.Builder()
                                            .target(pos)
                                            .zoom(12)
                                            .build();

                                    googleMap.animateCamera(CameraUpdateFactory
                                            .newCameraPosition(cameraPosition));

                                } else { // center map on US
                                    LatLng pos = new LatLng(37.0902, -95.7129);

                                    CameraPosition cameraPosition = new CameraPosition.Builder()
                                            .target(pos)
                                            .zoom(3.5f)
                                            .build();

                                    googleMap.animateCamera(CameraUpdateFactory
                                            .newCameraPosition(cameraPosition));
                                }

                            } catch (Exception e) {
                                Log.e(TAG, e.getLocalizedMessage());

                                // center map on US
                                LatLng pos = new LatLng(37.0902, -95.7129);

                                CameraPosition cameraPosition = new CameraPosition.Builder()
                                        .target(pos)
                                        .zoom(3.5f)
                                        .build();

                                googleMap.animateCamera(CameraUpdateFactory
                                        .newCameraPosition(cameraPosition));
                            }
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMapView != null) {
            mMapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMapView != null) {
            mMapView.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMapView != null) {
            mMapView.onDestroy();
        }
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mMapView != null) {
            mMapView.onLowMemory();
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Log.d(TAG, "marker clicked");
        LatLng pos = marker.getPosition();

        Uri mapIntentUri = Uri.parse(String.format(Locale.ENGLISH, "google.navigation:q=%f,%f",
                pos.latitude, pos.longitude));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            showAlert(getString(R.string.uhoh), "We couldn't find a navigation app installed on your device.");
        }
    }

    public void showAlert(String title, String message) {
        AlertDialog alert = new AlertDialog.Builder(getActivity()).create();
        alert.setTitle(title);
        alert.setMessage(message);
        alert.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alert.show();
    }
}
