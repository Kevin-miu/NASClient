package client.nas.find.com.nasclient.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

/**
 * @author Kevin-
 * @time
 * @description
 * @updateTime
 */

public class PerferenceUtil {

    private static final String PREFERENCES = "prefs";
    private static final String KEY_WORKGROUP = "workgroup";
    private static final String KEY_CHECKED = "checked";
    private static final String KEY_IP = "ip";
    private static final String KEY_HOSTNAME = "hostname";
    private static final String KEY_USER = "user";
    private static final String KEY_PASS = "passwd";
    private static final String KEY_FOLDER = "folder";
    private static final String KEY_DEFAULT_PATH = "path";

    /**
     * 获取sharepreference对象
     *
     * @param context
     * @return
     */
    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    /**
     * 设置workgroup
     *
     * @param workgroup
     * @param context
     */
    public static void setWorkgroup(String workgroup, Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_WORKGROUP, workgroup);
        editor.apply();
    }

    /**
     * 获取workgroup
     *
     * @param context
     * @return
     */
    public static String getWorkgroup(Context context) {
        return getSharedPreferences(context).getString(KEY_WORKGROUP, "");
    }

    /**
     * 以下分别是对各个key的设置和获取方法
     *
     * @param isChecked
     * @param context
     */
    public static void setCheck(boolean isChecked, Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_CHECKED, isChecked);
        editor.apply();
    }

    public static boolean getCheck(Context context) {
        return getSharedPreferences(context).getBoolean(KEY_CHECKED, false);
    }

    public static void setIp(String value, Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_IP, value);
        editor.apply();
    }

    public static String getIp(Context context) {
        return getSharedPreferences(context).getString(KEY_IP, "");
    }

    public static void setUser(String value, Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_USER, value);
        editor.apply();
    }

    public static String getUser(Context context) {
        return getSharedPreferences(context).getString(KEY_USER, "");
    }

    public static void setPass(String value, Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_PASS, value);
        editor.apply();
    }

    public static String getPass(Context context) {
        return getSharedPreferences(context).getString(KEY_PASS, "");
    }

    public static void setFolder(String value, Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_FOLDER, value);
        editor.apply();
    }

    public static String getFolder(Context context) {
        return getSharedPreferences(context).getString(KEY_FOLDER, "");
    }

    public static void setLocalPath(String value, Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_DEFAULT_PATH, value);
        editor.apply();
    }

    public static String getLocalPath(Context context) {
        String defaultPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        return getSharedPreferences(context).getString(KEY_DEFAULT_PATH, defaultPath);
    }

    public static void setHostname(String value, Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_HOSTNAME, value);
        editor.apply();
    }

    public static String getHostname(Context context) {
        return getSharedPreferences(context).getString(KEY_HOSTNAME, "");
    }


}
