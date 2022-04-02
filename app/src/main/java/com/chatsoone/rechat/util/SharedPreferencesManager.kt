package com.chatsoone.rechat.util

import com.chatsoone.rechat.ApplicationClass

fun saveID(user_id: Long) {
    val editor = ApplicationClass.mSharedPreferences.edit()
    editor.putLong(ApplicationClass.USER_INFO, user_id)
    editor.apply()
}

fun getID(): Long = ApplicationClass.mSharedPreferences.getLong(ApplicationClass.USER_INFO, -1)