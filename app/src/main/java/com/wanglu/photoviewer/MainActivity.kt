package com.wanglu.photoviewer

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.wanglu.photoviewerlibrary.single.SinglePhoto
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val picData = arrayListOf(
                "http://img.zcool.cn/community/0117e2571b8b246ac72538120dd8a4.jpg@1280w_1l_2o_100sh.jpg",
                "http://img3.imgtn.bdimg.com/it/u=2200166214,500725521&fm=26&gp=0.jpg",
                "http://img.zcool.cn/community/01f76f5a4b4aa2a801219741c7bde1.jpg@1280w_1l_2o_100sh.jpg",
                "http://pic29.nipic.com/20130511/9252150_174018365301_2.jpg",
                "https://static.51jinke.com/112/A90DE763-4C25-4582-B5FA-2B5347482263/1544166428450"
        )

        val adapter = GvAdapter(this)
        gv.adapter = adapter
        adapter.setData(picData)
        gv.setOnItemClickListener { _, view, position, _ ->
//            PhotoViewer
//                    .setData(picData)
//                    .setCurrentPage(position)
//                    .setProcessButtonInterface(object : PhotoViewer.ProcessButtonInterface {
//                        override fun processButton(url: String) {
//                            Log.e("ernest", "url:" + url);
//                        }
//                    })
////                    .setImgContainer(gv)
//                    .setShowImageViewInterface(object : PhotoViewer.ShowImageViewInterface {
//                        override fun show(iv: ImageView, url: String) {
//                            Glide.with(iv.context).load(url).into(iv)
//                        }
//                    })
//                    .start(this)

            SinglePhoto
                    .setClickSingleImg(picData.get(position), view)
//                    .setCurrentPage(position)
//                    .setImgContainer(gv)
                    .setShowImageViewInterface(object : SinglePhoto.ShowImageViewInterface {
                        override fun show(iv: ImageView, url: String) {
                            Glide.with(iv.context).load(url).into(iv)
                        }
                    })
                    .show(this)
        }

//        Glide.with(this).load(picData[3]).into(iv)
//
//        iv.setOnClickListener {
//            PhotoViewer
//                    .setClickSingleImg(picData[3], iv)
//                    .setShowImageViewInterface(object : PhotoViewer.ShowImageViewInterface {
//                        override fun show(iv: ImageView, url: String) {
//                            Glide.with(iv.context).load(url).into(iv)
//                        }
//                    })
//                    .start(this)
//        }

    }
}
