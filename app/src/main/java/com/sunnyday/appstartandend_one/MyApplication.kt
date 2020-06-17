package com.sunnyday.appstartandend_one

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView

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
                val rootView = activity.findViewById<FrameLayout>(android.R.id.content)
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
        val listener = getOnClickListener(view)
        if (null != listener && !(listener is View.OnClickListener)) {
            view.setOnClickListener(WrapOnClickListener(listener))
        }
    }

    /**
     * get instance of OnClickListener
     * @param view view
     * */
    private fun getOnClickListener(view: View): View.OnClickListener? {
        return null
    }
}