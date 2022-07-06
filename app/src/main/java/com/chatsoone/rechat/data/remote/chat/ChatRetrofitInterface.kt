package com.chatsoone.rechat.data.remote.chat

import com.chatsoone.rechat.data.remote.Chat
import com.chatsoone.rechat.data.remote.FolderList
import com.chatsoone.rechat.data.remote.ServerResponse
import retrofit2.Call
import retrofit2.http.*

interface ChatRetrofitInterface {
    // 채팅 추가하기
    @POST("/app/chats/{kakaoUserIdx}/chat")
    fun addChat(
        @Path("kakaoUserIdx") kakaoUserIdx: Long,
        @Body chat: Chat
    ): Call<ServerResponse>

    // 전체 채팅목록 가져오기 (메인화면)
    @GET("/app/chats/{kakaoUserIdx}/chatlist")
    fun getChatList(
        @Path("kakaoUserIdx") kakaoUserIdx: Long
    ): Call<ServerResponse>

    // 갠톡 or 단톡 채팅 가져오기
    @GET("/app/chats/{kakaoUserIdx}/chats")
    fun getChat(
        @Path("kakaoUserIdx") kakaoUserIdx: Long,
        @Query("chatIdx") chatIdx: Int,
        @Query("groupName") groupName: String? = null   // null이면 갠톡
    ): Call<ServerResponse>

    // 폴더 안의 채팅 가져오기
    @GET("/app/chats/{kakaoUserIdx}/folder-chats")
    fun getFolderContent(
        @Path("kakaoUserIdx") kakaoUserIdx: Long,
        @Query("folderIdx") folderIdx: Int
    ): Call<ServerResponse>

    // 선택한 채팅 삭제하기
    @DELETE("/app/chats/{kakaoUserIdx}/chat")
    fun deleteChat(
        @Path("kakaoUserIdx") kakaoUserIdx: Long,
        @Query("chatIdx") chatIdx: Int
    ): Call<ServerResponse>

    // 선택한 채팅목록의 모든 채팅 삭제하기
    @DELETE("/app/chats/{kakaoUserIdx}/chats")
    fun deleteChatList(
        @Path("kakaoUserIdx") kakaoUserIdx: Long,
        @Query("chatIdx") chatIdx: Int,
        @Query("groupName") groupName: String?
    ): Call<ServerResponse>

    // 폴더에 채팅 추가하기
    @POST("/app/chats/{kakaoUserIdx}/folder-chat")
    fun addChatToFolder(
        @Path("kakaoUserIdx") kakaoUserIdx: Long,
        @Query("chatIdx") chatIdx: Int,
        @Body folder: FolderList
    ): Call<ServerResponse>

    // 폴더에 채팅목록의 채팅(갠톡/단톡) 모두 추가하기
    @POST("/app/chats/{kakaoUserIdx}/folder-chats")
    fun addChatListToFolder(
        @Path("kakaoUserIdx") kakaoUserIdx: Long,
        @Query("chatIdx") chatIdx: Int,
        @Query("groupName") groupName: String?,
        @Body folder: FolderList
    ): Call<ServerResponse>

    // 폴더에서 채팅 삭제하기
    @DELETE("/app/chats/{kakaoUserIdx}/folder-chat")
    fun deleteChatFromFolder(
        @Path("kakaoUserIdx") kakaoUserIdx: Long,
        @Query("chatIdx") chatIdx: Int,
        @Query("folderIdx") folderIdx: Int
    ): Call<ServerResponse>

    // 채팅목록/유저 차단하기
    @PATCH("/app/chats/{kakaoUserIdx}/block")
    fun block(
        @Path("kakaoUserIdx") kakaoUserIdx: Long,
        @Query("chatName") chatName: String,
        @Query("groupName") groupName: String?
    ): Call<ServerResponse>

    // 채팅목록/유저 차단 해제하기
    @PATCH("/app/chats/{kakaoUserIdx}/unblock")
    fun unblock(
        @Path("kakaoUserIdx") kakaoUserIdx: Long,
        @Query("chatName") chatName: String,
        @Query("groupName") groupName: String?
    ): Call<ServerResponse>

    // 차단된 톡방 목록 가져오기
    @GET("/app/chats/{kakaoUserIdx}/blocked-chatlist")
    fun getBlockedChatList(
        @Path("kakaoUserIdx") kakaoUserIdx: Long
    ): Call<ServerResponse>
}
