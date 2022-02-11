package com.chat_soon_e.re_chat.data.remote.chat

import android.util.Log
import com.chat_soon_e.re_chat.ApplicationClass.Companion.retrofit
import com.chat_soon_e.re_chat.data.entities.ChatList
import com.chat_soon_e.re_chat.ui.view.*

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// 레트로핏 사용하는 함수들 따로 모아서!(api사용하는 것을) 모듈화
class ChatService {
    // 채팅 추가하기
    fun addChat(addChatView: AddChatView, userIdx: Long, chat: Chat) {
        val chatService = retrofit.create(ChatRetrofitInterface::class.java)

        // 응답 처리
        chatService.addChat(userIdx, chat).enqueue(object: Callback<AddChatResponse> {
            // 응답이 왔을 때
            override fun onResponse(call: Call<AddChatResponse>, response: Response<AddChatResponse>) {
                val resp = response.body()!!
                Log.d("CHAT-API", "addChat()")

                when(resp.code) {
                    1000 -> addChatView.onAddChatSuccess()
                    else -> addChatView.onAddChatFailure(resp.code, resp.message)
                }
            }

            // 네트워크 자체가 실패한 경우
            override fun onFailure(call: Call<AddChatResponse>, t: Throwable) {
                Log.d("CHAT-API", t.message.toString())
                addChatView.onAddChatFailure(400, "네트워크 오류")
            }
        })
    }

    // 전체 채팅목록 가져오기 (메인화면)
    fun getChatList(getChatListView: GetChatListView, userIdx: Long) {
        val chatList = ArrayList<ChatList>()
        val chatService = retrofit.create(ChatRetrofitInterface::class.java)

        // 응답 처리
        chatService.getChatList(userIdx).enqueue(object: Callback<ChatListResponse> {
            // 응답이 왔을 때
            override fun onResponse(call: Call<ChatListResponse>, response: Response<ChatListResponse>) {
                val resp = response.body()!!

                when(resp.code) {
                    1000 -> {
                        val jsonArray = resp.result
                        if(jsonArray != null) {
                            // JSONArray parsing
                            for(i in 0 until jsonArray.size()) {
                                val jsonElement = jsonArray.get(i)
                                val chatIdx = jsonElement.asJsonObject.get("chatIdx").asInt
                                val nickname = jsonElement.asJsonObject.get("chat_name").asString
                                val groupName = if(jsonElement.asJsonObject.get("groupName").isJsonNull) null else jsonElement.asJsonObject.get("groupName").asString
                                val profileImg = if(jsonElement.asJsonObject.get("profile_image").isJsonNull) null else jsonElement.asJsonObject.get("profile_image").asString
                                val postTime = if(jsonElement.asJsonObject.get("latest_time").isJsonNull) null else jsonElement.asJsonObject.get("latest_time").asString
                                val message = jsonElement.asJsonObject.get("latest_message").asString
                                val chat = ChatList(chatIdx, nickname, groupName, profileImg, postTime, message, 1)
                                chatList.add(chat)
                                Log.d("CHAT-API", "chatList: $chatList")
                            }
                        }
                        getChatListView.onGetChatListSuccess(chatList)
                        Log.d("Notifi", "$chatList")
                    }
                    else -> getChatListView.onGetChatListFailure(resp.code, resp.message)
                }
            }

            // 네트워크 자체가 실패한 경우
            override fun onFailure(call: Call<ChatListResponse>, t: Throwable) {
                Log.d("CHAT-API/ERROR", t.message.toString())
                getChatListView.onGetChatListFailure(400, "네트워크 오류")
            }
        })
    }
}