package com.chatsoone.rechat.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.chatsoone.rechat.ApplicationClass.Companion.ACT
import com.chatsoone.rechat.ApplicationClass.Companion.ACTIVE
import com.chatsoone.rechat.ApplicationClass.Companion.mSharedPreferences
import com.chatsoone.rechat.base.BaseActivity
import com.chatsoone.rechat.data.local.AppDatabase
import com.chatsoone.rechat.data.remote.USER_ID
import com.chatsoone.rechat.data.remote.User
import com.chatsoone.rechat.data.remote.user.UserService
import com.chatsoone.rechat.databinding.ActivitySplashBinding
import com.chatsoone.rechat.ui.explain.ExplainActivity
import com.chatsoone.rechat.ui.main.MainActivity
import com.chatsoone.rechat.ui.permission.PermissionActivity
import com.chatsoone.rechat.ui.view.UserView
import com.chatsoone.rechat.util.permissionGrantred
import com.chatsoone.rechat.util.saveID
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.common.model.KakaoSdkError
import com.kakao.sdk.user.UserApiClient

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>(ActivitySplashBinding::inflate),
    UserView {

    override fun initAfterBinding() {
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

        loginPermission()

        binding.splashKakaoBtn.setOnClickListener {
            if (binding.splashKakaoBtn.isVisible) {
                login()
            }
        }

        binding.splashStartBtn.setOnClickListener {
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 로그인이 되었다면 로그인은 안뜨게 == O
        // 데이터 다운이 완료되면 시작하기 버튼 활성화 == X
    }

    // Token 존재 확인, 즉 로그인 확인
    private fun loginPermission() {
        if (AuthApiClient.instance.hasToken()) {
            // Token 유효성 검증
            UserApiClient.instance.accessTokenInfo { _, error ->
                if (error != null) {
                    // 로그인 필요
                    if (error is KakaoSdkError && error.isInvalidTokenError()) {
//                        binding.splashKakaoIv.visibility = View.VISIBLE
                        binding.splashKakaoBtn.visibility = View.VISIBLE
                    }
                    // 기타 에러
                    else {
                        Log.d(ACT, "SPLASH/${error.message}")
                    }
                }
                // 토큰 유효성 체크 성공 (필요 시 토큰 갱신됨)
                else {
                    Log.d(ACT, "SPLASH/Valid token")
                    Log.d(ACT, "SPLASH/user id in loginPermission: $USER_ID")
                    binding.splashKakaoBtn.visibility = View.INVISIBLE
                    saveUserInfo("login")
//                    binding.splashKakaoIv.visibility = View.INVISIBLE
                }
            }
        }
        // 토큰 없음 (로그아웃 혹은 연결 끊김)
        else {
//            binding.splashKakaoIv.visibility=View.VISIBLE
            binding.splashKakaoBtn.visibility = View.VISIBLE
        }
    }

    // 카카오계정 로그인
    private fun login() {
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e(ACT, "SPLASH/카카오계정으로 로그인 실패", error)
            } else if (token != null) {
                Log.i(ACT, "SPLASH/카카오계정으로 로그인 성공 ${token.accessToken}")
//                binding.splashKakaoIv.visibility = View.INVISIBLE
                runOnUiThread {
                    binding.splashKakaoBtn.visibility = View.INVISIBLE
                }
                saveUserInfo("login")
            }
        }

        // 카카오톡 로그인 가능하다면 카카오톡으로 로그인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                if (error != null) {
                    Log.e(ACT, "SPLASH/카카오톡으로 로그인 실패", error)
                    // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                    // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }
                    // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                    UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
                } else if (token != null) {
                    Log.i(ACT, "SPLASH/카카오톡으로 로그인 성공 ${token.accessToken}")
//                    binding.splashKakaoIv.visibility=View.INVISIBLE
                    runOnUiThread {
                        binding.splashKakaoBtn.visibility = View.INVISIBLE
                    }
                    saveUserInfo("login")
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }
    }

    // User 정보 업데이트 및 생성
    private fun saveUserInfo(state: String) {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.d(ACT, "SPLASH/사용자 정보 가져오기 실패")
            } else {
                if (user != null) {
                    if (state == "login") {
                        // id 암호화(encrypted사용) 후 spf 저장
                        USER_ID = user.id
                        saveID(user.id)
                        Log.d(ACT, "SPLASH/user id: ${user.id}")

                        // Server API: 카카오 회원 추가하기
                        val kakaoUserIdx = User(user.id)
                        val userService = UserService()
                        userService.addKakaoUser(this, kakaoUserIdx)

                        Log.d(ACT, "SPLASH/Server API: ${user.id}")
                    } else if (state == "logout") {
                        saveID(-1)
                    } else if (state == "withdraw")
                        saveID(-1)
                }
            }
        }
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onAddKakaoUserSuccess() {
        // 카카오 회원 추가하기 성공
        Log.d(ACT, "SPLASH/onAddKakaoUserSuccess")
    }

    override fun onAddKakaoUserFailure(code: Int, message: String) {
        when (code) {
            3100 -> Log.d(ACT, "SPLASH/$message")
            4000 -> Log.d(ACT, "SPLASH/$message")
            4001 -> Log.d(ACT, "SPLASH/$message")
            else -> Log.d(ACT, "SPLASH/onAddKakaoUserFailure: other error")
        }
    }
}
