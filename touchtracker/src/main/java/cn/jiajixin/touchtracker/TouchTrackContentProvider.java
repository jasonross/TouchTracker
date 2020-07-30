package cn.jiajixin.touchtracker;

import android.app.Activity;
import android.app.Application;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import cn.jiajixin.touchtrack.R;


public class TouchTrackContentProvider extends ContentProvider {

    private final FragmentManager.FragmentLifecycleCallbacks mFragmentLifecycleCallbacks =
            new FragmentManager.FragmentLifecycleCallbacks() {
                @Override
                public void onFragmentViewCreated(FragmentManager fm, Fragment f,
                                                  View v, Bundle savedInstanceState) {
                    super.onFragmentViewCreated(fm, f, v, savedInstanceState);
                    v.setTag(R.id._touch_track_object, f);
                }
            };

    @Override
    public boolean onCreate() {
        if (isAppDebuggable()) {
            if (getContext().getApplicationContext() instanceof Application) {
                ((Application) getContext().getApplicationContext()).registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacksAdapter() {

                    @Override
                    public void onActivityResumed(@NonNull Activity activity) {
                        super.onActivityResumed(activity);
                        if (TouchTrackToggle.enabled) {
                            Window.Callback callback = activity.getWindow().getCallback();
                            if (callback != null && !(callback instanceof TouchTrackWindowCallbackWrapper)) {
                                activity.getWindow().setCallback(
                                        new TouchTrackWindowCallbackWrapper(callback, activity.getWindow()));
                                if (TouchTrackToggle.enabled && activity instanceof FragmentActivity) {
                                    ((FragmentActivity) activity).getSupportFragmentManager()
                                            .registerFragmentLifecycleCallbacks(mFragmentLifecycleCallbacks, true);
                                }
                            }
                        }
                    }
                });
            }
        }
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        return 0;
    }

    private boolean isAppDebuggable() {
        return 0 != (getContext().getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE);
    }
}
