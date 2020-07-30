package cn.jiajixin.touchtracker;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

public class TouchTrackWindowCallbackWrapper extends WindowCallbackWrapper {

    Window mWindow;

    public TouchTrackWindowCallbackWrapper(Window.Callback wrapped, Window window) {
        super(wrapped);
        this.mWindow = window;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        View view = TouchTrackUtils.findTouchTargetView(mWindow);
        if (TouchTrackToggle.enabled && event != null && event.getAction() == MotionEvent.ACTION_UP) {
            Log.i("TouchTrack", TouchTrackUtils.getViewInfo(view));
        }
        return super.dispatchTouchEvent(event);
    }
}
