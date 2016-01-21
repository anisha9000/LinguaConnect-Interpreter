package interpreter.linguaconnect.com.linguaconnectinterpreter.utils;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by anisha on 7/1/16.
 */
public class HandleProgressBar extends ProgressDialog {

    private ActivityListener listener;

    public HandleProgressBar(Context context) {
        super(context, ProgressDialog.THEME_HOLO_LIGHT);
        this.setIndeterminate(true);
    }

    public HandleProgressBar(Context context,boolean isHorizontal) {
        super(context, ProgressDialog.THEME_HOLO_LIGHT);
        if(!isHorizontal) {
            this.setIndeterminate(true);
        }

    }


    public void setControl() {
        this.setCancelable(false);
        this.setCanceledOnTouchOutside(false);
    }

    public void hideProgressBar() {
        if (this.isShowing()) {
            this.dismiss();
        }
    }

    public void showProgressBar() {
        if (!this.isShowing()) {
            this.show();
        }
    }

    @Override
    public void onBackPressed() {
        if(listener!=null) {
            listener.backPressedCalled();
        }
        super.onBackPressed();
    }

    public void setActivityListener(ActivityListener listener){
        this.listener=listener;
    }

    public static interface ActivityListener{
        void backPressedCalled();
    }

}
