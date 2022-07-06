package com.chatsoone.rechat.data.remote.user

import android.util.Log
import com.chatsoone.rechat.ApplicationClass.Companion.SERVICE
import com.chatsoone.rechat.ApplicationClass.Companion.retrofit
import com.chatsoone.rechat.data.remote.*
import com.chatsoone.rechat.ui.view.UserView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserService {

    // 카카오 회원 추가하기
    fun addKakaoUser(userView: UserView, userIdx: User) {
        val userService = retrofit.create(UserRetrofitInterface::class.java)

        // 응답 처리
        userService.addKakaoUser(userIdx).enqueue(object : Callback<ServerResponse> {
            override fun onResponse(
                call: Call<ServerResponse>,
                response: Response<ServerResponse>
            ) {
                Log.d(SERVICE, "USER/addKakaoUser/onResponse")
                Log.d(SERVICE, "USER/addKakaoUSer/onResponse/userIdx: $userIdx")
                Log.d(SERVICE, "USER/addKakaoUSer/onResponse/response.body: ${response.body()}")
                Log.d(
                    SERVICE,
                    "USER/addKakaoUSer/onResponse/response.isSuccessful: ${response.isSuccessful}"
                )

                if (response.isSuccessful && response.body() != null) {
                    val resp = response.body()!!

                    when (resp.code) {
                        1000 -> userView.onAddKakaoUserSuccess()
                        else -> userView.onAddKakaoUserFailure(resp.code, resp.message)
                    }
                }
            }

            // 응답 실패
            override fun onFailure(call: Call<ServerResponse>, t: Throwable) {
                Log.d(SERVICE, "USER/onFailure in addKakaoUser")
                userView.onAddKakaoUserFailure(400, t.message.toString())
            }
        })
    }
}
