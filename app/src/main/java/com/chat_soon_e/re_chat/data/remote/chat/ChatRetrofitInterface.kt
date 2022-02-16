package com.chat_soon_e.re_chat.data.remote.chat

import retrofit2.Call
import retrofit2.http.*

interface ChatRetrofitInterface {
    // 채팅 추가하기
    @POST("/app/chats/{kakaoUserIdx}/chat")
    fun addChat(
        @Path("kakaoUserIdx") kakaoUserIdx: Long,
        @Body chat: Chat
    ): Call<ChatResponse>

    // 전체 채팅목록 가져오기 (메인화면)
    @GET("/app/chats/{kakaoUserIdx}/chatlist")
    fun getChatList(
        @Path("kakaoUserIdx") kakaoUserIdx: Long
    ): Call<ChatResponse>

    // 갠톡 or 단톡 채팅 가져오기
    @GET("/app/chats/{kakaoUserIdx}/chats")
    fun getChat(
        @Path("kakaoUserIdx") kakaoUserIdx: Long,
        @Query("chatIdx") chatIdx: Int,
        @Query("groupName") groupName: String? = null   // null이면 갠톡
    ): Call<ChatResponse>

    // 폴더 안의 채팅 가져오기
    @GET("/app/chats/{kakaoUserIdx}/folder-chats")
    fun getFolderContent(
        @Path("kakaoUserIdx") kakaoUserIdx: Long,
        @Query("folderIdx") folderIdx: Int
    ): Call<ChatResponse>

    // 선택한 채팅 삭제하기
    @DELETE("/app/chats/{kakaoUserIdx}/chat")
    fun deleteChat(
        @Path("kakaoUserIdx") kakaoUserIdx: Long,
        @Query("chatIdx") chatIdx: Int
    ): Call<ChatResponse>

    // 선택한 채팅목록의 모든 채팅 삭제하기
    @DELETE("/app/chats/{kakaoUserIdx}/chats")
    fun deleteChatList(
        @Path("kakaoUserIdx") kakaoUserIdx: Long,
        @Query("chatIdx") chatIdx: Int,
        @Query("groupName") groupName: String?
    ): Call<ChatResponse>

    // 폴더에 채팅 추가하기
    @POST("/app/chats/{kakaoUserIdx}/folder-chat")
    fun addChatToFolder(
        @Path("kakaoUserIdx") kakaoUserIdx: Long,
        @Query("chatIdx") chatIdx: Int,
        @Body folderInx: Int
    ): Call<ChatResponse>

    // 폴더에 채팅목록의 채팅(갠톡/단톡) 모두 추가하기
    @POST("/app/chats/{kakaoUserIdx}/folder-chats")
    fun addChatListToFolder(
        @Path("kakaoUserIdx") kakaoUserIdx: Long,
        @Query("chatIdx") chatIdx: Int,
        @Query("groupName") groupName: String?,
        @Body folderIdx: Int
    ): Call<ChatResponse>

    // 폴더에서 채팅 삭제하기
    @DELETE("/app/chats/{kakaoUserIdx}/folder-chat")
    fun deleteChatFromFolder(
        @Path("kakaoUserIdx") kakaoUserIdx: Long,
        @Query("chatIdx") chatIdx: Int,
        @Query("folderIdx") folderIdx: Int
    ): Call<ChatResponse>

    // 채팅목록/유저 차단하기
    @PATCH("/app/chats/{kakaoUserIdx}/block")
    fun block(
        @Path("kakaoUserIdx") kakaoUserIdx: Long,
        @Query("chatName") chatName: String,
        @Query("groupName") groupName: String?
    ): Call<ChatResponse>

    // 채팅목록/유저 차단 해제하기
    @PATCH("/app/chats/{kakaoUserIdx}/unblock")
    fun unblock(
        @Path("kakaoUserIdx") kakaoUserIdx: Long,
        @Query("chatName") chatName: String,
        @Query("groupName") groupName: String?
    ): Call<ChatResponse>

    // 차단된 톡방 목록 가져오기
    @GET("/app/chats/{kakaoUserIdx}/blocked-chatlist")
    fun getBlockedChatList(
        @Path("kakaoUserIdx") kakaoUserIdx: Long
    ): Call<ChatResponse>

    //갠톡 or 단톡 채팅 가져오기
    @GET("/app/chat/{kakaoUserIdx}")
    fun getChat(@Path("kakaoUserIdx")kakaoUserIdx: Int, @Query("otherUserIdx")otherUserIdx:Int?=null, @Query("groupName")groupName:String?=null):Call<ChatResponse>

    //폴더 안의 채팅 리스트 가져오기
    @GET("/app/chat-folder/{kakaoUserIdx}")
    fun getFolderChat(@Path("kakaoUserIdx")kakaoUserIdx:Int, @Query("folderIdx")folderIdx:Int):Call<FolderChatResponse>
}