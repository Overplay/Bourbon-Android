package tv.ourglass.alyssa.bourbon_android.Scenes.Control;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import okhttp3.Call;
import okhttp3.Response;
import tv.ourglass.alyssa.bourbon_android.Model.BourbonNotification;
import tv.ourglass.alyssa.bourbon_android.Model.OGVenue.OGVenue;
import tv.ourglass.alyssa.bourbon_android.Model.OGVenue.OGVenueListAdapter;
import tv.ourglass.alyssa.bourbon_android.Model.OGVenue.OGVenueType;
import tv.ourglass.alyssa.bourbon_android.Model.StateController;
import tv.ourglass.alyssa.bourbon_android.Networking.OGCloud;
import tv.ourglass.alyssa.bourbon_android.R;
import tv.ourglass.alyssa.bourbon_android.Scenes.Tabs.MainTabsActivity;


public class ChooseVenueFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener{

    String TAG = "ChooseVenueFragment";

    ArrayList<OGVenue> mVenues = new ArrayList<>();

    OGVenueListAdapter mVenueListAdapter;

    BroadcastReceiver mBroadcastReceiver;

    GoogleApiClient mLocationClient;  // used to get user's location

    Location mLastLocation;

    Handler mHandler = new Handler();

    OGCloud.HttpCallback venueCallback = new OGCloud.HttpCallback() {

        @Override
        public void onSuccess(final Response response) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mVenueListAdapter.notifyDataSetChanged();
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

        if (mLocationClient == null) {
            mLocationClient = new GoogleApiClient.Builder(this.getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mVenueListAdapter.notifyDataSetChanged();
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
        View rootView = inflater.inflate(R.layout.fragment_venues, container, false);

        // set up top toolbar
        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.tb_venues));

        // set up venue list
        mVenueListAdapter = new OGVenueListAdapter(getActivity(), OGVenueType.ALL,
                new OGVenueListAdapter.OnClickVenue() {
                    @Override
                    public void onClick(View view, OGVenue venue) {
                        if (venue != null) {

                            Answers.getInstance().logContentView(new ContentViewEvent()
                                .putContentType("Fragment")
                                .putContentName("Venue Overview")
                                .putCustomAttribute("Venue", venue.name));

                            ((MainTabsActivity) getActivity()).openNewFragment(
                                    ChooseDeviceFragment.newInstance(venue.name, venue.uuid));
                        }
                    }
                }, View.VISIBLE);
        ListView listView = (ListView) rootView.findViewById(R.id.venueList);
        listView.setAdapter(mVenueListAdapter);
        listView.setEmptyView(rootView.findViewById(R.id.empty));

        getVenues();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getVenues();
                mHandler.postDelayed(this, 15000);
            }
        }, 15000);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_add).setVisible(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        mLocationClient.connect();

    }

    @Override
    public void onStop() {
        if (mLocationClient != null) {
            mLocationClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        this.getVenues();
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

    private void getVenues() {
        StateController.getInstance().findAllVenues(venueCallback);
    }

    private void sortByLocationAndReload() {
        if (mLastLocation != null) {
            Collections.sort(mVenues, new Comparator<OGVenue>() {
                @Override
                public int compare(OGVenue lhs, OGVenue rhs) {
                    Location a = new Location(LocationManager.GPS_PROVIDER);
                    Location b = new Location(LocationManager.GPS_PROVIDER);

                    a.setLatitude(lhs.latitude);
                    a.setLongitude(lhs.longitude);
                    b.setLatitude(rhs.latitude);
                    b.setLongitude(rhs.longitude);

                    Float distA = a.distanceTo(mLastLocation);
                    Float distB = b.distanceTo(mLastLocation);

                    return distA.compareTo(distB);
                }
            });
        }

        mVenueListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onConnected(Bundle bundle) {
        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mLocationClient);
            sortByLocationAndReload();

        } catch (SecurityException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        this.getVenues();
    }
}
