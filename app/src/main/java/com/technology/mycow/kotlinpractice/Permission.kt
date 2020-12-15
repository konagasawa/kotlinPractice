package com.technology.mycow.kotlinpractice

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import java.util.jar.Manifest

object Permission {

    fun requestPermission(
        activity: FragmentActivity,
        requestId: Int,
        permission: String,
        finishActivity: Boolean
    ){
        if(ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)){
            RationaleDialog.newInstance(requestId, finishActivity).show(activity.supportFragmentManager, "dialog")
        } else {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(permission),
                requestId
            )
        }
    }

    fun isPermissionGranted(
        grantPermissions: Array<String>, grantResults: IntArray, permission: String
    ): Boolean {
        for (i in grantPermissions.indices){
            if(permission == grantPermissions[i]){
                return grantResults[i] == PackageManager.PERMISSION_GRANTED
            }
        }
        return false
    }

    class PermissionDeniedDialog : DialogFragment(){
        private var finishActivity = false
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            finishActivity = arguments?.getBoolean(ARGUMENT_FINISH_ACTIVITY) ?: false
            return AlertDialog.Builder(activity)
                .setMessage("PERMISSION IS DENIED").setPositiveButton("OK", null)
                .create()
        }

        override fun onDismiss(dialog: DialogInterface) {
            super.onDismiss(dialog)
            if(finishActivity){
                Toast.makeText(activity, "PERMISSION IS REQUIRED", Toast.LENGTH_LONG).show()
                //activity?.finish()
            }
        }

        companion object {
            private const val ARGUMENT_FINISH_ACTIVITY = "finish"

            fun newInstance(finishActivity: Boolean): PermissionDeniedDialog {
                val arguments = Bundle().apply {
                    putBoolean(ARGUMENT_FINISH_ACTIVITY, finishActivity)
                }
                return PermissionDeniedDialog().apply {
                    this.arguments = arguments
                }
            }
        }
    }

    class RationaleDialog : DialogFragment(){
        private var finishActivity = false
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val requestCode = arguments?.getInt(ARGUMENT_PERMISSION_REQUEST_CODE) ?: 0
            finishActivity = arguments?.getBoolean(ARGUMENT_FINISH_ACTIVITY) ?: false
            return AlertDialog.Builder(activity)
                .setMessage("THIS APP REQUIRES YOUR LOCATION")
                .setPositiveButton("OK") { dialog, which ->
                    ActivityCompat.requestPermissions(
                        activity!!,
                        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                        requestCode
                    )
                    finishActivity = false
                }
                .setNegativeButton("CANCEL",null)
                .create()
        }

        override fun onDismiss(dialog: DialogInterface) {
            super.onDismiss(dialog)
            if(finishActivity){
                Toast.makeText(activity, "PERMISSION IS REQUIRED", Toast.LENGTH_LONG).show()
                //activity?.finish()
            }
        }

        companion object {
            private const val ARGUMENT_PERMISSION_REQUEST_CODE = "requestCode"
            private const val ARGUMENT_FINISH_ACTIVITY = "finish"

            fun newInstance(requestCode: Int, finishActivity: Boolean): RationaleDialog {
                val arguments = Bundle().apply {
                    putInt(ARGUMENT_PERMISSION_REQUEST_CODE, requestCode)
                    putBoolean(ARGUMENT_FINISH_ACTIVITY, finishActivity)
                }
                return RationaleDialog().apply {
                    this.arguments = arguments
                }
            }

        }
    }

}