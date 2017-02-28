package tv.ourglass.alyssa.bourbon_android.Networking;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONObject;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import tv.ourglass.alyssa.bourbon_android.Models.OGConstants;
import tv.ourglass.alyssa.bourbon_android.Scenes.Control.OGDevice;

/**
 * Created by mitch on 11/10/16.
 * Does ogdp discovery.
 *
 */

public class OGDPService extends Service implements OGDPListenHandlerThread.OGDPListenerListener {

    public static final String TAG = "OGDPService";
    public static final int OGDP_PORT = OGConstants.udpDiscoveryPort;

    private static OGDPService sService;

    // Provide static/singleton instance. This can be null if Service has not been created!
    public static OGDPService getInstance(){
        return sService;
    }

    // handlerThread that only sends out the inquiry packet
    private OGDPPingHandlerThread mOGDPDiscoveryThread;
    private OGDPListenHandlerThread mListenerThread;

    public ArrayList<OGDevice> devices = new ArrayList<>();

    public HashMap<String, JSONObject> allOGs = new HashMap<>();
    public HashSet<String> allOGAddresses = new HashSet<>();
    public Exception lastException;

    private DatagramSocket mSocket;

    private Timer mTimer = new Timer();

    // Stock stuff that needs to be here for all services

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        sService = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        processIntent(intent);
        return Service.START_STICKY;

    }

    private void processIntent(Intent intent){

        // This function is here in case there are some optional params passed in the intent
        // Like they are in the AmstelBright SSDPDisco service this is based on.

        discover();

    }

    private void prepSocket() throws SocketException {

        if (mSocket==null){
            mSocket = new DatagramSocket(null);
            mSocket.setReuseAddress(true);
            mSocket.setBroadcast(true);
            mSocket.bind(new InetSocketAddress(OGDP_PORT));
        }

    }

    private void prepPingThread(){

        if (mOGDPDiscoveryThread==null){
            mOGDPDiscoveryThread = new OGDPPingHandlerThread("ssdpdicsoping");
            mOGDPDiscoveryThread.start(this, mSocket);
        }

    }

    private void prepListenerThread(){

        if (mListenerThread==null){
            mListenerThread = new OGDPListenHandlerThread("ssdpdiscolisten");
            mListenerThread.start(this, mSocket, this);
        }
    }


    // Triggers a discovery pass
    public void discover(){

        try {
            prepSocket();
            prepListenerThread();
            prepPingThread();

        } catch (SocketException e) {
            e.printStackTrace();
        }

        mListenerThread.listen(15000);  // start listening
        mTimer.schedule(new DecrementTTL(), OGConstants.ttlInterval, OGConstants.ttlInterval);
        mOGDPDiscoveryThread.discover(); // send ping and sit back and chill

    }


    public void onDestroy() {

        Log.d(TAG, "onDestroy");

        if (mOGDPDiscoveryThread!=null)
            mOGDPDiscoveryThread.quit();

        if (mListenerThread!=null){
            mSocket.close();
            mListenerThread.quit();
        }

    }

    private void notifyNewDevices(){

        Intent intent = new Intent();
        intent.setAction("ogdp");
        intent.putExtra("devices", devices);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }

    private void notifyError(String message){

        Intent intent = new Intent();
        intent.setAction("ogdp");
        intent.putExtra("error", message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }

    private class DecrementTTL extends TimerTask {
        public void run() {
            Iterator<OGDevice> itr = devices.iterator();
            Log.d(TAG, "decrementing ttl");
            boolean drop = false;
            OGDevice d;

            while (itr.hasNext()) {

                d = itr.next();
                d.ttl -= 1;

                if (d.ttl <= 0) {
                    drop = true;
                    itr.remove();
                }
            }

            if (drop) {
                notifyNewDevices(); // indicate there was a change
            }
        }
    }

    @Override
    public void processDevice(OGDevice device) {

        Log.d(TAG, "processing device");

        for (OGDevice og : this.devices) {

            if (!OGConstants.devMode) {
                if (og.ipAddress.equals(device.ipAddress)) {
                    og.systemName = device.systemName;
                    og.location = device.location;
                    og.venue = device.venue;
                    og.ttl = OGConstants.maxTTL;
                    notifyNewDevices();
                    return;
                }
            }

            if (OGConstants.devMode) {
                if (og.systemName.equals(device.systemName)) {
                    og.location = device.location;
                    og.venue = device.venue;
                    og.ttl = OGConstants.maxTTL;
                    notifyNewDevices();
                    return;
                }
            }
        }

        device.ttl = OGConstants.maxTTL;
        this.devices.add(device);
        notifyNewDevices();
    }


    @Override
    public void devicesFound(HashMap<String, JSONObject> devices) {
        Log.d( TAG, "Found "+ devices.keySet().size()+" devices");
        allOGs = devices;
        allOGAddresses = new HashSet<>(devices.keySet());
        notifyNewDevices();
    }

    @Override
    public void error(Exception e) {
        Log.e( TAG, "Error discovering OGs");
        lastException = e;
        notifyError(e.getMessage());
    }
}