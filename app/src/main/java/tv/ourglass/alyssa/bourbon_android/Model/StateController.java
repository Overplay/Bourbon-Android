package tv.ourglass.alyssa.bourbon_android.Model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import okhttp3.Call;
import okhttp3.Response;
import tv.ourglass.alyssa.bourbon_android.BourbonApplication;
import tv.ourglass.alyssa.bourbon_android.Model.OGVenue.OGVenue;
import tv.ourglass.alyssa.bourbon_android.Networking.OGCloud;
import tv.ourglass.alyssa.bourbon_android.Networking.SingleShotLocationProvider;

/**
 * Created by atorres on 5/13/17.
 */

public class StateController {

    public final String TAG = "StateController";

    private static StateController instance = new StateController();

    private ArrayList<OGVenue> mAllVenues = new ArrayList<>();

    private ArrayList<VenueCollection> mMyVenues = new ArrayList<>();

    private ArrayList<OGVenue> mOwnedVenues = new ArrayList<>();

    private ArrayList<OGVenue> mManagedVenues = new ArrayList<>();

    public Location latestLocation;

    /**
     * Updates the venue arrays when it receives a notification.
     */
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            OGCloud.HttpCallback cb = new OGCloud.HttpCallback() {
                @Override
                public void onSuccess(Response response) {

                }

                @Override
                public void onFailure(Call call, IOException e, OGCloud.OGCloudError error) {

                }
            };

            findAllVenues(cb);
            findMyVenues(cb);
        }
    };

    private SingleShotLocationProvider.LocationCallback locationCallback = new SingleShotLocationProvider.LocationCallback() {
        @Override
        public void onLocationAvailable(Location location) {
            Log.d(TAG, "Got a location update");
            latestLocation = location;
        }

        @Override
        public void onLocationFailure() {
            Log.d(TAG, "Failed getting location update");
        }
    };

    private StateController() {
        // register to receive notifications when a venue is added
        LocalBroadcastManager.getInstance(BourbonApplication.getContext())
                .registerReceiver(mBroadcastReceiver,
                        new IntentFilter(BourbonNotification.addedVenue.name()));

        // register to receive notifications when the network changed
        LocalBroadcastManager.getInstance(BourbonApplication.getContext())
                .registerReceiver(mBroadcastReceiver,
                        new IntentFilter(BourbonNotification.networkChanged.name()));


    }

    public static StateController getInstance() {
        return instance;
    }

    public ArrayList<OGVenue> getAllVenues() {

        // Sort by location distance if we have a location
        if (latestLocation!=null){

             Collections.sort(mAllVenues, new Comparator<OGVenue>() {
                 @Override
                 public int compare(OGVenue t2, OGVenue t1) {

                     Location t2loc = new Location("");
                     t2loc.setLatitude(t2.latitude);
                     t2loc.setLongitude(t2.longitude);

                     Location t1loc = new Location("");
                     t1loc.setLatitude(t1.latitude);
                     t1loc.setLongitude(t1.longitude);

                     return t1loc.distanceTo(latestLocation) >= t2loc.distanceTo(latestLocation) ? -1:1;
                 }
             });

        }

        return mAllVenues;

    }

    public ArrayList<VenueCollection> getMyVenues() {
        return mMyVenues;
    }

    public ArrayList<OGVenue> getOwnedVenues() {
        return mOwnedVenues;
    }

    public ArrayList<OGVenue> getManagedVenues() {
        return mManagedVenues;
    }

    public void findAllVenues(OGCloud.HttpCallback cb) {
        Log.d(TAG, "findAllVenues");
        SingleShotLocationProvider lp = new SingleShotLocationProvider();
        lp.requestSingleUpdate(BourbonApplication.getContext(), locationCallback);
        OGCloud.getInstance().getVenues(BourbonApplication.getContext(), wrapAllVenuesCb(cb));
    }

    public void findMyVenues(OGCloud.HttpCallback cb) {
        Log.d(TAG, "findMyVenues");
        OGCloud.getInstance().getUserVenues(BourbonApplication.getContext(), wrapMyVenuesCb(cb));
    }

    private ArrayList<OGVenue> processVenues(JSONArray venueJson) {
        ArrayList<OGVenue> venues = new ArrayList<>();

        for (int i = 0; i < venueJson.length(); i++) {
            try {
                JSONObject venue = venueJson.getJSONObject(i);
                JSONObject addr = venue.getJSONObject("address");
                JSONObject geoLoc = venue.getJSONObject("geolocation");

                String name = venue.getString("name");
                String uuid = venue.getString("uuid");
                String street = addr.getString("street");
                String city = addr.getString("city");
                String state = addr.getString("state");
                String zip = addr.getString("zip");
                double lat = geoLoc.getDouble("latitude");
                double lng = geoLoc.getDouble("longitude");

                OGVenue ogVenue = new OGVenue(name, street, city, state, zip, lat, lng, uuid);

                // TODO: test that this works under all conditions
                // check for the optional values
                try {
                    ogVenue.address2 = addr.getString("street2");
                } finally {
                    try {
                        ogVenue.yelpId = venue.getString("yelpId");
                    } finally {
                        venues.add(ogVenue);
                    }
                }

            } catch (JSONException e) {
                Log.e(TAG, "found venue with missing info");
            }
        }

        // sort in alphabetical order
        Collections.sort(venues, new Comparator<OGVenue>() {
            @Override
            public int compare(OGVenue lhs, OGVenue rhs) {
                return lhs.name.compareTo(rhs.name);
            }
        });

        return venues;
    }

    private OGCloud.HttpCallback wrapAllVenuesCb(final OGCloud.HttpCallback cb) {
        return new OGCloud.HttpCallback() {
            @Override
            public void onSuccess(Response response) {
                try {
                    mAllVenues = processVenues(new JSONArray(response.body().string()));
                    BourbonNotification.allVenuesUpdated.issue(BourbonApplication.getContext());
                    cb.onSuccess(response);

                } catch (IOException | JSONException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                    cb.onFailure(null, null, OGCloud.OGCloudError.jsonError);

                } finally {
                    response.body().close();
                }
            }

            @Override
            public void onFailure(Call call, IOException e, OGCloud.OGCloudError error) {
                cb.onFailure(call, e, error);
            }
        };
    }

    private OGCloud.HttpCallback wrapMyVenuesCb(final OGCloud.HttpCallback cb) {
        return new OGCloud.HttpCallback() {
            @Override
            public void onSuccess(Response response) {
                try {
                    JSONObject venuesObj = new JSONObject(response.body().string());

                    mOwnedVenues = processVenues(venuesObj.getJSONArray("owned"));
                    mManagedVenues = processVenues(venuesObj.getJSONArray("managed"));

                    mMyVenues = new ArrayList<>();
                    mMyVenues.add(new VenueCollection("Owned", mOwnedVenues));
                    mMyVenues.add(new VenueCollection("Managed", mManagedVenues));

                    BourbonNotification.myVenuesUpdated.issue(BourbonApplication.getContext());
                    cb.onSuccess(response);

                } catch (IOException | JSONException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                    cb.onFailure(null, null, OGCloud.OGCloudError.jsonError);

                } finally {
                    response.body().close();
                }
            }

            @Override
            public void onFailure(Call call, IOException e, OGCloud.OGCloudError error) {
                cb.onFailure(call, e, error);
            }
        };
    }

    public class VenueCollection {
        public String label;
        public ArrayList<OGVenue> venues;

        VenueCollection(String label, ArrayList<OGVenue> venues) {
            this.label = label;
            this.venues = venues;
        }
    }
}
