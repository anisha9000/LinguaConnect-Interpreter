package interpreter.linguaconnect.com.linguaconnectinterpreter;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import interpreter.linguaconnect.com.linguaconnectinterpreter.utils.LruBitmapCache;

/**
 * Created by anisha on 7/1/16.
 */
public class AppController extends Application {

    /**
     * Global request queue for Volley
     */

    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private String TAG = AppController.class.getName();

    private static AppController instance;

    public static AppController getInstance() {
        return instance;
    }

    public AppController() {
        instance = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley
                    .newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }

    public ImageLoader getImageLoader() {
        getRequestQueue();
        if (mImageLoader == null) {
            mImageLoader = new ImageLoader(this.mRequestQueue,
                    new LruBitmapCache());
        }
        return this.mImageLoader;
    }

    /**
     * Adds the specified request to the global queue using the Default TAG.
     *
     * @param req
     * @param
     */
    public <T> void addToRequestQueue(Request<T> req) {
        // set the default tag if tag is empty
        req.setTag(TAG);

        getRequestQueue().add(req);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}
