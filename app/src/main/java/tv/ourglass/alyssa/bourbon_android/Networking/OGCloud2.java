package tv.ourglass.alyssa.bourbon_android.Networking;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.crashlytics.android.answers.LoginEvent;
import com.crashlytics.android.answers.SignUpEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tv.ourglass.alyssa.bourbon_android.BourbonApplication;
import tv.ourglass.alyssa.bourbon_android.Model.BourbonNotification;
import tv.ourglass.alyssa.bourbon_android.Model.OGConstants;
import tv.ourglass.alyssa.bourbon_android.Model.OGVenue.OGVenue;
import tv.ourglass.alyssa.bourbon_android.Model.SharedPrefsManager;
import tv.ourglass.alyssa.bourbon_android.Scenes.Registration.WelcomeActivity;

/**
 * Performs HTTP requests to OGCloud endpoints.
 * Created by atorres on 5/30/17.
 */
public class OGCloud2 {

    public static final String TAG = "OGCloud2";

    private static final OkHttpClient client = BourbonApplication.okclient;

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    /**
     * Error type for OGCloud HTTP requests.
     */
    public enum OGCloudError {
        authFailure, tokenInvalid, jsonError, defaultError
    }


    /**
     * Makes an HTTP request with OkHttp.
     *
     * @param request the request
     * @param cb the callback to process the response
     * @param context the context to use
     */
    static void request(Request request, final HttpCallback cb, final Context context) {

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                cb.onFailure(call, e, OGCloudError.defaultError);
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {

                if (!response.isSuccessful()) {

                    // if error 403, then check authorization
                    if (response.code() == 403) {

                        checkJWT(context, new HttpCallback() {
                            @Override
                            public void onSuccess(Response jwtResponse) {
                                // this resource is not allowed for this user
                                jwtResponse.body().close();
                                cb.onFailure(call, null, OGCloudError.authFailure);
                            }
                            @Override
                            public void onFailure(Call jwtCall, IOException e, OGCloudError error) {
                                // this user's JWT is invalid
                                cb.onFailure(jwtCall, e, OGCloudError.tokenInvalid);
                            }
                        });

                    } else {
                        cb.onFailure(call, null, OGCloudError.defaultError);
                    }
                    response.body().close();

                } else { // put response in a JSONObject
                    cb.onSuccess(response);
                }
            }
        });
    }

    /**
     * Performs a GET request without the authorization check.
     *
     * @param context the context to use
     * @param url the URL
     * @param cb the callback to process the response
     */
    private static void getWithoutAuthCheck(Context context, String url, final HttpCallback cb) {
        Request req;
        String jwt = SharedPrefsManager.getJwt(context);

        if (jwt != null) {
            req = new Request.Builder()
                    .addHeader("Authorization", "Bearer " + jwt)
                    .url(url)
                    .get()
                    .build();
        } else {
            req = new Request.Builder()
                    .url(url)
                    .get()
                    .build();
        }

        client.newCall(req).enqueue(new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    response.body().close();
                    cb.onFailure(call, null, OGCloudError.defaultError);

                } else { // put response in a JSONObject
                    cb.onSuccess(response);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                cb.onFailure(call, e, OGCloudError.defaultError);
            }
        });
    }

    /**
     * Performs a GET request.
     *
     * @param context the context to use
     * @param url the URL
     * @param cb the callback to process the response
     */
    public static void get(Context context, String url, HttpCallback cb) {
        Request req;
        String jwt = SharedPrefsManager.getJwt(context);

        if (jwt != null) {
            req = new Request.Builder()
                    .addHeader("Authorization", "Bearer " + jwt)
                    .url(url)
                    .get()
                    .build();
        } else {
            req = new Request.Builder()
                    .url(url)
                    .get()
                    .build();
        }

        request(req, cb, context);
    }

    /**
     * Performs a POST with JSON parameters.
     *
     * @param context the context to use
     * @param url the URL
     * @param params the parameters
     * @param cb the callback to process the response
     */
    private static void postJson(Context context, String url, JSONObject params, HttpCallback cb) {

        String jsonStr = params.toString();
        RequestBody body = RequestBody.create(JSON, jsonStr);
        Request req;
        String jwt = SharedPrefsManager.getJwt(context);

        if (jwt != null) {
            req = new Request.Builder()
                    .addHeader("Authorization", "Bearer " + jwt)
                    .url(url)
                    .post(body)
                    .build();
        } else {
            req = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
        }

        request(req, cb, context);
    }

    /**
     * Performs a PUT with JSON parameters.
     *
     * @param context the context to use
     * @param url the URL
     * @param params the parameters
     * @param cb the callback to process the response
     */
    public static void putJson(Context context, String url, JSONObject params, HttpCallback cb) {

        String jsonStr = params.toString();
        RequestBody body = RequestBody.create(JSON, jsonStr);
        Request req;
        String jwt = SharedPrefsManager.getJwt(context);

        if (jwt != null) {
            req = new Request.Builder()
                    .addHeader("Authorization", "Bearer " + jwt)
                    .url(url)
                    .put(body)
                    .build();
        } else {
            req = new Request.Builder()
                    .url(url)
                    .put(body)
                    .build();
        }

        request(req, cb, context);
    }

    /**
     * Logs in to OurGlass, gets a token, and sets `SharedPrefsManager` user variables.
     * @param context the context to use
     * @param email user's email
     * @param password user's password
     * @param cb the callback to process the response
     */
    public static void login(final Context context, final String email, final String password,
                      final HttpCallback cb) {


        try {
            JSONObject params = new JSONObject();
            params.put("email", email);
            params.put("password", password);
            params.put("type", "local");
            params.put("responseType", "jwt"); // newly added feature in Mitch's Waterlock


            HttpCallback loginCb = new HttpCallback() {
                @Override
                public void onSuccess(final Response loginResponse) {
                    try {
                        String jsonData = loginResponse.body().string();
                        JSONObject json = new JSONObject(jsonData);
                        SharedPrefsManager.setJwt(context, json.getString("token"));
                        SharedPrefsManager.setJwtExpiry(context, json.getLong("expires"));
                        // userinfo for fabric set in checkJwt method
                        Answers.getInstance().logLogin(new LoginEvent()
                                .putMethod("email")
                                .putSuccess(true));
                        cb.onSuccess(loginResponse);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d(TAG, "unable to get necessary token info");
                        cb.onFailure(null, null, OGCloudError.jsonError);
                    }

                }
                @Override
                public void onFailure(Call call, IOException e, OGCloudError error) {
                    Answers.getInstance().logLogin(new LoginEvent()
                            .putMethod("email")
                            .putSuccess(false));
                    cb.onFailure(call, e, error);
                }
            };

            postJson(context, OGConstants.belliniCore + OGConstants.loginPath, params, loginCb);

        } catch (JSONException e) {
            e.printStackTrace();
            cb.onFailure(null, null, OGCloudError.jsonError);
        }
    }

    /**
     * Logs in to OurGlass without getting a token.
     * @param context the context to use
     * @param email user's email
     * @param password user's password
     * @param cb the callback to process the response
     */
    public static void loginOnly(final Context context, final String email, final String password,
                      final HttpCallback cb) {
        try {
            JSONObject params = new JSONObject();
            params.put("email", email);
            params.put("password", password);
            params.put("type", "local");

            postJson(context, OGConstants.belliniCore + OGConstants.loginPath, params, cb);

        } catch (JSONException e) {
            e.printStackTrace();
            cb.onFailure(null, null, OGCloudError.jsonError);
        }
    }

    /**
     * Registers a new user, gets a token, and sets `SharedPrefsManager` user variables.
     *
     * @param context the context to use
     * @param email new user's email
     * @param password new user's password
     * @param firstName new user's first name
     * @param lastName  new user's last name
     * @param cb the callback to process the response
     */
    public static void register(final Context context, final String email, final String password,
                         final String firstName, final String lastName, final HttpCallback cb) {
        try {
            JSONObject params = new JSONObject();
            params.put("email", email);
            params.put("password", password);
            params.put("type", "local");


            JSONObject user = new JSONObject();
            user.put("firstName", firstName);
            user.put("lastName", lastName);
            params.put("user", user);

            HttpCallback registerCb = new HttpCallback() {
                @Override
                public void onSuccess(final Response regResponse) {


                }
                @Override
                public void onFailure(Call call, IOException e, OGCloudError error) {
                    cb.onFailure(call, e, error);
                }
            };

            postJson(context, OGConstants.belliniCore + OGConstants.registerPath, params, registerCb);

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "error creating JSON object for registration");
            cb.onFailure(null, null, OGCloudError.jsonError);
        }
    }

    public static void registerAndLogin(final Context context, final String email, final String password,
                                        final String firstName, final String lastName, final HttpCallback cb){


        try {
            JSONObject params = new JSONObject();
            params.put("email", email);
            params.put("password", password);
            params.put("type", "local");


            JSONObject user = new JSONObject();
            user.put("firstName", firstName);
            user.put("lastName", lastName);
            params.put("user", user);

            HttpCallback registerCb = new HttpCallback() {
                @Override
                public void onSuccess(final Response regResponse) {
                    login(context, email, password, new HttpCallback() {
                        @Override
                        public void onSuccess(Response tokenResponse) {
                            Answers.getInstance().logSignUp(new SignUpEvent()
                                    .putMethod("email")
                                    .putSuccess(true));

                           cb.onSuccess(tokenResponse);
                        }
                        @Override
                        public void onFailure(Call call, IOException e, OGCloudError error) {
                            Answers.getInstance().logSignUp(new SignUpEvent()
                                    .putMethod("email")
                                    .putSuccess(false));
                            cb.onFailure(call, e, error);
                        }
                    });
                }
                @Override
                public void onFailure(Call call, IOException e, OGCloudError error) {
                    cb.onFailure(call, e, error);
                }
            };

            postJson(context, OGConstants.belliniCore + OGConstants.registerPath, params, registerCb);

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "error creating JSON object for registration");
            cb.onFailure(null, null, OGCloudError.jsonError);
        }







    }

    /**
     * Changes the password of the user.
     *
     * @param context the context to use
     * @param email user's email
     * @param newPassword new password
     * @param cb the callback to process the response
     */
    public static void changePassword(final Context context, String email, final String newPassword,
                               final HttpCallback cb) {
        try {
            JSONObject params = new JSONObject();
            params.put("email", email);
            params.put("newpass", newPassword);

            postJson(context, OGConstants.belliniCore + OGConstants.changePasswordPath, params, cb);

        } catch (JSONException e) {
            e.printStackTrace();
            cb.onFailure(null, null, OGCloudError.jsonError);
        }
    }

    /**
     * Changes the account information of a user.
     *
     * @param context the context to use
     * @param firstName new first name
     * @param lastName new last name
     * @param email new email
     * @param userId the user's id
     * @param cb the callback to process the response
     */
    public static void changeAccountInfo(final Context context, final String firstName, final String lastName,
                                  final String email, final String userId, final HttpCallback cb) {
        try {
            JSONObject params = new JSONObject();
            params.put("email", email);
            params.put("firstName", firstName);
            params.put("lastName", lastName);

            HttpCallback changeAcctCb = new HttpCallback() {
                @Override
                public void onSuccess(Response response) {
                    SharedPrefsManager.setUserFirstName(context, firstName);
                    SharedPrefsManager.setUserLastName(context, lastName);
                    SharedPrefsManager.setUserEmail(context, email);
                    BourbonApplication.setFabricUserInfo();
                    Answers.getInstance().logCustom(new CustomEvent("User Info Update")
                    .putCustomAttribute("success", 1));
                    cb.onSuccess(response);
                }
                @Override
                public void onFailure(Call call, IOException e, OGCloudError error) {
                    Answers.getInstance().logCustom(new CustomEvent("User Info Update")
                            .putCustomAttribute("success", 0));
                    cb.onFailure(call, e, error);
                }
            };

            putJson(context, OGConstants.belliniCore + OGConstants.changeAccountPath + userId, params, changeAcctCb);

        } catch (JSONException e) {
            e.printStackTrace();
            cb.onFailure(null, null, OGCloudError.jsonError);
        }
    }

    /**
     * Invites someone to OurGlass via email.
     *
     * @param context the context to use
     * @param email invitee's email
     * @param cb the callback to process the response
     */
    public static void inviteUser(Context context, String email, HttpCallback cb) {
        try {
            JSONObject params = new JSONObject();
            params.put("email", email);

            postJson(context, OGConstants.belliniCore + OGConstants.inviteUserPath, params, cb);

        } catch (JSONException e) {
            e.printStackTrace();
            cb.onFailure(null, null, OGCloudError.jsonError);
        }
    }

    /**
     * Gets a token.
     *
     * @param context the context to use
     * @param cb the callback to process the response
     */
    public static void getToken(final Context context, final HttpCallback cb) {
        HttpCallback getTokenCb = new HttpCallback() {
            @Override
            public void onSuccess(Response response) {
                try {
                    String jsonData = response.body().string();
                    JSONObject json = new JSONObject(jsonData);
                    SharedPrefsManager.setJwt(context, json.getString("token"));
                    SharedPrefsManager.setJwtExpiry(context, json.getLong("expires"));
                    cb.onSuccess(response);

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "unable to get necessary token info");
                    cb.onFailure(null, null, OGCloudError.jsonError);
                }
            }
            @Override
            public void onFailure(Call call, IOException e, OGCloudError error) {
                cb.onFailure(call, e, error);
            }
        };
        get(context, OGConstants.belliniCore + OGConstants.getTokenPath, getTokenCb);
    }

    /**
     * Checks the user's current JWT to see if it is still valid.
     *
     * @param context the context to use
     * @param cb the callback to process the response
     */
    public static void checkJWT(final Context context, final HttpCallback cb) {
        HttpCallback checkJWTCb = new HttpCallback() {
            @Override
            public void onSuccess(Response response) {
                try {
                    String jsonData = response.body().string();
                    JSONObject json = new JSONObject(jsonData);
                    SharedPrefsManager.setUserId(context, json.getString("id"));
                    SharedPrefsManager.setUserFirstName(context, json.getString("firstName"));
                    SharedPrefsManager.setUserLastName(context, json.getString("lastName"));
                    SharedPrefsManager.setUserEmail(context, json.getString("email"));
                    BourbonApplication.setFabricUserInfo();

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "unable to get all user info from check JWT");

                } finally {
                    cb.onSuccess(response);
                }
            }
            @Override
            public void onFailure(Call call, IOException e, OGCloudError error) {
                cb.onFailure(call, e, error);
            }
        };
        // cannot use same get() as everyone else or we might end up in a loop
        getWithoutAuthCheck(context, OGConstants.belliniCore + OGConstants.checkJWTPath, checkJWTCb);
    }

    /**
     * Gets all venues.
     *
     * @param context the context to use
     * @param cb the callback to process the response
     */
    public static void getVenues(Context context, HttpCallback cb) {
        get(context, OGConstants.belliniCore + OGConstants.venuesPath, cb);
    }

    /**
     * Gets the venues associated with the current user.
     *
     * @param context the context to use
     * @param cb the callback to process the response
     */
    public static void getUserVenues(Context context, HttpCallback cb) {
        get(context, OGConstants.belliniCore + OGConstants.userVenuesPath, cb);
    }

    /**
     * Gets the devices associated with a venue.
     *
     * @param context the context to use
     * @param venueUUID venue UUID
     * @param cb the callback to process the response
     */
    public static void getDevices(Context context, String venueUUID, HttpCallback cb) {
        get(context, OGConstants.belliniDM + OGConstants.devicesPath + venueUUID, cb);
    }

    /**
     * Finds a device by its registration code.
     *
     * @param context the context to use
     * @param regCode registration code
     * @param cb the callback to process the response
     */
    public static void findByRegCode(Context context, String regCode, HttpCallback cb) {
        get(context, OGConstants.belliniDM + OGConstants.findByRegCodePath + regCode, cb);
    }

    /**
     * Changes the name of the associated device.
     *
     * @param context the context to use
     * @param udid device UDID
     * @param name name to give the device
     * @param cb the callback to process the response
     */
    public static void changeDeviceName(final Context context, String udid, String name,
                                 final HttpCallback cb) {
        try {
            JSONObject params = new JSONObject();
            params.put("deviceUDID", udid);
            params.put("name", name);

            HttpCallback changeNameCb = new HttpCallback() {
                @Override
                public void onSuccess(Response response) {
                    BourbonNotification.updatedDevice.issue(context);
                    cb.onSuccess(response);
                }
                @Override
                public void onFailure(Call call, IOException e, OGCloudError error) {
                    cb.onFailure(call, e, error);
                }
            };

            postJson(context, OGConstants.belliniDM + OGConstants.changeDeviceNamePath, params, changeNameCb);

        } catch (JSONException e) {
            e.printStackTrace();
            cb.onFailure(null, null, OGCloudError.jsonError);
        }
    }

    /**
     * Associates the device with the venue.
     *
     * @param context the context to use
     * @param deviceUdid device UDID
     * @param venueUuid venue UUID
     * @param cb the callback to process the response
     */
    public static void associate(final Context context, String deviceUdid, String venueUuid,
                          final HttpCallback cb) {
        try {
            JSONObject params = new JSONObject();
            params.put("deviceUDID", deviceUdid);
            params.put("venueUUID", venueUuid);

            HttpCallback associateCb = new HttpCallback() {
                @Override
                public void onSuccess(Response response) {
                    BourbonNotification.updatedDevice.issue(context);
                    cb.onSuccess(response);
                }
                @Override
                public void onFailure(Call call, IOException e, OGCloudError error) {
                    cb.onFailure(call, e, error);
                }
            };

            postJson(context, OGConstants.belliniDM + OGConstants.associateWithVenuePath, params, associateCb);

        } catch (JSONException e) {
            e.printStackTrace();
            cb.onFailure(null, null, OGCloudError.jsonError);
        }
    }

    /**
     * Performs a Yelp search.
     *
     * @param context the context to use
     * @param term search term
     * @param location search location
     * @param cb the callback to process the response
     */
    public static void yelpSearch(Context context, String term, String location, HttpCallback cb) {
        String url = OGConstants.belliniCore + OGConstants.yelpSearchPath;
        url += "?location=" + location + "&term=" + term;
        get(context, url, cb);
    }

    /**
     * Performs a Yelp search.
     *
     * @param context the context to use
     * @param term search term
     * @param latitude latitude of the search location
     * @param longitude longitude of the search location
     * @param cb the callback to process the response
     */
    public static void yelpSearch(Context context, String term, double latitude, double longitude,
                           HttpCallback cb) {
        String url = OGConstants.belliniCore + OGConstants.yelpSearchPath;
        url += "?latitude=" + latitude + "&longitude=" + longitude + "&term=" + term;
        get(context, url, cb);
    }

    /**
     * Creates a new venue.
     *
     * @param context the context to use
     * @param venue the venue to create
     * @param cb the callback to process the response
     */
    public static void addVenue(final Context context, OGVenue venue, final HttpCallback cb) {
        try {
            JSONObject params = new JSONObject();
            params.put("name", venue.name);
            params.put("yelpId", venue.yelpId);

            JSONObject addr = new JSONObject();
            addr.put("street", venue.address1);
            addr.put("street2", venue.address2);
            addr.put("city", venue.city);
            addr.put("state", venue.state);
            addr.put("zip", venue.zip);
            params.put("address", addr);

            JSONObject geoLoc = new JSONObject();
            geoLoc.put("latitude", venue.latitude);
            geoLoc.put("longitude", venue.longitude);
            params.put("geolocation", geoLoc);

            HttpCallback addCb = new HttpCallback() {
                @Override
                public void onSuccess(Response response) {
                    BourbonNotification.addedVenue.issue(context);
                    cb.onSuccess(response);
                }
                @Override
                public void onFailure(Call call, IOException e, OGCloudError error) {
                    cb.onFailure(call, e, error);
                }
            };

            postJson(context, OGConstants.belliniCore + OGConstants.addVenuePath, params, addCb);

        } catch (JSONException e) {
            e.printStackTrace();
            cb.onFailure(null, null, OGCloudError.jsonError);
        }
    }

    /**
     * Logs out of OurGlass by removing stored information and navigates to the Welcome page.
     *
     * @param context the context to use
     */
    public static void logout(final Context context) {
        // TODO: is there more we need to do here?
        SharedPrefsManager.setJwt(context, null);
        SharedPrefsManager.setJwtExpiry(context, 0l);
        SharedPrefsManager.setUserId(context, null);

        ((Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, "Logged out!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, WelcomeActivity.class);
                context.startActivity(intent);
            }
        });
    }
}
