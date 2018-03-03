package tv.ourglass.alyssa.bourbon_android.Scenes.Control;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import java.util.HashMap;
import java.util.Map;

import tv.ourglass.alyssa.bourbon_android.Model.SharedPrefsManager;
import tv.ourglass.alyssa.bourbon_android.R;

/**
 * Created by atorres on 4/5/17.
 */

public class WebViewBaseFragment extends Fragment {

    String TAG = "WebViewBaseFragment";

    Activity mActivity;
    String targetUrlString;
    String title;
    WebViewClient webViewClient;
    Boolean timeout;
    Long webViewTimeout = 30000l;

    ProgressBar mSpinner;
    WebView webview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_device_view2, container, false);
        mSpinner = (ProgressBar) view.findViewById(R.id.webviewSpinner);

        mActivity = getActivity();

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setTitle(title);

        webview = (WebView) view.findViewById(R.id.webview);
        webview.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.OGBackgroundGrey));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webview.setWebContentsDebuggingEnabled(true);
        }

        // Configure web view
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setBuiltInZoomControls(false);
        webview.getSettings().setDomStorageEnabled(true);
        timeout = true;

        // show alert on error loading page
        webview.setWebViewClient(webViewClient);

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Bearer " + SharedPrefsManager.getJwt(mActivity));
        headers.put("X-OG-Mobile", "OGHomey");

        webview.loadUrl(targetUrlString, headers);

        return view;
    }

    public WebResourceResponse getNewResponse(String url) {
        /*try {
            Request request = new Request.Builder()
                    .url(url.trim())
                    .addHeader("Authorization", "Bearer " + SharedPrefsManager.getJwt(mActivity))
                    .addHeader("X-OG-Mobile", "OGHomey")
                    .build();

            Response response = BourbonApplication.okclient.newCall(request).execute();
            getMimeType(url);
            return new WebResourceResponse(
                    response.header("Content-Type", "text/plain").split(";")[0],
                    response.header("content-encoding", "utf-8"),
                    response.body().byteStream());

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }*/
        return null;
    }

    private String getMimeType(String url) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        Log.d(TAG, "url: " + url);
        Log.d(TAG, "extension: " + (extension == null ? "null" : extension));
        switch (extension) {
            case "js":
                return "text/javascript";
            case "woff":
                return "application/font-woff";
            case "woff2":
                return "application/font-woff2";
            case "ttf":
                return "application/x-font-ttf";
            case "eot":
                return "application/vnd.ms-fontobject";
            case "svg":
                return "image/svg+xml";
            default:
                String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                Log.d(TAG, "type: " + (type == null ? "null" : type));
                return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
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
                        getActivity().onBackPressed();
                    }
                });
        alert.show();
    }
}
