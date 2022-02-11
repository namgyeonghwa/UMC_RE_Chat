package com.chat_soon_e.re_chat.data.remote.user

import retrofit2.Call
import retrofit2.http.*

interface UserRetrofitInterface {
    // 카카오 회원 추가하기
    @POST("/app/users/user")
    fun addKakaoUser(@Body kakaoUserIdx: Long): Call<UserResponse>
}