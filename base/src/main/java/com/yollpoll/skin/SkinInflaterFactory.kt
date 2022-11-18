package com.yollpoll.skin

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewStub
import com.yollpoll.base.R
import com.yollpoll.base.logE
import com.yollpoll.base.logI
import java.lang.reflect.Constructor
import java.util.*

/**
 * Created by spq on 2022/11/15
 */
private val constructorMap: HashMap<String, Constructor<out View>> = HashMap()
private val BOOT_CLASS_LOADER = LayoutInflater::class.java.classLoader

object SkinInflaterFactory : LayoutInflater.Factory2 {
    override fun onCreateView(
        parent: View?,
        name: String,
        context: Context,
        attrs: AttributeSet
    ): View? {
        // if this is NOT enable to be skined , simplly skip it
        val skinTag: String =
            attrs.getAttributeValue(NAMESPACE, SKIN_TAG) ?: return null

        val view: View? = createView(context, name, attrs).let {
            it?.setSkin(parent, name, skinTag, attrs)
        }
//        view?.setBackgroundColor(context.resources.getColor(R.color.black))
//        parseSkinAttr(context, attrs, view)

        return view
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        return null
    }

    /**
     * Invoke low-level function for instantiating a view by name. This attempts to
     * instantiate a view class of the given <var>name</var> found in this
     * LayoutInflater's ClassLoader.
     *
     * @param context
     * @param name    The full name of the class to be instantiated.
     * @param attrs   The XML attributes supplied for this instance.
     * @return View The newly instantiated view, or null.
     */
    private fun createView(context: Context, name: String, attrs: AttributeSet): View? {
        var view: View? = null
        try {
            if (-1 == name.indexOf('.')) {
                view = createViewFromPrefix(context, name, "android.view.", attrs)
                if (view == null) {
                    view = createViewFromPrefix(context, name, "android.widget.", attrs)
                    if (view == null) {
                        view = createViewFromPrefix(context, name, "android.webkit.", attrs)
                    }
                }
            } else {
                "自定义view create".logI()
                view = createViewFromPrefix(context, name, null, attrs)
            }
        } catch (e: Exception) {
            ("error while create 【" + name + "】 : " + e.message).logE()
            view = null
        }
        return view
    }

    private fun createViewFromPrefix(
        context: Context,
        name: String,
        prefix: String?,
        attrs: AttributeSet
    ): View? {
        val view: View? = try {
            if (constructorMap.containsKey(prefix + name) && verifyClassLoader(
                    context,
                    constructorMap[prefix + name]!!
                )
            ) {
                //缓存中存在并且通过classloader检查
                constructorMap[prefix + name]!!.newInstance(context, attrs)
            } else {
                //缓存中不存在
                //反射获取完整类名
                var clazz: Class<out View?> = Class.forName(
                    if (prefix != null) prefix + name else name, false,
                    context.classLoader
                ).asSubclass(View::class.java)
                //获取构造方法
                val constructor =
                    clazz.getConstructor(Context::class.java, AttributeSet::class.java)
                constructor.isAccessible = true
                //加入缓存
                constructorMap[prefix + name] = constructor
                constructor.newInstance(context, attrs)
            }

        } catch (e: java.lang.Exception) {
            null
        }
        if (view == null) {
            "view is null".logI()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                "view is not null: ${view.accessibilityClassName}".logI()
            }
        }
        return view
    }

    /**
     * classloader检查是否和context是一个classloader，createView中源码是这么做的，一起搬过来了
     * @param context Context
     * @param constructor Constructor<out View>
     * @return Boolean
     */
    private fun verifyClassLoader(context: Context, constructor: Constructor<out View>): Boolean {
        val constructorLoader = constructor.declaringClass.classLoader
        if (constructorLoader === BOOT_CLASS_LOADER) {
            // fast path for boot class loader (most common case?) - always ok
            return true
        }
        // in all normal cases (no dynamic code loading), we will exit the following loop on the
        // first iteration (i.e. when the declaring classloader is the contexts class loader).
        var cl: ClassLoader? = context.classLoader
        do {
            if (constructorLoader === cl) {
                return true
            }
            cl = cl!!.parent
        } while (cl != null)
        return false
    }

}

