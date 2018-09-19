package com.leading.mediatest

import android.hardware.Camera
import android.os.Bundle
import android.view.SurfaceHolder
import kotlinx.android.synthetic.main.activity_camera.*

class CameraActivity : BaseActivity(), SurfaceHolder.Callback {
    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        camera.release()
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        camera.setPreviewDisplay(holder)
        camera.startPreview()
    }

    lateinit var camera: Camera

    override fun allPermissionsGranted() {
        surface_view.holder.addCallback(this)

        // 打开摄像头并将展示方向旋转90度
        camera = Camera.open()
        camera.setDisplayOrientation(90)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        checkPermission()
    }
}
