package com.chatsoone.rechat

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Insets
import android.graphics.Point
import android.os.Build
import android.util.SparseBooleanArray
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.chatsoone.rechat.ui.main.MainActivity
import com.chatsoone.rechat.ui.main.home.HomeRVAdapter
import com.kakao.sdk.common.KakaoSdk
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ApplicationClass : Application() {
    companion object {
        const val X_ACCESS_TOKEN: String = "X-ACCESS-TOKEN" // JWT token key
        const val USER_INFO: String = "USER_LOGIN_INFO"
        lateinit var mSharedPreferences: SharedPreferences

        // log
        const val TAG: String = "RE:CHAT-APP"
        const val ACT: String = "ACT/"
        const val FRAG: String = "FRAG/"
        const val RV: String = "RV/"
        const val SERVICE: String = "SERVICE/"

        // database
        const val APP_DATABASE = "$TAG-DB"
        const val ACTIVE: String = "ACTIVE"
        const val INACTIVE: String = "INACTIVE"
        const val BLOCKED: String = "BLOCKED"
        const val DELETED: String = "DELETED"
        const val HIDDEN: String = "HIDDEN"

        // server api
        lateinit var retrofit: Retrofit
        private const val DEV_URL: String = ""
        private const val PROD_URL: String = ""
        const val BASE_URL: String = DEV_URL

        // 캐시 저장소로부터 이미지 불러오기
        // 추후 진짜 캐시를 사용해 볼 것
        fun loadBitmap(name: String, context: Context): Bitmap? {
            val file = File(context.cacheDir.toString())
            val files = file.listFiles()
            var list: String = ""
            for (tempFile in files) {
                // name이 들어가 있는 파일 찾기
                if (tempFile.name.contains(name)) {
                    list = tempFile.name
                }
            }
            val path = context.cacheDir.toString() + "/" + list
            return BitmapFactory.decodeFile(path)
        }

        // Toast message
        fun showToast(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        @SuppressLint("SimpleDateFormat")
        fun convertDate(date: String): String {
            val str: String
            val today = Calendar.getInstance()

            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val dateAsDate = simpleDateFormat.parse(date)

            val diffDay = (today.time.time - dateAsDate!!.time) / (60 * 60 * 24 * 1000)

            str = if (diffDay < 0) {
                // 오늘인 경우
                val sdf = SimpleDateFormat("a h:m")
                sdf.format(dateAsDate).toString()
            } else {
                val time = SimpleDateFormat("M월 d일")
                time.format(dateAsDate).toString()
            }

            return str
        }

        // 디바이스 크기에 따라 사이즈 변경
        fun WindowManager.currentWindowMetricsPointCompat(): Point {
            return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                val windowInsets = currentWindowMetrics.windowInsets
                var insets: Insets = windowInsets.getInsets(WindowInsets.Type.navigationBars())

                windowInsets.displayCutout?.run {
                    insets = Insets.max(
                        insets,
                        Insets.of(safeInsetLeft, safeInsetTop, safeInsetRight, safeInsetBottom)
                    )
                }

                val insetsWidth = insets.right + insets.left
                val insetsHeight = insets.top + insets.bottom
                Point(
                    currentWindowMetrics.bounds.width() - insetsWidth,
                    currentWindowMetrics.bounds.height() - insetsHeight
                )
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

        // kakao sdk 연결
        KakaoSdk.init(this, "")

        // client definition
        // Http 통신할 때 클라이언트 옵션 설정해주는 부분
        val client: OkHttpClient = OkHttpClient.Builder()
            .readTimeout(30000, TimeUnit.MILLISECONDS)  // timeout 3초 설정
            .connectTimeout(30000, TimeUnit.MILLISECONDS)
            // 에러 떴을 때 상세한 로그 출력을 위한 부분
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
//            .addNetworkInterceptor(XAccessTokenInterceptor()) // JWT 자동 헤더 전송
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        mSharedPreferences = applicationContext.getSharedPreferences(TAG, Context.MODE_PRIVATE)
    }
}
