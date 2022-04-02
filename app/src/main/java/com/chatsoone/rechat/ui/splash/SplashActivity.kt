package com.chatsoone.rechat.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.chatsoone.rechat.ApplicationClass.Companion.ACTIVE
import com.chatsoone.rechat.ApplicationClass.Companion.mSharedPreferences
import com.chatsoone.rechat.data.entity.User
import com.chatsoone.rechat.data.local.AppDatabase
import com.chatsoone.rechat.data.remote.USER_ID
import com.chatsoone.rechat.databinding.ActivitySplashBinding
import com.chatsoone.rechat.ui.explain.ExplainActivity
import com.chatsoone.rechat.ui.main.MainActivity
import com.chatsoone.rechat.ui.permission.PermissionActivity
import com.chatsoone.rechat.util.permissionGrantred
import com.chatsoone.rechat.util.saveID
import com.kakao.sdk.user.UserApiClient

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private val tag = "ACT/SPLASH"
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // defaultValue = 0, 설명창 보임 = 1, 더 이상 보이지 않음 = 2
        mSharedPreferences = getSharedPreferences("explain", MODE_PRIVATE)
        val isExplain = mSharedPreferences.getInt("explain", 0)

        if (isExplain == 0 || isExplain == 1) {
            val intent = Intent(this@SplashActivity, ExplainActivity::class.java)
            startActivity(intent)
        } else if (isExplain == 2) {
            if (!permissionGrantred(this)) {
                val intent = Intent(this@SplashActivity, PermissionActivity::class.java)
                startActivity(intent)
            }
        }

        binding.splashStartBtn.setOnClickListener {
            USER_ID = -1
            saveID(USER_ID)
            AppDatabase.getInstance(this)!!.userDao().insert(User(-1, null, null, ACTIVE))

            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 로그인이 되었다면 로그인은 안뜨게 == O
        // 데이터 다운이 완료되면 시작하기 버튼 활성화 == X
    }

    // User 정보 업데이트 및 생성
    private fun saveUserInfo(state: String) {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.d(tag, "사용자 정보 가져오기 실패")
            } else {
                if (user != null) {
                    val database = AppDatabase.getInstance(this)!!
                    val dao = database.userDao()
                    if (state == "login") {
                        // id 암호화(encrypted 사용) 후 spf 저장, 일단은 그냥 local 사용해 저장
                        // ----------------------------------
                        USER_ID = user.id
                        saveID(user.id)

                        Log.d(tag, "user id: ${user.id}")
                        // ----------------------------------

                        val users = dao.getUser(user.id)
                        if (users == null) {
                            // 유저 인포 저장
                            dao.insert(
                                User(
                                    user.id,
                                    user.kakaoAccount?.profile?.nickname.toString(),
                                    user.kakaoAccount?.email.toString(),
                                    ACTIVE
                                )
                            )

                        } else {
                            if (users.status == "delete")
                            // 유저 인포 업데이트
                                dao.update(
                                    User(
                                        user.id,
                                        user.kakaoAccount?.profile?.nickname.toString(),
                                        user.kakaoAccount?.email.toString(),
                                        ACTIVE
                                    )
                                )
                        }
                    }
                    // 로그아웃 시
                    else if (state == "logout") {
                        saveID(-1)
                        // dao.updateStatus(user.id, "inactivate")
                    }
                    // 탈퇴 시
                    else if (state == "withdraw")
                        saveID(-1)
                    // dao.updateStatus(user.id, "delete")
                }
            }
        }
    }

    override fun onBackPressed() {
        finish()
    }
}