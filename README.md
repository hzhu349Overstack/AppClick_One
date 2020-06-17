#  AppClick全埋点解决方案1：代理View.OnclickListener

### 一、技术实现原理 ：android.R.id.content

###### 1、原理

>Activity的setContentView（View view）方法加载的view，最终会被加载到id为android.R.id.content的FrameLayout中。基于这点我们可以在自定义的application中注册ActivityLifecycleCallbacks回调，在onActivityResumed(Activity activity)回调方法中就可拿到activity的实例，然后通过activity.findViewById(android.R.id.content)就可获得整体的根view。获得根view后就可遍历所有的子view，判断当前子View是否设置了mOnClickListener对象，如果已设置mOnClickListener对象并且mOnClickListener又不是我们自定义的WrapperOnClickListener类型，则通过WrapperOnClickListener代理当前View设置的mOnClickListener。即可实现“插入”埋点代码，从而达到自动埋点的效果。

###### 2、原理图

![](<https://github.com/sunnnydaydev/AppStartAndEnd_One/blob/master/%E5%8E%9F%E7%90%86%E5%9B%BE.png>)

###### 3、代码实现



###### 4、弊端



### 二、优化：引入DecorView



### 三、小结

> 本章使用的技术方式缺点很多，就当学学技术吧！！！

###### 1、缺点

- 由于使用反射，效率比较低，对App的整体性能有一定的影响，也可能会引入兼容性方面的风险
- Application.ActivityLifecycleCallbacks要求API 14+
- View.hasOnClickListeners()要求API 15+
- removeOnGlobalLayoutListener要求API 16+
- 无法直接支持采集游离于Activity之上的View的点击，比如Dialog、Popup-Window等。

待续！！！