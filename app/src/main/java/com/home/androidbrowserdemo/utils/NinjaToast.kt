package com.home.androidbrowserdemo.utils

import android.app.Activity
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.home.androidbrowserdemo.R


object NinjaToast {

    fun show(context: Context?, stringResId: Int) {
        show(context, context!!.getString(stringResId))
    }

    fun show(context: Context?, text: String) {
        val activity = context as Activity
        val inflater = activity.layoutInflater
        val layout = inflater.inflate(
            R.layout.dialog_bottom,
            activity.findViewById<View>(R.id.dialog_toast) as ViewGroup
        )
        val dialogText = layout.findViewById<TextView>(R.id.dialog_text)
        dialogText.text = text
        val toast = Toast(activity.applicationContext)
        toast.setGravity(Gravity.BOTTOM or Gravity.FILL_HORIZONTAL, 0, 0)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        toast.show()
    }
}
