package com.sunnyday.appstartandend_one

import android.util.Log
import android.view.View

/**
 * Create by SunnyDay on 21:06 2020/06/17
 */
class WrapOnClickListener(var source: View.OnClickListener) : View.OnClickListener {

    /**
     * 代理onClick，新增一些逻辑
     * */
    override fun onClick(v: View?) {
        // 调用原有的功能
        source.onClick(v)
        // 插入埋点代码
        Log.d("WrapOnClickListener", "我是埋点代码")
    }
}