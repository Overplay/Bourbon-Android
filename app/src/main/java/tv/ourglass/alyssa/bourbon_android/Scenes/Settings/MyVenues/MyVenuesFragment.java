package tv.ourglass.alyssa.bourbon_android.Scenes.Settings.MyVenues;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;
import tv.ourglass.alyssa.bourbon_android.Model.BourbonNotification;
import tv.ourglass.alyssa.bourbon_android.Model.OGVenue.OGVenue;
import tv.ourglass.alyssa.bourbon_android.Model.OGVenue.OGVenueListAdapter;
import tv.ourglass.alyssa.bourbon_android.Model.OGVenue.OGVenueType;
import tv.ourglass.alyssa.bourbon_android.Model.StateController;
import tv.ourglass.alyssa.bourbon_android.Networking.OGCloud;
import tv.ourglass.alyssa.bourbon_android.R;
import tv.ourglass.alyssa.bourbon_android.Scenes.Control.ChooseDeviceFragment;
import tv.ourglass.alyssa.bourbon_android.Scenes.Tabs.MainTabsActivity;

/**
 * Created by atorres on 5/10/17.
 */

public class MyVenuesFragment extends Fragment {
    final String TAG = "MyVenuesFragment";

    OGVenueListAdapter mVenueListAdapter;

    BroadcastReceiver mBroadcastReceiver;

    ListView mVenueList;

    View mEmptyView;

    ProgressBar mProgressSpinner;

    OGCloud.HttpCallback venueCallback = new OGCloud.HttpCallback() {

        @Override
        public void onSuccess(final Response response) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mVenueListAdapter.notifyDataSetChanged();
                    mVenueList.setEmptyView(mEmptyView);
                    mProgressSpinner.setVisibility(View.GONE);
                }
            });
            response.body().close();
        }

        @Override
        public void onFailure(Call call, final IOException e, OGCloud.OGCloudError error) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), "Error retrieving venues", Toast.LENGTH_SHORT).show();
                    mVenueList.setEmptyView(mEmptyView);
                    mProgressSpinner.setVisibility(View.GONE);
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
                        mVenueListAdapter.notifyDataSetChanged();
                    }
                });
            }
        };

        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(mBroadcastReceiver,
                        new IntentFilter(BourbonNotification.myVenuesUpdated.name()));

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pick_venue, container, false);

        // set up top toolbar
        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.tb_my_venues));
        ((MainTabsActivity) getActivity()).setSupportActionBar(toolbar);

        // set up venue list
        mVenueListAdapter = new OGVenueListAdapter(getActivity(), OGVenueType.MINE,
                new OGVenueListAdapter.OnClickVenue() {
                    @Override
                    public void onClick(View view, OGVenue venue) {
                        if (venue != null) {
                            ((MainTabsActivity) getActivity()).openNewFragment(
                                    ChooseDeviceFragment.newInstance(venue.name, venue.uuid));
                        }
                    }
                });
        mVenueList = (ListView) rootView.findViewById(R.id.venueList);
        mVenueList.setAdapter(mVenueListAdapter);
        mEmptyView = rootView.findViewById(R.id.empty);
        mProgressSpinner = (ProgressBar) rootView.findViewById(R.id.progress);
        mProgressSpinner.setVisibility(View.VISIBLE);

        // find venues
        StateController.getInstance().findMyVenues(venueCallback);

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        // set to null to remove any previously selected Yelp venue
        ((MainTabsActivity) getActivity()).setSelectedYelpVenue(null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        menu.findItem(R.id.action_add).setVisible(true);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                //((MainTabsActivity) getActivity()).openNewFragment(new CreateVenueFragment());
                warn();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void warn() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Contact Us")
                .setMessage("Contact Ourglass Support to add a new venue.")
                .setPositiveButton("OK", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }

}

