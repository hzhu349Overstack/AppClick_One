package com.sunnyday.appstartandend_one

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import java.lang.reflect.Method

/**
 * Create by SunnyDay on 20:50 2020/06/17
 */
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityPaused(activity: Activity) {

            }

            override fun onActivityStarted(activity: Activity) {

            }

            override fun onActivityDestroyed(activity: Activity) {

            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

            }

            override fun onActivityStopped(activity: Activity) {

            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

            }

            override fun onActivityResumed(activity: Activity) {
                // 获得每个activity页面的rootView（android.R.id.content）
                val rootView: ViewGroup = activity.findViewById(android.R.id.content)
                delegateViewsOnClickListener(activity, rootView)
            }
        })
    }

    /**
     * Delegate view OnClickListener
     *@param context activity
     * @param view rootView
     *
     * */
    private fun delegateViewsOnClickListener(context: Context, view: View) {
        // 获取当前view 设置的OnClickListener
        val listener = getOnClickListener(view)
         Log.d(tag,"listener:$listener")
        //  判断已经设置的OnCLickListener类型，如果是自定义的WrapOnClickListener则说明
        // 已经代理过了，不用再去代理。
        if (null != listener && listener !is WrapOnClickListener) {
            Log.d(tag,"setOnClickListener")
            view.setOnClickListener(WrapOnClickListener(listener))
        }
        // 如果view 类型为ViewGroup则递归遍历子view
        if (view is ViewGroup) {
            val childCount = view.childCount
            if (childCount > 0) {
                for (i in 0 until childCount) {
                    val childView = view.getChildAt(i)
                    delegateViewsOnClickListener(context, childView)
                }
            }
        }
    }

    /**
     * get instance of OnClickListener
     * @param view view
     * */
    private fun getOnClickListener(view: View): View.OnClickListener? {
        val hasOnClickListener = view.hasOnClickListeners()
        if (hasOnClickListener) {
            val viewClazz = Class.forName("android.view.View")
            //通过view的getListenerInfo 方法来获得 ListenerInfo
            val listenerInfoMethod = viewClazz.getDeclaredMethod("getListenerInfo")
            if (!listenerInfoMethod.isAccessible) {
                listenerInfoMethod.isAccessible = true
            }
            val listenerInfoOjb = listenerInfoMethod.invoke(view)
            val listenerInfoClazz = Class.forName("android.view.View\$ListenerInfo")
            val onClickListenerField = listenerInfoClazz.getDeclaredField("mOnClickListener")
            if (!onClickListenerField.isAccessible) {
                onClickListenerField.isAccessible = true
            }
            return onClickListenerField.get(listenerInfoOjb) as View.OnClickListener
        }
        return null
    }
}