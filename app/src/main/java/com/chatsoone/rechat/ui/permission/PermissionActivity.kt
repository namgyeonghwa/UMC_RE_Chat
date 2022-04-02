package com.chatsoone.rechat.ui.permission

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.chatsoone.rechat.ApplicationClass.Companion.ACT
import com.chatsoone.rechat.ApplicationClass.Companion.currentWindowMetricsPointCompat
import com.chatsoone.rechat.NotificationListener
import com.chatsoone.rechat.databinding.ActivityPermissionBinding
import com.chatsoone.rechat.util.permissionGrantred

class PermissionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPermissionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPermissionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getPermission()
        initImageSize()
    }

    private fun getPermission() {
        // 권한 얻기 버튼
        binding.notificationPermissionBtn.setOnClickListener {
            startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))

            // 권한을 얻었다면 서비스 시작
            if (permissionGrantred(this)) Log.d(ACT, "PERMISSION/inPermission")
            startForegroundService(Intent(this, NotificationListener::class.java))
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