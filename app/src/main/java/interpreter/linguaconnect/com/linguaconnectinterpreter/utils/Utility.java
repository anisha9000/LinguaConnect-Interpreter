package interpreter.linguaconnect.com.linguaconnectinterpreter.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by anisha on 7/1/16.
 */
public class Utility {

    private static String TAG="utility";
    public static final String PREFS_NAME = "linguaconnectinterpreter";
    public static final String CURRENT_GCM_ID = "interpreter.linguaconnect.com.linguaconnectinterpreter.utils.CURRENT_GCM_ID";
    public static final String APP_VERSION = "interpreter.linguaconnect.com.linguaconnectinterpreter.utils.APP_VERSION";


    public static String getLocalString(Context mContext, String key) {
        final SharedPreferences appData = mContext.getSharedPreferences( PREFS_NAME, 0);
        final String preData = appData.getString(key, "").trim();
        return preData;
    }

    public static int getLocalInt(Context mContext, String key) {
        final SharedPreferences appData = mContext.getSharedPreferences( PREFS_NAME, 0);
        final int preData = appData.getInt(key, 0);
        return preData;
    }

    public static Boolean getLocalBoolean(Context mContext, String key) {
        final SharedPreferences appData = mContext.getSharedPreferences( PREFS_NAME, 0);
        final boolean preData = appData.getBoolean(key, false);
        return preData;
    }

    public static void saveLocalBoolean(Context mContext,String key, boolean value){
        final SharedPreferences appData = mContext.getSharedPreferences(
                PREFS_NAME, 0);
        SharedPreferences.Editor editor = appData.edit();
        editor.putBoolean(key, value);
        editor.commit();

    }

    public static void  saveLocalString(Context mContext, String key,
                                        String data) {
        //  final String PREFS_NAME = "LinguaConnect Interpreter";
        final SharedPreferences appData = mContext.getSharedPreferences(
                PREFS_NAME, 0);
        SharedPreferences.Editor editor = appData.edit();
        editor.putString(key, data);
        editor.commit();
    }

    public static void  saveLocalInt(Context mContext, String key,
                                     int data) {
        //  final String PREFS_NAME = "LinguaConnect Interpreter";
        final SharedPreferences appData = mContext.getSharedPreferences(
                PREFS_NAME, 0);
        SharedPreferences.Editor editor = appData.edit();
        editor.putInt(key, data);
        editor.commit();
    }

    public static void showToast(Context context,String message){
        try{
            Toast t = Toast.makeText(context, message, Toast.LENGTH_LONG);
            ((TextView)((LinearLayout)t.getView()).getChildAt(0))
                    .setGravity(Gravity.CENTER_HORIZONTAL);
            t.show();
        } catch (Exception e) {
            Log.e(TAG,e.toString());
        }
    }

    public static void clearPreferences(Context _context){
        final SharedPreferences appData = _context.getSharedPreferences(
                PREFS_NAME, 0);
        SharedPreferences.Editor editor = appData.edit();
        editor.clear();
        editor.commit();
    }

}
