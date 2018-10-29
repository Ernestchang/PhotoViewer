package com.wanglu.photoviewerlibrary

import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup

class PhotoPagerAdapter(private var mData: MutableList<SingleViewInPager>) : PagerAdapter() {

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view==`object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        container.addView(mData[position])
        return mData[position]

    }

    override fun getCount(): Int {
        return mData.size
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        super.destroyItem(container, position, `object`)
        container.removeView(`object` as View?);
    }

    override fun getItemPosition(`object`: Any): Int {
        return super.getItemPosition(`object`)
    }

}