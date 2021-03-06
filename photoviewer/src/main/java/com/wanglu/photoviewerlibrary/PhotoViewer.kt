package com.wanglu.photoviewerlibrary

import android.animation.LayoutTransition
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.wanglu.photoviewerlibrary.R.id.tv_index


@SuppressLint("StaticFieldLeak")
/**
 * Created by WangLu on 2018/7/15.
 */
object PhotoViewer {
    internal var mInterface: ShowImageViewInterface? = null
    internal var mProcessInterface: ProcessButtonInterface? = null

    private lateinit var imgData: ArrayList<String> // 图片数据
    private lateinit var container: ViewGroup   // 存放图片的容器， ListView/GridView/RecyclerView
    private var currentPage = 0    // 当前页

    private var clickView: View? = null //点击那一张图片时候的view

    /**
     * 小圆点的drawable
     * 下标0的为没有被选中的
     * 下标1的为已经被选中的
     */
    private val mDot = intArrayOf(R.drawable.no_selected_dot, R.drawable.selected_dot)
    /**
     * 存放小圆点的Group
     */
    private var mDotGroup: LinearLayout? = null
    /**
     * 存放没有被选中的小圆点Group和已经被选中小圆点
     */
    private var mFrameLayout: FrameLayout? = null
    /**
     * 选中的小圆点
     */
    private var mSelectedDot: View? = null


    interface ShowImageViewInterface {
        fun show(iv: ImageView, url: String)
    }

    interface ProcessButtonInterface {
        fun processButton(url: String)
    }

    /**
     * 设置显示ImageView的接口
     */
    fun setShowImageViewInterface(i: ShowImageViewInterface): PhotoViewer {
        mInterface = i
        return this
    }

    fun setProcessButtonInterface(i: ProcessButtonInterface): PhotoViewer {
        mProcessInterface = i
        return this
    }

    /**
     * 设置点击一个图片
     */
    fun setClickSingleImg(data: String, view: View): PhotoViewer {
        imgData = arrayListOf(data)
        clickView = view
        return this
    }

    /**
     * 设置图片数据
     */
    fun setData(data: ArrayList<String>): PhotoViewer {
        imgData = data
        return this
    }


    fun setImgContainer(container: AbsListView): PhotoViewer {
        this.container = container
        return this
    }

    fun setImgContainer(container: RecyclerView): PhotoViewer {
        this.container = container
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
    fun setCurrentPage(page: Int): PhotoViewer {
        currentPage = page
        return this
    }

    fun start(fragment: Fragment) {
        val activity = fragment.activity!!
        start(activity as AppCompatActivity)
    }


    fun start(activity: AppCompatActivity) {
        show(activity)
    }

    private fun show(activity: AppCompatActivity) {


        val decorView = activity.window.decorView as ViewGroup

        // 设置添加layout的动画
        val layoutTransition = LayoutTransition()
        val alphaOa = ObjectAnimator.ofFloat(null, "alpha", 0f, 1f)
        alphaOa.duration = 100
        layoutTransition.setAnimator(LayoutTransition.APPEARING, alphaOa)
        decorView.layoutTransition = layoutTransition

        val frameLayout = FrameLayout(activity)

        val photoViewLayout = LayoutInflater.from(activity).inflate(R.layout.activity_photoviewer, null)
        val viewPager = photoViewLayout.findViewById<ViewPager>(R.id.mLookPicVP)
//        val tv_index = photoViewLayout.findViewById<TextView>(R.id.tv_index)
        val iv_save = photoViewLayout.findViewById<ImageView>(R.id.iv_save)
        iv_save.setOnClickListener(View.OnClickListener {
            if (mProcessInterface != null) {
                mProcessInterface!!.processButton(imgData[currentPage])
            }
        })

//        tv_index.setText((currentPage+1).toString() + "/" + imgData.size)

//        var fragments = mutableListOf<PhotoViewerFragment>()
        var fragments = mutableListOf<SingleViewInPager>()


        for (i in 0 until imgData.size) {

            val b = Bundle()
            b.putString("pic_data", imgData[i])
//            b.putIntArray("exit_location", getCurrentViewLocation())
//            b.putBoolean("in_anim", false)
//            b.putIntArray("img_size", intArrayOf(getItemView().measuredWidth, getItemView().measuredHeight))
            val f = SingleViewInPager(activity, b)

            f.exitListener = object : SingleViewInPager.OnExitListener {
                override fun exit() {
                    activity.runOnUiThread {
                        frameLayout.removeAllViews()
                        decorView.removeView(frameLayout)
                        fragments.clear()
                    }
                }

            }
            fragments.add(f)
        }

        val adapter = PhotoPagerAdapter(fragments)


        viewPager.adapter = adapter
        viewPager.currentItem = currentPage
        viewPager.offscreenPageLimit = 100
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

                if (mSelectedDot != null && imgData.size > 1) {
                    val dx = mDotGroup!!.getChildAt(1).x - mDotGroup!!.getChildAt(0).x
                    mSelectedDot!!.translationX = (position * dx) + positionOffset * dx
                }
            }

            override fun onPageSelected(position: Int) {
                currentPage = position

//                tv_index.setText((currentPage + 1).toString() + "/" + imgData.size)


//                val b = Bundle()
//                b.putString("pic_data", imgData[currentPage])
//                b.putIntArray("img_size", intArrayOf(getItemView().measuredWidth, getItemView().measuredHeight))
//                b.putBoolean("in_anim", false)
//                b.putIntArray("exit_location", getCurrentViewLocation())
//                fragments[position].arguments = b

            }

        })
        viewPager.isEnabled = false;

        frameLayout.addView(photoViewLayout)


        if (imgData.size > 1)
            frameLayout.post {

                /**
                 * 实例化两个Group
                 */
                if (mFrameLayout != null) {
                    mFrameLayout!!.removeAllViews()
                    mFrameLayout = null
                }
                mFrameLayout = FrameLayout(activity)
                if (mDotGroup != null) {
                    mDotGroup!!.removeAllViews()
                    mDotGroup = null
                }
                mDotGroup = LinearLayout(activity)

                if (mDotGroup!!.childCount != 0)
                    mDotGroup!!.removeAllViews()
                val dotParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT)
                /**
                 * 未选中小圆点的间距
                 */
                dotParams.rightMargin = Utils.dp2px(activity, 12)

                /**
                 * 创建未选中的小圆点
                 */
                for (i in 0 until imgData.size) {
                    val iv = ImageView(activity)
                    iv.setImageDrawable(activity.resources.getDrawable(mDot[0]))
                    iv.layoutParams = dotParams
                    mDotGroup!!.addView(iv)
                }

                /**
                 * 设置小圆点Group的方向为水平
                 */
                mDotGroup!!.orientation = LinearLayout.HORIZONTAL
                /**
                 * 设置小圆点在中间
                 */
                mDotGroup!!.gravity = Gravity.CENTER or Gravity.BOTTOM
                /**
                 * 两个Group的大小都为match_parent
                 */
                val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT)


                params.bottomMargin = Utils.dp2px(activity, 70)
                /**
                 * 首先添加小圆点的Group
                 */
                frameLayout.addView(mDotGroup, params)

                mDotGroup!!.post {
                    if (mSelectedDot != null) {
                        mSelectedDot = null
                    }
                    if (mSelectedDot == null) {
                        val iv = ImageView(activity)
                        iv.setImageDrawable(activity.resources.getDrawable(mDot[1]))
                        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        /**
                         * 设置选中小圆点的左边距
                         */
                        params.leftMargin = mDotGroup!!.getChildAt(0).x.toInt()
                        iv.translationX = (dotParams.rightMargin * currentPage + mDotGroup!!.getChildAt(0).width * currentPage).toFloat()
                        params.gravity = Gravity.BOTTOM
                        mFrameLayout!!.addView(iv, params)
                        mSelectedDot = iv
                    }
                    /**
                     * 然后添加包含未选中圆点和选中圆点的Group
                     */
                    frameLayout.addView(mFrameLayout, params)
                }
            }
        decorView.addView(frameLayout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

    }


}