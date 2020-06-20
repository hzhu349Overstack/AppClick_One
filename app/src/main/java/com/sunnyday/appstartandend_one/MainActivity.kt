package com.sunnyday.appstartandend_one

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.AppCompatTextView
import kotlinx.android.synthetic.main.activity_main.*

const val tag = "MainActivity"
class MainActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val textView = AppCompatTextView(this)
        textView.text = "我是动态添加的view"

        btn.setOnClickListener{
            Log.d("MainActivity","我是按钮点击事件")
            root_relative_layout.addView(textView)//点击按钮时给RelativeLayout添加个AppCompatTextView
        }

    }
}
