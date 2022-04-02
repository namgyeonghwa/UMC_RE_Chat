package com.chatsoone.rechat

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Insets
import android.graphics.Point
import android.os.Build
import android.view.WindowInsets
import android.view.WindowManager
import androidx.annotation.RequiresApi
import java.io.File

class ApplicationClass : Application() {
    companion object {
        const val USER_INFO: String = "USER_LOGIN_INFO"
        lateinit var mSharedPreferences: SharedPreferences

        // log
        const val TAG: String = "RE:CHAT-APP"
        const val ACT: String = "ACT/"
        const val FRAG: String = "FRAG/"

        // database
        const val APP_DATABASE = "$TAG-DB"
        const val ACTIVE: String = "ACTIVE"
        const val INACTIVE: String = "INACTIVE"
        const val BLOCKED: String = "BLOCKED"
        const val DELETED: String = "DELETED"
        const val HIDDEN: String = "HIDDEN"

        // server api
//        const val BASE_URL: String = DEV_URL

        // 공통 변수
        var count: Int = 0

        // 캐시 저장소로부터 이미지 불러오기
        // 추후 진짜 캐시를 사용해 볼 것
        fun loadBitmap(name: String, context: Context): Bitmap? {
            val file = File(context.cacheDir.toString())
            val files = file.listFiles()
            var list: String = ""
            for (tempFile in files) {
                //Log.d("MyTag", tempFile.name)
                //name이 들어가 있는 파일 찾기
                if (tempFile.name.contains(name)) {
                    list = tempFile.name
                }
            }
            val path = context.cacheDir.toString() + "/" + list
            return BitmapFactory.decodeFile(path)
        }

        // 디바이스 크기에 따라 사이즈 변경
        fun WindowManager.currentWindowMetricsPointCompat(): Point {
            return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                val windowInsets = currentWindowMetrics.windowInsets
                var insets: Insets = windowInsets.getInsets(WindowInsets.Type.navigationBars())

                windowInsets.displayCutout?.run {
                    insets = Insets.max(insets, Insets.of(safeInsetLeft, safeInsetTop, safeInsetRight, safeInsetBottom))
                }

                val insetsWidth = insets.right + insets.left
                val insetsHeight = insets.top + insets.bottom
                Point(currentWindowMetrics.bounds.width() - insetsWidth, currentWindowMetrics.bounds.height() - insetsHeight)
            } else {
                Point().apply {
                    defaultDisplay.getSize(this)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate() {
        super.onCreate()
        mSharedPreferences = applicationContext.getSharedPreferences(TAG, Context.MODE_PRIVATE)
    }
}