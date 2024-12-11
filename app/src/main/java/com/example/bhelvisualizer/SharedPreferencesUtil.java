package com.example.bhelvisualizer;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesUtil {
    private static final String SHARED_PREF_NAME = "my_shared_pref";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_LOGGED_IN_AS = "logged_in_as";
    private static final String KEY_USER_PRIORITY = "user_priority";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_ROLE = "user_role";

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isLoggedIn(Context context) {
        return getSharedPreferences(context).getBoolean(KEY_LOGGED_IN, false);
    }

    public static void setLoggedIn(Context context, boolean loggedIn, String loggedInAs) {
        getSharedPreferences(context).edit()
                .putBoolean(KEY_LOGGED_IN, loggedIn)
                .putString(KEY_LOGGED_IN_AS, loggedInAs)
                .apply();
    }

    public static String getLoggedInAs(Context context) {
        return getSharedPreferences(context).getString(KEY_LOGGED_IN_AS, "");
    }

    public static void setUserPriority(Context context, int priority) {
        getSharedPreferences(context).edit()
                .putInt(KEY_USER_PRIORITY, priority)
                .apply();
    }

    public static int getUserPriority(Context context) {
        return getSharedPreferences(context).getInt(KEY_USER_PRIORITY, 0); // Default to lowest priority
    }

    public static void setUserName(Context context, String name) {
        getSharedPreferences(context).edit()
                .putString(KEY_USER_NAME, name)
                .apply();
    }

    public static String getUserName(Context context) {
        return getSharedPreferences(context).getString(KEY_USER_NAME, "");
    }

    public static void setUserEmail(Context context, String email) {
        getSharedPreferences(context).edit()
                .putString(KEY_USER_EMAIL, email)
                .apply();
    }

    public static String getUserEmail(Context context) {
        return getSharedPreferences(context).getString(KEY_USER_EMAIL, "");
    }

    public static void clearPreferences(Context context) {
        getSharedPreferences(context).edit()
                .clear()
                .apply();
    }

    public static void setUserRole(Context context, String role) {
        getSharedPreferences(context).edit()
                .putString(KEY_USER_ROLE, role)
                .apply();
    }

    public static String getUserRole(Context context) {
        return getSharedPreferences(context).getString(KEY_USER_ROLE, "");
    }
}
