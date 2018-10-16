package com.wanglu.photoviewerlibrary.single

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.wanglu.photoviewerlibrary.R
import kotlinx.android.synthetic.main.item_picture_single.view.*

class SingleView constructor(context: Activity, arguments: Bundle) : FrameLayout(context) {

    var exitListener: OnExitListener? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.item_picture_single, this, true)
        val mIv = findViewById<SinglePhotoView>(R.id.mIv)
        val iv_save = findViewById<ImageView>(R.id.iv_save)
        val tv_origin = findViewById<TextView>(R.id.tv_origin)
        val root = findViewById<RelativeLayout>(R.id.root)

        val mExitLocation: IntArray = arguments!!.getIntArray("exit_location")
        val mImgSize: IntArray = arguments!!.getIntArray("img_size")
        val mPicData = arguments!!.getString("pic_data")

        if (SinglePhoto.mProcessInterface != null) {
            SinglePhoto.mProcessInterface!!.processButton(tv_origin, iv_save, mIv)
        }

        if (SinglePhoto.mInterface != null) {
            SinglePhoto.mInterface!!.show(mIv, mPicData)
        } else {
            throw RuntimeException("请设置图片加载回调 ShowImageViewInterface")
        }

        var alpha = 1f  // 透明度
        mIv.setExitLocation(mExitLocation)
        mIv.setImgSize(mImgSize)

        var intAlpha = 255
//        root.background.alpha = intAlpha
        mIv.rootView = root
        mIv.setOnViewFingerUpListener {
            alpha = 1f
            intAlpha = 255
        }

        // 注册退出Activity 滑动大于一定距离后退出
        mIv.setExitListener {
            if (exitListener != null) {
                exitListener!!.exit()
            }
        }

        // 循环查看是否添加上了图片
        Thread(Runnable {
            while (true) {
                if (mIv.drawable != null) {
                    context.runOnUiThread {
                        loading.visibility = View.GONE
                    }
                    break
                }
                Thread.sleep(300)
            }
        }).start()

        // 添加点击进入时的动画
        if (arguments!!.getBoolean("in_anim", true))
            mIv.post {
                mIv.visibility = View.VISIBLE
                val scaleOa = ObjectAnimator.ofFloat(mIv, "scale", mImgSize[0].toFloat() / mIv.width, 1f)
                val xOa = ObjectAnimator.ofFloat(mIv, "translationX", mExitLocation[0].toFloat() - mIv.width / 2, 0f)
                val yOa = ObjectAnimator.ofFloat(mIv, "translationY", mExitLocation[1].toFloat() - mIv.height / 2, 0f)
                val alphaOa = ValueAnimator.ofInt(0, 255)
                alphaOa.addUpdateListener({ valueAnimator ->
                    root.background.alpha = valueAnimator.animatedValue as Int
                })
                val set = AnimatorSet()
                set.duration = 250
                set.playTogether(scaleOa, xOa, yOa, alphaOa)
                set.start()
            }

        root.isFocusableInTouchMode = true
        root.requestFocus()
        root.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {

                mIv.exit()

                return@OnKeyListener true
            }
            false
        })

        mIv.setOnViewDragListener { dx, dy ->
            mIv.scrollBy((-dx).toInt(), (-dy).toInt())  // 移动图像
            alpha -= dy * 0.001f
            intAlpha -= (dy * 0.2).toInt()

            if (alpha > 1) alpha = 1f
            else if (alpha < 0) alpha = 0f

            if (intAlpha < 0) intAlpha = 0
            else if (intAlpha > 255) intAlpha = 255
            root.background.alpha = intAlpha    // 更改透明度

//            if (intAlpha >= 140)
//                mIv.attacher.scale = (intAlpha / 255.0f)   // 更改大小

            if (alpha >= 0.6) {
                mIv.attacher.scale = alpha
            }
        }


        mIv.setOnClickListener {
            mIv.exit()
        }
    }


    interface OnExitListener {
        fun exit()
    }


}