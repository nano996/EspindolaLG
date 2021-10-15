package com.cgobbo.espindolalg

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat


class RequestPermissionHandler {
    private var mActivity: Activity? = null
    private var mRequestPermissionListener: RequestPermissionListener? = null
    private var mRequestCode = 0

    fun requestPermission(
        activity: Activity?, permissions: Array<String>, requestCode: Int,
        listener: RequestPermissionListener?
    ) {
        mActivity = activity
        mRequestCode = requestCode
        mRequestPermissionListener = listener
        if (!needRequestRuntimePermissions()) {
            mRequestPermissionListener!!.onSuccess()
            return
        }
        requestUnGrantedPermissions(permissions, requestCode)
    }

    private fun needRequestRuntimePermissions(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    private fun requestUnGrantedPermissions(permissions: Array<String>, requestCode: Int) {
        val unGrantedPermissions = findUnGrantedPermissions(permissions)
        if (unGrantedPermissions.size == 0) {
            mRequestPermissionListener!!.onSuccess()
            return
        }
        ActivityCompat.requestPermissions(mActivity!!, unGrantedPermissions, requestCode)
    }

    private fun isPermissionGranted(permission: String): Boolean {
        return (ActivityCompat.checkSelfPermission(mActivity!!, permission)
                == PackageManager.PERMISSION_GRANTED)
    }

    private fun findUnGrantedPermissions(permissions: Array<String>): Array<String> {
        val unGrantedPermissionList: MutableList<String> = ArrayList()
        for (permission in permissions) {
            if (!isPermissionGranted(permission)) {
                unGrantedPermissionList.add(permission)
            }
        }
        return unGrantedPermissionList.toTypedArray()
    }

    fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == mRequestCode) {
            if (grantResults.size > 0) {
                for (grantResult in grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        mRequestPermissionListener!!.onFailed()
                        return
                    }
                }
                mRequestPermissionListener!!.onSuccess()
            } else {
                mRequestPermissionListener!!.onFailed()
            }
        }
    }

    interface RequestPermissionListener {
        fun onSuccess()
        fun onFailed()
    }

}