package tv.ourglass.alyssa.bourbon_android.Networking;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by mkahn on 1/31/18.
 */

public abstract class HttpCallback {
    abstract public void onSuccess(Response response); // this must close the response body or resources will leak
    abstract public void onFailure(Call call, IOException e, OGCloud2.OGCloudError error);
}
