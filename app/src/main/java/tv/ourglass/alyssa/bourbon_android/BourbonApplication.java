package tv.ourglass.alyssa.bourbon_android;

import android.app.Application;
import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;

import io.fabric.sdk.android.Fabric;
import okhttp3.CookieJar;
import okhttp3.OkHttpClient;
import tv.ourglass.alyssa.bourbon_android.Model.SharedPrefsManager;
import tv.ourglass.alyssa.bourbon_android.Networking.OGHeaderInterceptor;

/**
 * Created by atorres on 11/14/16.
 */

public class BourbonApplication extends Application {

    private static BourbonApplication instance;

    //public static OkHttpClient okclient;

    public static Context getContext() {
        return instance;
    }

    public static final OkHttpClient okclient = new OkHttpClient.Builder()
            .addInterceptor(new OGHeaderInterceptor())
//            .cookieJar(new CookieJar() {
//                private final HashMap<HttpUrl, List<Cookie>> cookieStore = new HashMap<>();
//
//                @Override
//                public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
//                    cookieStore.put(url, cookies);
//                }
//
//                @Override
//                public List<Cookie> loadForRequest(HttpUrl url) {
//                    List<Cookie> cookies = cookieStore.get(url);
//                    return cookies != null ? cookies : new ArrayList<Cookie>();
//                }
//            })
            .build();

    @Override
    public void onCreate() {
        super.onCreate();

        CookieJar cookieJar = new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(getApplicationContext()));

        instance = this;

        Fabric.with(this, new Crashlytics());
        Fabric.with(this, new Answers());

        setFabricUserInfo();

    }

    public static void setFabricUserInfo(){
        Crashlytics.setUserEmail(SharedPrefsManager.getUserEmail(BourbonApplication.instance));
        Crashlytics.setUserIdentifier(SharedPrefsManager.getUserId(BourbonApplication.instance));
    }

}
