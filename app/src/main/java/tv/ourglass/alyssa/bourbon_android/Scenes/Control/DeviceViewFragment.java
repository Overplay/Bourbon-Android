package tv.ourglass.alyssa.bourbon_android.Scenes.Control;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import tv.ourglass.alyssa.bourbon_android.Model.OGConstants;
import tv.ourglass.alyssa.bourbon_android.R;
import tv.ourglass.alyssa.bourbon_android.Scenes.Tabs.MainTabsActivity;

public class DeviceViewFragment extends WebViewBaseFragment {

    String TAG = "DeviceViewFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();

        // set target url and display title
        if (args != null) {

            if (args.containsKey(OGConstants.deviceNameExtra)) {
                title = args.getString(OGConstants.deviceNameExtra);
            }
            if (args.containsKey(OGConstants.deviceUrlExtra)) {
                targetUrlString = args.getString(OGConstants.deviceUrlExtra);
            }

        } else {
            title = "";
            targetUrlString = "";
        }

        webViewClient = new WebViewClient() {

            @TargetApi(android.os.Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                view.setVisibility(View.INVISIBLE);
                showAlert(getString(R.string.uhoh), error.getDescription().toString());
            }

            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                view.setVisibility(View.INVISIBLE);
                showAlert(getString(R.string.uhoh), description);
            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();

                if (url.contains(OGConstants.appControlPath)) {
                    ((MainTabsActivity) getActivity())
                            .openNewFragment(AppControlViewFragment.newInstance(url));
                }
                return getNewResponse(url);
            }

            @SuppressWarnings("deprecation")
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                if (url.contains(OGConstants.appControlPath)) {
                    ((MainTabsActivity) getActivity())
                            .openNewFragment(AppControlViewFragment.newInstance(url));
                }
                return getNewResponse(url);
            }

            // start timing to detect timeout
            @Override
            public void onPageStarted(final WebView view, String url, Bitmap favicon) {
                mSpinner.setVisibility(View.VISIBLE);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(webViewTimeout);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (timeout) {
                            showAlert(getString(R.string.uhoh), "We were unable to connect to this device.");
                        }
                    }
                }).start();
            }

            // end timeout
            @Override
            public void onPageFinished(WebView view, String url) {
                timeout = false;
                mSpinner.setVisibility(View.INVISIBLE);
            }

        };
    }

    public static DeviceViewFragment newInstance(String deviceName, String deviceUrl) {
        DeviceViewFragment deviceViewFragment = new DeviceViewFragment();

        Bundle args = new Bundle();
        args.putString(OGConstants.deviceNameExtra, deviceName);
        args.putString(OGConstants.deviceUrlExtra, deviceUrl);

        deviceViewFragment.setArguments(args);

        return deviceViewFragment;
    }
}
