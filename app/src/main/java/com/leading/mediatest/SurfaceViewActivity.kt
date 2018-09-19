package com.leading.mediatest

import android.graphics.BitmapFactory
import android.graphics.Paint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.SurfaceHolder
import kotlinx.android.synthetic.main.activity_surface_view.*

class SurfaceViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_surface_view)
        sv.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {

            }

            override fun surfaceDestroyed(p0: SurfaceHolder?) {
            }

            override fun surfaceCreated(p0: SurfaceHolder?) {
                val paint = Paint()
                paint.isAntiAlias = true
                paint.style = Paint.Style.STROKE
                val bitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
                val canvas = p0?.lockCanvas()
                canvas?.drawBitmap(bitmap, 0f, 0f, paint)
                p0?.unlockCanvasAndPost(canvas)
            }

        })
    }
}
