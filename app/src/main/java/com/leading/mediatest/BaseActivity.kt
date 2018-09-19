package com.leading.mediatest

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast

/**
 * @package com.leading.mediatest
 * @fileName BaseActivity
 * @date 2018/9/19 16:01
 * @author Zj
 * @describe TODO
 * @org Leading.com(北京理正软件)
 * @email 2856211755@qq.com
 * @computer Administrator
 */
abstract class BaseActivity : AppCompatActivity() {
    companion object {
        const val TAG = "BaseActivity"
        const val PERMISSION_REQUEST_CODE = 666
    }

    /**
     * 需要申请的权限
     */
    protected val allPermissions = arrayOf(Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA)
    /**
     * 被用户拒绝的权限
     */
    protected lateinit var permissionsNeedToRequest: ArrayList<String>

    /**
     * 获取到了所有权限
     */
    abstract fun allPermissionsGranted()

    /**
     * 检查权限
     */
    protected fun checkPermission() {
        permissionsNeedToRequest = ArrayList()
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            for (permission in allPermissions) {
                if (ContextCompat.checkSelfPermission(this, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    permissionsNeedToRequest.add(permission)
                }
            }
            if (!permissionsNeedToRequest.isEmpty()) {
                ActivityCompat.requestPermissions(this,
                        permissionsNeedToRequest.toArray(arrayOfNulls(permissionsNeedToRequest.size))
                        , PERMISSION_REQUEST_CODE)
            } else {
                allPermissionsGranted()
            }
        } else {
            Log.e(TAG, "Build.VERSION.SDK_INT <= Build.VERSION_CODES.M --> " + Build.VERSION.SDK_INT)
            allPermissionsGranted()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val permissionsDenied: ArrayList<String> = ArrayList()
            for (i in 0 until grantResults.size) {
                val grantResult = grantResults[i]
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    permissionsDenied.add(permissions[i])
                }
            }
            if (permissionsDenied.isNotEmpty()) {
                Toast.makeText(this, (permissionsDenied + " 权限被用户禁止！")
                        .toString(), Toast.LENGTH_LONG).show()
                Log.i(TAG, (permissionsDenied + " 权限被用户禁止！").toString())
                finish()
            } else {
                allPermissionsGranted()
            }
        }
    }
}