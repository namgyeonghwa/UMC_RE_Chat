package com.chat_soon_e.re_chat.data.remote.chat

import com.google.gson.JsonArray
import com.google.gson.annotations.SerializedName
import java.util.*

// 자바의 데이터 클래스 멤버변수 이름은 camelCase 사용
// default
data class ChatResponse(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: JsonArray?
)

// 채팅 추가하기 (서버와의 통신을 위해)
data class Chat(
    @SerializedName("nickname") var chatName: String,
    @SerializedName("groupName") var groupName: String?,
    @SerializedName("profileImgUrl") var profileImgUrl: String?,
    @SerializedName("message") var message: String,
    @SerializedName("postTime") var postTime: String
)

// 폴더 안의 채팅 가져오기
data class FolderChat(
    @SerializedName("folderName") val folderName: String,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("profileImgUrl") val profileImgUrl: String?,
    @SerializedName("message") val message: String,
    @SerializedName("chat_date") val chatDate: String,
    @SerializedName("post_time") val postTime: String
)