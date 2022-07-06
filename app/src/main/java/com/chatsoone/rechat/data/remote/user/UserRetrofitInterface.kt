package com.chatsoone.rechat.data.remote.user

import com.chatsoone.rechat.data.remote.ServerResponse
import com.chatsoone.rechat.data.remote.User
import retrofit2.Call
import retrofit2.http.*

interface UserRetrofitInterface {
    // 카카오 회원 추가하기
    @POST("/app/users/user")
    fun addKakaoUser(@Body user: User): Call<ServerResponse>
}
