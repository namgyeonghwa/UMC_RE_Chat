package com.chatsoone.rechat.ui.permission

import android.content.Intent
import android.util.Log
import com.chatsoone.rechat.ApplicationClass.Companion.ACT
import com.chatsoone.rechat.ApplicationClass.Companion.currentWindowMetricsPointCompat
import com.chatsoone.rechat.NotificationListener
import com.chatsoone.rechat.base.BaseActivity
import com.chatsoone.rechat.databinding.ActivityPermissionBinding
import com.chatsoone.rechat.util.permissionGrantred

class PermissionActivity :
    BaseActivity<ActivityPermissionBinding>(ActivityPermissionBinding::inflate) {

    override fun initAfterBinding() {
        getPermission()
        initImageSize()
    }

    private fun getPermission() {
        // 권한 얻기 버튼
        binding.notificationPermissionBtn.setOnClickListener {
            startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))

            // 권한을 얻었다면 서비스 시작
            if (permissionGrantred(this)) Log.d(ACT, "PERMISSION/inPermission")
            startService(Intent(this, NotificationListener::class.java))
            finish()
        }
    }

    private fun initImageSize() {
        val size = windowManager.currentWindowMetricsPointCompat()
        val width = (size.x * 0.6f).toInt()
        val height = (size.y * 0.3f).toInt()

        binding.permissionBackgroundImgIv.maxWidth = width
        binding.permissionBackgroundImgIv.maxHeight = height
    }
}
