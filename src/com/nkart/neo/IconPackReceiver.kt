package com.nkart.neo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import app.lawnchair.preferences.PreferenceManager.Companion.getInstance

class IconPackReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val i = intent.extras
        val pkgName = i!!["pkgname"] as String
        Log.d("launcherThemeReceiver", pkgName)
        val prefs = getInstance(context)
        prefs.iconPackPackage.set(pkgName)

    }
}