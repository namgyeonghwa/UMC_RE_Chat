package com.chat_soon_e.re_chat.data.remote.user

import android.util.Log
import com.chat_soon_e.re_chat.ApplicationClass.Companion.retrofit
import com.chat_soon_e.re_chat.ui.view.UserView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// 레트로핏(API) 사용하는 함수들 따로 모아서 모듈화
class UserService {
    private val tag = "SERVICE/USER"

    // 카카오 회원 추가하기
    fun addKakaoUser(userView: UserView, userIdx: Long) {
        val userService = retrofit.create(UserRetrofitInterface::class.java)

        // 응답 처리
        userService.addKakaoUser(userIdx).enqueue(object: Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                val resp = response.body()!!
                Log.d(tag, "onResponse() in addKakaoUser()")

                when(resp.code) {
                    1000 -> userView.onAddKakaoUserSuccess()
                    else -> userView.onAddKakaoUserFailure(resp.code, resp.message)
                }
            }

            // 응답 실패
            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Log.d(tag, "onFailure() in addKakaoUser()")
                userView.onAddKakaoUserFailure(400, t.message.toString())
            }
        })
    }
}