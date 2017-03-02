package tv.ourglass.alyssa.bourbon_android.Scenes.Control;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import tv.ourglass.alyssa.bourbon_android.Models.OGConstants;
import tv.ourglass.alyssa.bourbon_android.Networking.NetUtils;
import tv.ourglass.alyssa.bourbon_android.Networking.OGDPService;
import tv.ourglass.alyssa.bourbon_android.R;

public class DevicesFragment extends Fragment {

    String TAG = "DevicesFragment";

    ArrayList<OGDevice> devices = new ArrayList<>();

    DevicesListAdapter devicesListAdapter;

    TextView mNetworkName;

    private BroadcastReceiver mWifiBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "got network change!");

            if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)) {
                mNetworkName.setText(R.string.notConnected);

            } else {
                mNetworkName.setText(NetUtils.getCurrentSSID(context));
            }
        }
    };

    private BroadcastReceiver mBroadcastRcvr = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "received OGDP broadcast");
            devices.clear();
            devices.addAll(OGDPService.getInstance().devices);
            devicesListAdapter.notifyDataSetChanged();
        }
    };

    /*private ArrayList<OGDevice> test_devices = new ArrayList<OGDevice>() {{
        add(new OGDevice("System name", "location", "0.0.0.0", "venue", 6));
    }};*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_devices, container, false);

        // Set network name
        mNetworkName = (TextView)view.findViewById(R.id.networkName);
        mNetworkName.setText(NetUtils.getCurrentSSID(getContext()));

        // Get devices
        this.devicesListAdapter = new DevicesListAdapter(getActivity(), this.devices);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadcastRcvr,
                new IntentFilter("ogdp"));

        // Attach the adapter to a ListView
        ListView listView = (ListView) view.findViewById(R.id.devicesList);
        listView.setAdapter(devicesListAdapter);

        // Attach text to display when list is empty
        listView.setEmptyView(view.findViewById(R.id.empty));

        // Change network name display if needed
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        getActivity().registerReceiver(mWifiBroadcastReceiver, intentFilter);

        return view;
    }
}