package com.sunnyday.appstartandend_one

import android.app.Activity
import android.content.ContextWrapper
import android.view.View

/**
 * Create by SunnyDay on 20:34 2020/06/19
 */

/**
 * 获取view 所属的activity
 * @param view view
 * */
fun getActivityFromView(view: View): Activity? {
    var activity: Activity? = null
    try {
        var context = view.context
        if (context is Activity) {
            activity = context
        } else if (context is ContextWrapper) {
            while (context !is Activity && context is ContextWrapper) {
                context = context.baseContext
            }
            if (context is Activity) {
                activity = context
            }
        }

    } catch (e: Exception) {
        e.printStackTrace()
    }
    return activity
}