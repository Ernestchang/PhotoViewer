package com.wanglu.photoviewerlibrary.single

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ImageView
import android.widget.TextView

@SuppressLint("StaticFieldLeak")
/**
 * Created by WangLu on 2018/7/15.
 */
object SinglePhoto {
    internal var mInterface: ShowImageViewInterface? = null
    internal var mProcessInterface: ProcessButtonInterface? = null

    private lateinit var imgData: ArrayList<String> // 图片数据
    private lateinit var container: ViewGroup   // 存放图片的容器， ListView/GridView/RecyclerView
    private var currentPage = 0    // 当前页

    private var clickView: View? = null //点击那一张图片时候的view


    interface ShowImageViewInterface {
        fun show(iv: ImageView, url: String)
    }

    interface ProcessButtonInterface {
        fun processButton(tvOrigin: TextView, ivSave: ImageView, ivFull: ImageView)
    }

    /**
     * 设置显示ImageView的接口
     */
    fun setShowImageViewInterface(i: ShowImageViewInterface): SinglePhoto {
        mInterface = i
        return this
    }

    fun setProcessButtonInterface(i: ProcessButtonInterface): SinglePhoto {
        mProcessInterface = i
        return this
    }

    /**
     * 设置点击一个图片
     */
    fun setClickSingleImg(data: String, view: View): SinglePhoto {
        imgData = arrayListOf(data)
        clickView = view
        return this
    }

    /**
     * 设置图片数据
     */
    fun setData(data: ArrayList<String>): SinglePhoto {
        imgData = data
        return this
    }


    fun setImgContainer(container: AbsListView): SinglePhoto {
        SinglePhoto.container = container
        return this
    }

    fun setImgContainer(container: RecyclerView): SinglePhoto {
        SinglePhoto.container = container
        return this
    }

    /**
     * 获取itemView
     */
    private fun getItemView(): View {
        if (clickView == null) {
            val itemView = if (container is AbsListView) {
                val absListView = container as AbsListView
                absListView.getChildAt(currentPage - absListView.firstVisiblePosition)
            } else {
                (container as RecyclerView).layoutManager.findViewByPosition(currentPage)
            }

            var result: View? = null
            if (itemView is ViewGroup) {
                for (i in 0 until itemView.childCount) {
                    if (itemView.getChildAt(i) is ImageView) {
                        result = itemView.getChildAt(i) as ImageView
                        break
                    }
                }
            } else {
                result = itemView as ImageView
            }
            return result!!
        } else {
            return clickView!!
        }
    }

    /**
     * 获取现在查看到的图片的原始位置 (中间)
     */
    private fun getCurrentViewLocation(): IntArray {
        val result = IntArray(2)
        getItemView().getLocationInWindow(result)
        result[0] += getItemView().measuredWidth / 2
        result[1] += getItemView().measuredHeight / 2
        return result
    }


    /**
     * 设置当前页， 从0开始
     */
    fun setCurrentPage(page: Int): SinglePhoto {
        currentPage = page
        return this
    }

    fun start(fragment: Fragment) {
        val activity = fragment.activity!!
        start(activity as AppCompatActivity)
    }


    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun start(activity: AppCompatActivity) {
        show(activity)
    }


    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun show(activity: AppCompatActivity) {

        val decorView = activity.window.decorView as ViewGroup

        val b = Bundle()
        b.putString("pic_data", imgData[0])
        b.putIntArray("exit_location", getCurrentViewLocation())
        b.putIntArray("img_size", intArrayOf(getItemView().measuredWidth, getItemView().measuredHeight))
        val f = SingleView(activity, b)

        f.exitListener = object : SingleView.OnExitListener {
            override fun exit() {
                activity.runOnUiThread {
                    decorView.removeView(f)
                }
            }

        }
        decorView.addView(f, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

    }


}