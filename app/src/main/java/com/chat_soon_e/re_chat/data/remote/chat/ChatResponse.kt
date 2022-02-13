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

//채팅 추가하기
data class AddChatResponse(@SerializedName("isSuccess") val isSuccess: Boolean, @SerializedName("code") val code: Int, @SerializedName("message") val message: String)
data class Chat(
    @SerializedName("nickname") var chatName: String,
    @SerializedName("groupName") var groupName: String?,
    @SerializedName("profileImgUrl") var profileImgUrl: String?,
    @SerializedName("message") var message: String,
    @SerializedName("postTime") var postTime: String
)

// 폴더 안의 채팅 가져오기
data class FolderContent(
    @SerializedName("folderName") val folderName: String,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("profileImgUrl") val profileImgUrl: String?,
    @SerializedName("message") val message: String,
    @SerializedName("chat_date") val chatDate: String?,
    @SerializedName("post_time") val postTime: String
)

// 차단된 톡방 목록 가져오기
data class BlockedChatList(
    @SerializedName("blocked_name") val blockedName: String,
    @SerializedName("blocked_profileImg") val blockedProfileImg: String,
    @SerializedName("groupName") val groupName: String?,
    @SerializedName("status") val status: String,
    @SerializedName("postTime") var postTime: Date
)

// 전체 채팅 목록 가져오기 (메인화면)
data class ChatListResponse(@SerializedName("isSuccess") val isSuccess: Boolean, @SerializedName("code") val code: Int, val message: String, val result: JsonArray?)

// 갠톡 or 단톡 채팅 가져오기
data class Chatas(
    val nickname:String,
    val profileImgUrl:String,
    val message: String,
    //@SerializedName("chat_date")val chatDate:Date,
    @SerializedName("post_time")val postTime:Date,
)

// 폴더 안의 채팅 가져오기
data class FolderChatResponse(@SerializedName("isSuccess")val isSuccess: Boolean, @SerializedName("code") val code:Int, val message: String, val result:JsonArray?)
data class FolderChat(
    val folderName:String,
    val nickname:String,
    val profileImgUrl: String,
    val message:String,
    @SerializedName("chat_date")val chatDate:Date,
    @SerializedName("post_time")val postTime:Date,
)