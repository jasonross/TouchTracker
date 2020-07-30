package cn.jiajixin.touchtracker;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import cn.jiajixin.touchtrack.R;


/**
 * reflect mFirstTouchTarget to get the view which consumed the touch event.
 * use .(*.java/kt:1) format to jump to source code directly.
 */

public class TouchTrackUtils {

    public static View findTouchTargetView(Window window) {
        try {
            if (window == null) {
                return null;
            }
            ViewGroup decorView = (ViewGroup) window.getDecorView();
            ViewGroup vg = decorView;
            View touchTarget;
            while (true) {
                touchTarget = findTouchTarget(vg);
                if (touchTarget == null) {
                    return null;
                }
                if (touchTarget == vg) {
                    break;
                }
                if (!(touchTarget instanceof ViewGroup)) {
                    break;
                }
                vg = (ViewGroup) touchTarget;
            }
            return touchTarget;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private static View findTouchTarget(ViewGroup ancestor) {
        try {
            Field firstTouchTargetField = getDeclaredField(ancestor, "mFirstTouchTarget");
            if (firstTouchTargetField == null) {
                return ancestor;
            }

            firstTouchTargetField.setAccessible(true);
            Object firstTouchTarget = firstTouchTargetField.get(ancestor);
            if (firstTouchTarget == null) {
                return ancestor;
            }

            Field firstTouchViewField = firstTouchTarget.getClass().getDeclaredField("child");
            if (firstTouchViewField == null) {
                return ancestor;
            }

            firstTouchViewField.setAccessible(true);
            View firstTouchView = (View) firstTouchViewField.get(firstTouchTarget);
            if (firstTouchView == null) {
                return ancestor;
            }

            return firstTouchView;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Field getDeclaredField(Object object, String fieldName) {
        if (TextUtils.isEmpty(fieldName)) {
            return null;
        }

        Class<?> clazz = object.getClass();

        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                return field;
            } catch (NoSuchFieldException e) {

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        return null;
    }

    public static String getViewInfo(View view) {
        if (view == null) {
            return null;
        }

        List<String> classInfoList = getKeyClassInfoList(view);
        StringBuilder sb = new StringBuilder(view.toString());
        View.OnClickListener onClickListener = getOnClickListenerV14(view);
        if (onClickListener != null) {
            String classInfo = getClassInfo(onClickListener.getClass());
            if (classInfo != null) {
                sb.append("\n");
                sb.append(classInfo + " [OnClickListener]");
            }
        }
        if (classInfoList != null) {
            for (String classInfo : classInfoList) {
                if (!TextUtils.isEmpty(classInfo)) {
                    sb.append("\n").append(classInfo);
                }
            }
        }
        return sb.toString();
    }

    public static String getClassInfo(Class clazz) {
        if (clazz == null) {
            return null;
        }

        String name = clazz.getName();
        String liteName = name.replace("-$$Lambda$", "");
        int index = liteName.indexOf("$");
        if (index != -1) {
            String outerClassName = liteName.substring(0, index);
            try {
                Class outerClazz = Class.forName(outerClassName);
                return innerGetClassInfo(outerClazz) + " " + name;
            } catch (ClassNotFoundException e) {
                return name;
            }
        }

        String className = clazz.getSimpleName();
        if (TextUtils.isEmpty(className)) {
            return null;
        }

        return innerGetClassInfo(clazz);
    }


    private static String innerGetClassInfo(Class clazz) {
        return ".(" + clazz.getSimpleName() + "." + (isKotlinClass(clazz) ? "kt" : "java") + ":1)";
    }

    private static boolean isKeyClass(Class clazz) {
        if (clazz == null) {
            return false;
        }
        if (clazz.getName().startsWith("com.android.")) {
            return false;
        }
        if (clazz.getName().startsWith("android.")) {
            return false;
        }
        if (clazz.getName().startsWith("androidx.")) {
            return false;
        }
        return true;
    }

    //only works when proguard is not enabled
    private static boolean isKotlinClass(Class clazz) {
        if (clazz == null) {
            return false;
        }
        Annotation[] annotations = clazz.getDeclaredAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().getSimpleName().equals("Metadata")) {
                return true;
            }
        }
        return false;
    }

    private static void addClassInfo(List<String> list, String info) {
        if (info != null) {
            list.add(info);
        }
    }

    private static List<String> getKeyClassInfoList(View child) {
        if (child == null) {
            return null;
        }

        List<String> classInfoList = new ArrayList<>();
        View oc = child;
        Object object;
        String classInfo;
        while (child != null) {
            if (isKeyClass(child.getClass())) {
                classInfo = getClassInfo(child.getClass());
                addClassInfo(classInfoList, classInfo);
            }

            object = child.getTag(R.id._touch_track_object);
            if (object != null) {
                classInfo = getClassInfo(object.getClass());
                addClassInfo(classInfoList, classInfo);
            }

            if (child.getParent() instanceof View) {
                child = (View) child.getParent();
            } else {
                child = null;
            }
        }

        if (oc.getContext() instanceof Activity) {
            classInfo = getClassInfo(oc.getContext().getClass());
            addClassInfo(classInfoList, classInfo);
        }

        return classInfoList;
    }

    private static View.OnClickListener getOnClickListenerV14(View view) {
        View.OnClickListener retrievedListener = null;
        String viewStr = "android.view.View";
        String lInfoStr = "android.view.View$ListenerInfo";

        try {
            Field listenerField = Class.forName(viewStr).getDeclaredField("mListenerInfo");
            Object listenerInfo = null;

            if (listenerField != null) {
                listenerField.setAccessible(true);
                listenerInfo = listenerField.get(view);
            }

            Field clickListenerField = Class.forName(lInfoStr).getDeclaredField("mOnClickListener");

            if (clickListenerField != null && listenerInfo != null) {
                retrievedListener = (View.OnClickListener) clickListenerField.get(listenerInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return retrievedListener;
    }
}