package com.chatsoone.rechat.util

import android.content.Context
import androidx.core.app.NotificationManagerCompat

fun permissionGrantred(context: Context): Boolean {
    return NotificationManagerCompat.getEnabledListenerPackages(context).any { enabledPackageName ->
        enabledPackageName == context.packageName
    }
}