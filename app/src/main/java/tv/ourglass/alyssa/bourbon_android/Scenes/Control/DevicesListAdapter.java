package tv.ourglass.alyssa.bourbon_android.Scenes.Control;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import tv.ourglass.alyssa.bourbon_android.BourbonApplication;
import tv.ourglass.alyssa.bourbon_android.Model.OGDevice;
import tv.ourglass.alyssa.bourbon_android.R;
import tv.ourglass.alyssa.bourbon_android.Scenes.Tabs.MainTabsActivity;

/**
 * Created by atorres on 11/8/16.
 */

public class DevicesListAdapter extends ArrayAdapter<OGDevice> {

    String TAG = "DevicesListAdapter";

    private Context context;

    public DevicesListAdapter(Context context, ArrayList<OGDevice> devices) {
        super(context, 0, devices);
        this.context = context;
    }

    @Override
    @NonNull
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        OGDevice device = getItem(position);

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.device_cell_layout, parent, false);
        }

        if (device != null) {
            //((TextView) view.findViewById(R.id.number)).setText(String.format(Locale.ENGLISH, "%02d", position + 1));
            // myImgView.setImageDrawable(getResources().getDrawable(R.drawable.monkey, getApplicationContext().getTheme()));


            ((TextView) view.findViewById(R.id.name)).setText(device.name);

            if (device.isActive) {
                ((TextView) view.findViewById(R.id.station)).setText(device.stationName);
                ((ImageView) view.findViewById(R.id.imageViewTV))
                        .setImageDrawable(BourbonApplication.getContext()
                                .getResources()
                                //.getDrawable(R.drawable.ic_tv_green_24dp));
                                .getDrawable(R.drawable.ic_tv_green_24dp));
                ((ImageView) view.findViewById(R.id.imageViewWarning)).setVisibility(View.INVISIBLE);
            } else {
                ((TextView) view.findViewById(R.id.station)).setText(R.string.offline);
                ((ImageView) view.findViewById(R.id.imageViewTV))
                        .setImageDrawable(BourbonApplication.getContext()
                                .getResources()
                                .getDrawable(R.drawable.ic_tv_red_24dp));
                ((ImageView) view.findViewById(R.id.imageViewWarning)).setVisibility(View.VISIBLE);

            }
        }

        view.setTag(device);

        // go to device control view when a device is selected
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OGDevice device = (OGDevice) view.getTag();
                if (device != null) {
                    ((MainTabsActivity) context)
                            .openNewFragment(DeviceViewFragment.newInstance(device.name, device.getUrl()));
                } else {
                    Log.e(TAG, "selected a device that is null");
                }
            }
        });

        return view;

    }
}
