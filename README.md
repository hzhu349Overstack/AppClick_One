#  AppClick全埋点解决方案1：代理View.OnclickListener

### 一、技术实现原理 ：android.R.id.content

###### 1、原理

>Activity的setContentView（View view）方法加载的view，最终会被加载到id为android.R.id.content的FrameLayout中。基于这点我们可以在自定义的application中注册ActivityLifecycleCallbacks回调，在onActivityResumed(Activity activity)回调方法中就可拿到activity的实例，然后通过activity.findViewById(android.R.id.content)就可获得整体的根view。获得根view后就可遍历所有的子view，判断当前子View是否设置了mOnClickListener对象，如果已设置mOnClickListener对象并且mOnClickListener又不是我们自定义的WrapperOnClickListener类型，则通过WrapperOnClickListener代理当前View设置的mOnClickListener。即可实现“插入”埋点代码，从而达到自动埋点的效果。

###### 2、原理图

![](<https://github.com/sunnnydaydev/AppStartAndEnd_One/blob/master/%E5%8E%9F%E7%90%86%E5%9B%BE.png>)

###### 3、代码实现

（1）代理类

```java

/**
 * Create by SunnyDay on 21:06 2020/06/17
 */
class WrapOnClickListener(var source: View.OnClickListener) : View.OnClickListener {

    /**
     * 代理onClick，新增一些逻辑
     * */
    override fun onClick(v: View?) {
        Log.d("MainActivity", "用户点击事件之前：")// 代理的功能
        source.onClick(v) // 调用原有的功能
        Log.d("MainActivity", "用户点击事件之后:")//代理的功能
    }
}
```

（2）View点击事件的获取

```java
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
```



（3）获取每个Activity的rootView（android.R.id.content），递归遍历代理即可。

```java
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
```



(4)MainActivity&Button 点击事件测试

```java
 btn.setOnClickListener{
            Log.d("MainActivity","我是按钮点击事件")
        }

log:
2020-04-01 00:29:18.423 13681-13681/com.sunnyday.appstartandend_one D/MainActivity: 用户点击事件之前：
2020-04-01 00:29:18.423 13681-13681/com.sunnyday.appstartandend_one D/MainActivity: 我是按钮点击事件
2020-04-01 00:29:18.423 13681-13681/com.sunnyday.appstartandend_one D/MainActivity: 用户点击事件之后
```

###### 4、android.R.id.content 弊端

（1）可正常采集的点击事件

> - 通过代码设置mOnClickListener对象
> - 通过android：onClick属性绑定处理函数
> - 通过注解绑定处理函数，如ButterKnife绑定处理函数
> - 含有Lambda语法的mOnClickListener（本文栗子中采取的）

（2）无法采集的事件

> 通过DataBinding绑定处理函数的点击事件是无法正常采集的
>
> 原因：这是由于DataBinding框架给Button设置mOnClickListener对象的动作稍微晚于onActivityResumed生命周期函数。即我们去代理Button已设置的mOnClickListener对象时，DataBinding框架还没有完成给Button设置mOnClickListener对象的操作，所以我们去遍历RootView时，当前View不满足hasOnClickListener的判断条件，因此没有去代理其mOnClickListener对象，从而导致无法采集其点击事件。
>
> 解决方案：既然是某些动作延迟导致的，那我们可以在Application.ActivityLifecycleCallbacks的onActivityResumed(final Activity activity)回调方法中，也去延迟一定的时间，然后再去调用delegateViewsOnClickListener(Context context，View view)方法遍历RootView，这样就相当于给DataBinding框架一些时间去处理。



### 二、优化：引入DecorView

###### 1、ActionBar的兼容问题

> - android SDK 14+ ，android.R.id.content这个view的区域不包括actionBar
> - android Support Library Revision lower than 19：使用AppCompat，则显示区域包含ActionBar
> - Support Library Revision 19(or greater)：使用AppCompat，则显示区域不包含ActionBar
>
> 可知：如果不使用Support Library或使用Support Library的最新版本，则android.R.id.content所指的区域都是ActionBar以下的内容，所以当手机屏幕上的菜单（MenuItem）点击事件是无法采集的。

###### 2、DecorView的引入

![](<https://github.com/sunnnydaydev/AppClick_One/blob/master/decorview.jpg>)

> 针对上面提到的无法采集MenuItem点击事件的问题，我们只需要将之前方案中的activity.findViewById(android.R.id.content)换成activity.getWindow().getDecorView()，就可以遍历到MenuItem了，从而就可以自动采集到MenuItem点击事件了

```kotlin
    override fun onActivityResumed(activity: Activity) {
                // 获得每个activity页面的rootView（android.R.id.content）
              //  val rootView: ViewGroup = activity.findViewById(android.R.id.content)
                delegateViewsOnClickListener(activity, activity.window.decorView)
            }
```



###  三、继续优化：引入ViewTreeObserver.OnGlobalLayoutListener

###### 1、开发者动态添加view问题

> 当前的方案还有一个问题，即：该方案无法采集onResume()生命周期之后动态创建的View点击事件。如下：

```java
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
```

> 这是因为我们是在Activity的onResume生命周期之前去遍历整个RootView并代理其mOnClickListener对象的。如果是在onResume生命周期之后动态创建的View，当时肯定是无法被遍历到的，后来我们又没有再次去遍历，所以它的mOnClickListener对象就没有被我们代理过。因此，点击控件时，是无法采集到其点击事件的。

###### 2、引入：ViewTreeObserver.OnGlobalLayoutListener

> view视图树发生变化时这个方法会回调，根据这个原理，我们可以监听view视图树的变化，再次遍历RootView即可。

```java
 override fun onActivityResumed(activity: Activity) {
                // 获得每个activity页面的rootView（android.R.id.content）
                //  val rootView: ViewGroup = activity.findViewById(android.R.id.content)

                val rootView = activity.window.decorView
                rootView.viewTreeObserver.addOnGlobalLayoutListener {
                    delegateViewsOnClickListener(activity, rootView)
                }
            }
```

注意点：

- 最好在onDestory中销毁viewTreeObserver监听对象（本文省略了）
- 采用这种方案之后，也可以直接采集通过DataBinding绑定的点击事件了，同时之前采用延迟的方案也可以废弃了
- 由于该方案遍历的是Activity的RootView，所以游离于Activity之上的View的点击是无法采集的，比如Dialog、PopupWindow等

### 四、额外收获

###### 1、view.getContext

> 通过view.getContext()方法获取的Context有可能是ContextWrapper类型，此类型是无法直接转成Activity对象的，需要通过context.getBaseContext()方法逐层找到那个Activity

```java
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
```



### 小结

> 本章使用的技术方式缺点很多，就当学学技术吧！！！

###### 1、缺点

- 由于使用反射，效率比较低，对App的整体性能有一定的影响，也可能会引入兼容性方面的风险
- Application.ActivityLifecycleCallbacks要求API 14+
- View.hasOnClickListeners()要求API 15+
- removeOnGlobalLayoutListener要求API 16+
- 无法直接支持采集游离于Activity之上的View的点击，比如Dialog、Popup-Window等。

###### 2、小结

> 内部类包名写法（外部类包名+外部类名$内部类名）
>
> 反射复习
>
> 代理设计模式实际运用
>
> appClick 埋点一种思想

