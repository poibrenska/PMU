package com.pmu.gift_app.app;

public class AppConfig {

    private static String host = "192.168.1.6";
    // Server user login url
    public static String URL_LOGIN = "http://"+host+":8080/android_login_api/login.php";

    // Server user register url
    public static String URL_REGISTER = "http://"+host+":8080/android_login_api/register.php";

    public static String URL_CREATE_EVENT = "http://"+host+":8080/android_login_api/createEvent.php";

    public static String URL_SEARCH_EVENT = "http://"+host+":8080/android_login_api/searchEvent.php";

    public static String URL_DELETE_EVENT = "http://"+host+":8080/android_login_api/deleteEvent.php";

    public static String URL_GET_EVENTS = "http://"+host+":8080/android_login_api/getUserEvents.php";

    public static String URL_CREATE_GIFT = "http://"+host+":8080/android_login_api/createGift.php";

    public static String URL_DELETE_GIFT = "http://"+host+":8080/android_login_api/deleteGift.php";

    public static String URL_BOOK_GIFT = "http://"+host+":8080/android_login_api/bookGift.php";

    public static String URL_RETURN_GIFT = "http://"+host+":8080/android_login_api/returnGift.php";

    public static String URL_GET_GIFTS = "http://"+host+":8080/android_login_api/getGifts.php";
}
