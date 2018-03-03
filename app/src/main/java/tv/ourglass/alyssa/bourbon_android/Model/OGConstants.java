package tv.ourglass.alyssa.bourbon_android.Model;

/**
 * Created by atorres on 11/11/16.
 */

public class OGConstants {

    // Dev mode
    public static final boolean devMode = false;  // if true, login isn't really done
    public static final boolean alwaysShowIntro = false;
    public static final boolean alwaysSkipInto = true;

    // OGCloud paths
    // Production
    public static final String belliniCore = "https://cloud.ourglass.tv/";
    public static final String belliniDM = "https://cloud-dm.ourglass.tv/";

    // Staging
//    public static final String belliniCore = "http://138.68.230.239:2000/";
//    public static final String belliniDM = "http://138.68.230.239:2001/";

    // Local
    //public static final String belliniCore = "http://10.0.2.2:2000/";
    //public static final String BELLINI_DM_EMU_LOCAL_ADDRESS = "http://10.0.2.2:2001";

    public static final String loginPath = "auth/login";
    public static final String registerPath = "auth/addUser";
    public static final String changePasswordPath = "auth/changePwd";
    public static final String changeAccountPath = "user/";
    public static final String inviteUserPath = "user/inviteNewUser";
    public static final String getTokenPath = "user/jwt";
    public static final String checkJWTPath = "user/checkjwt";
    public static final String venuesPath = "venue/all";
    public static final String userVenuesPath = "venue/myvenues";
    public static final String devicesPath = "venue/devices?atVenueUUID=";
    public static final String findByRegCodePath = "ogdevice/findByRegCode?regcode=";
    public static final String changeDeviceNamePath = "ogdevice/changeName";
    public static final String associateWithVenuePath = "ogdevice/associateWithVenue";
    public static final String yelpSearchPath = "venue/yelpSearch";
    public static final String addVenuePath = "venue";

    public static final String deviceControlPath = "blueline/control?deviceUDID=";
    public static final String appControlPath = "app/control/index.html";

    public static final String introSlide1Path = "bourbon/intro/slide1.jpg";
    public static final String introSlide2Path = "bourbon/intro/slide2.jpg";
    public static final String introSlide3Path = "bourbon/intro/slide3.jpg";


    // Regex constants
    public static final String emailRegEx = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}";
    public static final String passwordRegEx = ".{8,}";

    // Intent extras
    public static final String firstNameExtra = "INTENT_EXTRA_FIRST_NAME";
    public static final String lastNameExtra = "INTENT_EXTRA_LAST_NAME";
    public static final String emailExtra = "INTENT_EXTRA_EMAIL";
    public static final String deviceUrlExtra = "INTENT_EXTRA_DEVICE_URL";
    public static final String deviceNameExtra = "INTENT_EXTRA_DEVICE_NAME";
    public static final String venueUUIDExtra = "INTENT_EXTRA_VENUE_UUID";
    public static final String venueNameExtra = "INTENT_EXTRA_VENUE_NAME";
    public static final String appUrlExtra = "INTENT_EXTRA_APP_URL";

    // Animations
    public static final int fadeInTime = 350;  // ms
    public static final int fadeOutTime = 350; // ms

    // Fonts
    public static final String regularFont = "Poppins-Regular.ttf";
    public static final String lightFont = "Poppins-Light.ttf";
    public static final String boldFont = "Poppins-Bold.ttf";
    public static final String mediumFont = "Poppins-Medium.ttf";
    public static final String semiBoldFont = "Poppins-SemiBold.ttf";
}
