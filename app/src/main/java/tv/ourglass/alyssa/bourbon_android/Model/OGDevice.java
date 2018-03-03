package tv.ourglass.alyssa.bourbon_android.Model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import tv.ourglass.alyssa.bourbon_android.BourbonApplication;

/**
 * Ourglass device.
 * Created by atorres on 11/8/16.
 */

public class OGDevice {

    public String name = "";
    public String atVenueUUID = "";
    public String udid = "";
    public String stationName = "";
    public Boolean isActive = false;
    public Date lastContact = null;

    public OGDevice(JSONObject deviceJson) throws JSONException {
        this.name = deviceJson.optString("name", "No Name");
        this.atVenueUUID = deviceJson.optString("atVenueUUID", "no-venue-uuid");
        this.udid = deviceJson.optString("deviceUDID", "no-device-uuid");
        JSONObject cp = deviceJson.optJSONObject("currentProgram");
        if (cp!=null){
            this.stationName = cp.optString("networkName", "No Network Info");
        } else {
            this.stationName = "No Station Info";
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
        Log.d("OGDevice", deviceJson.optString("lastContact"));
        try {
            this.lastContact = dateFormat.parse(deviceJson.getString("lastContact"));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //long interval = this.lastContact == null ? 1000000 : System.currentTimeMillis() - this.lastContact.getTime();
        //this.isActive = interval < TimeUnit.SECONDS.toMillis(300);
        this.isActive = true;
    }

    public String getUrl() {
        return OGConstants.belliniDM + OGConstants.deviceControlPath + this.udid + "&jwt=" +
                SharedPrefsManager.getJwt(BourbonApplication.getContext());
    }
}
