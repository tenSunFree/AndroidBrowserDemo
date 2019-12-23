package com.home.androidbrowserdemo.view.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import java.util.*

class HolderActivity : Activity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = Objects.requireNonNull(intent.data).toString()
        val toActivity = Intent(this@HolderActivity, MainActivity::class.java)
        toActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        toActivity.action = Intent.ACTION_SEND
        toActivity.putExtra(Intent.EXTRA_TEXT, url)
        startActivity(toActivity)
        finish()
    }
}
