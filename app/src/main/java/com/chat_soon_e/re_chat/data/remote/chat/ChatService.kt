package com.chat_soon_e.re_chat.data.remote.chat

import android.util.Log
import com.chat_soon_e.re_chat.ApplicationClass.Companion.retrofit
import com.chat_soon_e.re_chat.data.entities.ChatList
import com.chat_soon_e.re_chat.ui.view.*

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.sql.Date
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// 레트로핏 사용하는 함수들 따로 모아서!(api사용하는 것을) 모듈화
class ChatService {
    private val tag = "SERVICE/CHAT"

    // 채팅 추가하기
    fun addChat(addChatView: AddChatView, userIdx: Long, chat: Chat) {
        val chatService = retrofit.create(ChatRetrofitInterface::class.java)

        chatService.addChat(userIdx, chat).enqueue(object: Callback<ChatResponse> {
            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                val resp = response.body()!!
                Log.d(tag, "addChat()")

                when(resp.code) {
                    1000 -> addChatView.onAddChatSuccess()
                    else -> addChatView.onAddChatFailure(resp.code, resp.message)
                }
            }

            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                Log.d(tag, t.message.toString())
                addChatView.onAddChatFailure(400, "네트워크 오류")
            }
        })
    }

    // 전체 채팅목록 가져오기 (메인화면)
    fun getChatList(getChatListView: GetChatListView, userIdx: Long) {
        val chatList = ArrayList<ChatList>()
        val chatService = retrofit.create(ChatRetrofitInterface::class.java)

        chatService.getChatList(userIdx).enqueue(object: Callback<ChatResponse> {
            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                val resp = response.body()!!

                when(resp.code) {
                    1000 -> {
                        val jsonArray = resp.result
                        if(jsonArray != null) {
                            // JsonArray parsing
                            for(i in 0 until jsonArray.size()) {
                                val jsonElement = jsonArray.get(i)
                                val chatIdx = jsonElement.asJsonObject.get("chatIdx").asInt
                                val nickname = jsonElement.asJsonObject.get("chat_name").asString
                                val groupName = if(jsonElement.asJsonObject.get("groupName").isJsonNull) null else jsonElement.asJsonObject.get("groupName").asString
                                val profileImg = if(jsonElement.asJsonObject.get("profile_image").isJsonNull) null else jsonElement.asJsonObject.get("profile_image").asString
                                val postTime = jsonElement.asJsonObject.get("latest_time").asString
                                val message = jsonElement.asJsonObject.get("latest_message").asString

                                Log.d(tag, "postTime: ${postTime.isNullOrEmpty()}")
                                Log.d(tag, "postTime: $postTime")

                                val chat = ChatList(chatIdx, nickname, groupName, profileImg, postTime, message, 1)
                                chatList.add(chat)
                                Log.d(tag, "chatList: $chatList")
                            }
                        }
                        getChatListView.onGetChatListSuccess(chatList)
                        Log.d(tag, "$chatList")
                    }
                    else -> getChatListView.onGetChatListFailure(resp.code, resp.message)
                }
            }

            // 네트워크 자체가 실패한 경우
            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                Log.d(tag, t.message.toString())
                getChatListView.onGetChatListFailure(400, "네트워크 오류")
            }
        })
    }

    // 갠톡 or 단톡 채팅 가져오기
    fun getChat(getChatView: GetChatView, userIdx: Long, chatIdx: Int, groupName: String?) {
        val chatList = ArrayList<Chat>()
        val chatService = retrofit.create(ChatRetrofitInterface::class.java)

        // 응답 처리
        chatService.getChat(userIdx, chatIdx, groupName).enqueue(object: Callback<ChatResponse> {
            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                val resp = response.body()!!

                when(resp.code) {
                    1000 -> {
                        // 응답 성공했을 때 response parameters parsing
                        val jsonArray = resp.result
                        if(jsonArray != null) {
                            // JsonArray Parsing
                            for(i in 0 until jsonArray.size()) {
                                val jsonElement = jsonArray.get(i)
                                val nickname = jsonElement.asJsonObject.get("nickname").asString
                                val profileImgUrl = if(jsonElement.asJsonObject.get("profileImgUrl").isJsonNull) null else jsonElement.asJsonObject.get("profileImgUrl").asString
                                val message = jsonElement.asJsonObject.get("message").asString
                                val postTime = jsonElement.asJsonObject.get("postTime").asString
                                val groupName = if(jsonElement.asJsonObject.get("groupName").isJsonNull) null else jsonElement.asJsonObject.get("groupName").asString

                                val chat = Chat(nickname, groupName, profileImgUrl, message, postTime)
                                chatList.add(chat)
                                Log.d(tag, "getChat()/chat: $chat")
                                Log.d(tag, "getChat()/chatList: $chatList")
                            }
                            getChatView.onGetChatSuccess(chatList)
                            Log.d(tag, "getChat()/onResponse()/success")
                        }
                    }
                    else -> getChatView.onGetChatFailure(resp.code, resp.message)
                }
            }

            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                Log.d(tag, t.message.toString())
                getChatView.onGetChatFailure(400, "네트워크 오류")
            }
        })
    }
}