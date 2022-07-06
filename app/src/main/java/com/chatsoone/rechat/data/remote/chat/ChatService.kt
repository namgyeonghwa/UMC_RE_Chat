package com.chatsoone.rechat.data.remote.chat

import android.util.Log
import com.chatsoone.rechat.ApplicationClass.Companion.SERVICE
import com.chatsoone.rechat.ApplicationClass.Companion.retrofit
import com.chatsoone.rechat.data.remote.*
import com.chatsoone.rechat.ui.view.*

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatService {

    // 채팅 추가하기
    fun addChat(chatView: ChatView, userIdx: Long, chat: Chat) {
        val chatService = retrofit.create(ChatRetrofitInterface::class.java)

        chatService.addChat(userIdx, chat).enqueue(object : Callback<ServerResponse> {
            override fun onResponse(
                call: Call<ServerResponse>,
                response: Response<ServerResponse>
            ) {
                val resp = response.body()!!

                when (resp.code) {
                    1000 -> chatView.onChatSuccess()
                    else -> chatView.onChatFailure(resp.code, resp.message)
                }
            }

            override fun onFailure(call: Call<ServerResponse>, t: Throwable) {
                Log.d(SERVICE, "CHAT/ ${t.message}")
                chatView.onChatFailure(400, "네트워크 오류")
            }
        })
    }

    // 전체 채팅목록 가져오기 (메인화면)
    fun getChatList(getChatListView: GetChatListView, userIdx: Long) {
        val chatList = ArrayList<ChatList>()
        val chatService = retrofit.create(ChatRetrofitInterface::class.java)

        chatService.getChatList(userIdx).enqueue(object : Callback<ServerResponse> {
            // 응답이 왔을 때
            override fun onResponse(
                call: Call<ServerResponse>,
                response: Response<ServerResponse>
            ) {
                val resp = response.body()!!

                when (resp.code) {
                    1000 -> {
                        val jsonArray = resp.result
                        if (jsonArray != null) {
                            for (i in 0 until jsonArray.size()) {
                                val jsonElement = jsonArray.get(i)
                                val chatIdx = jsonElement.asJsonObject.get("chatIdx").asInt
                                val nickname = jsonElement.asJsonObject.get("chat_name").asString
                                val groupName =
                                    if (jsonElement.asJsonObject.get("groupName").isJsonNull) null else jsonElement.asJsonObject.get(
                                        "groupName"
                                    ).asString
                                val profileImg =
                                    if (jsonElement.asJsonObject.get("profile_image").isJsonNull) null else jsonElement.asJsonObject.get(
                                        "profile_image"
                                    ).asString
                                val postTime = jsonElement.asJsonObject.get("latest_time").asString
                                val message =
                                    jsonElement.asJsonObject.get("latest_message").asString
                                Log.d(SERVICE, "CHAT/postTime: ${postTime.isNullOrEmpty()}")
                                Log.d(SERVICE, "CHAT/postTime: $postTime")

                                val chat = ChatList(
                                    chatIdx,
                                    nickname,
                                    profileImg,
                                    postTime,
                                    message,
                                    groupName
                                )
                                chatList.add(chat)
                                Log.d(SERVICE, "CHAT/chatList: $chatList")
                            }
                        }
                        getChatListView.onGetChatListSuccess(chatList)
                        Log.d(SERVICE, "CHAT/$chatList")
                    }
                    else -> getChatListView.onGetChatListFailure(resp.code, resp.message)
                }
            }

            // 네트워크 자체가 실패한 경우
            override fun onFailure(call: Call<ServerResponse>, t: Throwable) {
                Log.d(SERVICE, "CHAT/${t.message}")
                getChatListView.onGetChatListFailure(400, "네트워크 오류")
            }
        })
    }

    // 갠톡 or 단톡 채팅 가져오기
    fun getChat(getChatView: GetChatView, userIdx: Long, chatIdx: Int, groupName: String?) {
        val chatList = ArrayList<ChatList>()
        val chatService = retrofit.create(ChatRetrofitInterface::class.java)

        // 응답 처리
        chatService.getChat(userIdx, chatIdx, groupName).enqueue(object : Callback<ServerResponse> {
            override fun onResponse(
                call: Call<ServerResponse>,
                response: Response<ServerResponse>
            ) {
                val resp = response.body()!!

                when (resp.code) {
                    1000 -> {
                        // 응답 성공했을 때 response parameters parsing
                        val jsonArray = resp.result
                        if (jsonArray != null) {
                            // JsonArray Parsing
                            for (i in 0 until jsonArray.size()) {
                                val jsonElement = jsonArray.get(i)
                                val chatIdx = jsonElement.asJsonObject.get("chatIdx").asInt
                                val nickname = jsonElement.asJsonObject.get("nickname").asString
                                val profileImgUrl =
                                    if (jsonElement.asJsonObject.get("profileImgUrl").isJsonNull) null else jsonElement.asJsonObject.get(
                                        "profileImgUrl"
                                    ).asString
                                val message = jsonElement.asJsonObject.get("message").asString
                                val postTime = jsonElement.asJsonObject.get("post_time").asString
                                val groupName =
                                    if (jsonElement.asJsonObject.get("groupName").isJsonNull) null else jsonElement.asJsonObject.get(
                                        "groupName"
                                    ).asString

                                val chat = ChatList(
                                    chatIdx,
                                    nickname,
                                    profileImgUrl,
                                    postTime,
                                    message,
                                    groupName
                                )
                                chatList.add(chat)
                                Log.d(SERVICE, "CHAT/getChat/chat: $chat")
                                Log.d(SERVICE, "CHAT/getChat/chatList: $chatList")
                            }
                            getChatView.onGetChatSuccess(chatList)
                            Log.d(SERVICE, "CHAT/getChat/onResponse/success")
                        }
                    }
                    else -> getChatView.onGetChatFailure(resp.code, resp.message)
                }
            }

            override fun onFailure(call: Call<ServerResponse>, t: Throwable) {
                Log.d(SERVICE, "CHAT/${t.message}")
                getChatView.onGetChatFailure(400, "네트워크 오류")
            }
        })
    }

    // 폴더 안의 채팅 가져오기
    fun getFolderContent(
        getFolderContentView: GetFolderContentView,
        userIdx: Long,
        folderIdx: Int
    ) {
        val folderContentList = ArrayList<FolderContent>()
        val chatService = retrofit.create(ChatRetrofitInterface::class.java)

        chatService.getFolderContent(userIdx, folderIdx).enqueue(object : Callback<ServerResponse> {
            override fun onResponse(
                call: Call<ServerResponse>,
                response: Response<ServerResponse>
            ) {
                val resp = response.body()!!

                when (resp.code) {
                    1000 -> {
                        val jsonArray = resp.result
                        if (jsonArray != null) {
                            // JsonArray parsing
                            for (i in 0 until jsonArray.size()) {
                                val jsonElement = jsonArray.get(i)
                                val chatIdx = jsonElement.asJsonObject.get("chatIdx").asInt
                                val folderName = jsonElement.asJsonObject.get("folderName").asString
                                val nickname = jsonElement.asJsonObject.get("nickname").asString
                                val profileImg =
                                    if (jsonElement.asJsonObject.get("profileImgUrl").isJsonNull) null else jsonElement.asJsonObject.get(
                                        "profileImgUrl"
                                    ).asString
                                val message = jsonElement.asJsonObject.get("message").asString
                                val postTime = jsonElement.asJsonObject.get("post_time").asString

                                Log.d(SERVICE, "CHAT/postTime: ${postTime.isNullOrEmpty()}")
                                Log.d(SERVICE, "CHAT/postTime: $postTime")

                                val folderContent = FolderContent(
                                    chatIdx,
                                    folderName,
                                    nickname,
                                    profileImg,
                                    message,
                                    postTime
                                )
                                Log.d(SERVICE, "CHAT/folderContent: $folderContent")
                                folderContentList.add(folderContent)
                            }
                        }
                        getFolderContentView.onGetFolderContentSuccess(folderContentList)
                    }
                    else -> getFolderContentView.onGetFolderContentFailure(resp.code, resp.message)
                }
            }

            override fun onFailure(call: Call<ServerResponse>, t: Throwable) {
                Log.d(SERVICE, "CHAT/${t.message}")
                getFolderContentView.onGetFolderContentFailure(400, "네트워크 오류")
            }
        })
    }

    // 선택한 채팅 삭제하기
    fun deleteChat(chatView: ChatView, userIdx: Long, chatIdx: Int) {
        val chatService = retrofit.create(ChatRetrofitInterface::class.java)

        chatService.deleteChat(userIdx, chatIdx).enqueue(object : Callback<ServerResponse> {
            override fun onResponse(
                call: Call<ServerResponse>,
                response: Response<ServerResponse>
            ) {
                val resp = response.body()!!
                Log.d(SERVICE, "CHAT/deleteChat/onResponse")

                when (resp.code) {
                    1000 -> chatView.onChatSuccess()
                    else -> chatView.onChatFailure(resp.code, resp.message)
                }
            }

            override fun onFailure(call: Call<ServerResponse>, t: Throwable) {
                Log.d(SERVICE, "CHAT/${t.message}")
                chatView.onChatFailure(400, "네트워크 오류")
            }
        })
    }

    // 선택한 채팅목록의 모든 채팅 삭제하기
    fun deleteChatList(chatView: ChatView, userIdx: Long, chatIdx: Int, groupName: String?) {
        val chatService = retrofit.create(ChatRetrofitInterface::class.java)

        chatService.deleteChatList(userIdx, chatIdx, groupName)
            .enqueue(object : Callback<ServerResponse> {
                override fun onResponse(
                    call: Call<ServerResponse>,
                    response: Response<ServerResponse>
                ) {
                    val resp = response.body()!!
                    Log.d(SERVICE, "CHAT/deleteChatList/onResponse")

                    when (resp.code) {
                        1000 -> chatView.onChatSuccess()
                        else -> chatView.onChatFailure(resp.code, resp.message)
                    }
                }

                override fun onFailure(call: Call<ServerResponse>, t: Throwable) {
                    Log.d(SERVICE, "CHAT/${t.message}")
                    chatView.onChatFailure(400, "네트워크 오류")
                }
            })
    }

    // 폴더에 채팅 추가하기
    fun addChatToFolder(chatView: ChatView, userIdx: Long, chatIdx: Int, folder: FolderList) {
        val chatService = retrofit.create(ChatRetrofitInterface::class.java)

        chatService.addChatToFolder(userIdx, chatIdx, folder)
            .enqueue(object : Callback<ServerResponse> {
                override fun onResponse(
                    call: Call<ServerResponse>,
                    response: Response<ServerResponse>
                ) {
                    val resp = response.body()!!
                    Log.d(SERVICE, "CHAT/addChatToFolder/onResponse")

                    when (resp.code) {
                        1000 -> chatView.onChatSuccess()
                        else -> chatView.onChatFailure(resp.code, resp.message)
                    }
                }

                override fun onFailure(call: Call<ServerResponse>, t: Throwable) {
                    Log.d(SERVICE, "CHAT/${t.message}")
                    chatView.onChatFailure(400, "네트워크 오류")
                }
            })
    }

    // 폴더에 채팅목록의 채팅(갠톡/단톡) 모두 추가하기
    fun addChatListToFolder(
        chatView: ChatView,
        userIdx: Long,
        chatIdx: Int,
        groupName: String?,
        folder: FolderList
    ) {
        val chatService = retrofit.create(ChatRetrofitInterface::class.java)

        chatService.addChatListToFolder(userIdx, chatIdx, groupName, folder)
            .enqueue(object : Callback<ServerResponse> {
                override fun onResponse(
                    call: Call<ServerResponse>,
                    response: Response<ServerResponse>
                ) {
                    val resp = response.body()!!
                    Log.d(SERVICE, "CHAT/addChatListToFolder/onResponse")

                    when (resp.code) {
                        1000 -> chatView.onChatSuccess()
                        else -> chatView.onChatFailure(resp.code, resp.message)
                    }
                }

                override fun onFailure(call: Call<ServerResponse>, t: Throwable) {
                    Log.d(SERVICE, "CHAT/${t.message}")
                    chatView.onChatFailure(400, "네트워크 오류")
                }
            })
    }

    // 폴더에서 채팅 삭제하기
    fun deleteChatFromFolder(chatView: ChatView, userIdx: Long, chatIdx: Int, folderIdx: Int) {
        val chatService = retrofit.create(ChatRetrofitInterface::class.java)

        chatService.deleteChatFromFolder(userIdx, chatIdx, folderIdx)
            .enqueue(object : Callback<ServerResponse> {
                override fun onResponse(
                    call: Call<ServerResponse>,
                    response: Response<ServerResponse>
                ) {
                    val resp = response.body()!!
                    Log.d(SERVICE, "CHAT/deleteChatFromFolder/onResponse")

                    when (resp.code) {
                        1000 -> chatView.onChatSuccess()
                        else -> chatView.onChatFailure(resp.code, resp.message)
                    }
                }

                override fun onFailure(call: Call<ServerResponse>, t: Throwable) {
                    Log.d(SERVICE, "CHAT/${t.message}")
                    chatView.onChatFailure(400, "네트워크 오류")
                }
            })
    }

    // 채팅목록/유저 차단하기
    fun block(chatView: ChatView, userIdx: Long, chatName: String, groupName: String?) {
        val chatService = retrofit.create(ChatRetrofitInterface::class.java)

        chatService.block(userIdx, chatName, groupName).enqueue(object : Callback<ServerResponse> {
            override fun onResponse(
                call: Call<ServerResponse>,
                response: Response<ServerResponse>
            ) {
                val resp = response.body()!!
                Log.d(SERVICE, "CHAT/block/onResponse")

                when (resp.code) {
                    1000 -> chatView.onChatSuccess()
                    else -> chatView.onChatFailure(resp.code, resp.message)
                }
            }

            override fun onFailure(call: Call<ServerResponse>, t: Throwable) {
                Log.d(SERVICE, "CHAT/${t.message}")
                chatView.onChatFailure(400, "네트워크 오류")
            }
        })
    }

    // 채팅목록/유저 차단 해제하기
    fun unblock(chatView: ChatView, userIdx: Long, chatName: String, groupName: String?) {
        val chatService = retrofit.create(ChatRetrofitInterface::class.java)

        chatService.unblock(userIdx, chatName, groupName)
            .enqueue(object : Callback<ServerResponse> {
                override fun onResponse(
                    call: Call<ServerResponse>,
                    response: Response<ServerResponse>
                ) {
                    val resp = response.body()!!
                    Log.d(SERVICE, "CHAT/unblock/onResponse")

                    when (resp.code) {
                        1000 -> chatView.onChatSuccess()
                        else -> chatView.onChatFailure(resp.code, resp.message)
                    }
                }

                override fun onFailure(call: Call<ServerResponse>, t: Throwable) {
                    Log.d(SERVICE, "CHAT/${t.message}")
                    chatView.onChatFailure(400, "네트워크 오류")
                }
            })
    }

    // 차단된 톡방 목록 가져오기
    fun getBlockedChatList(getBlockedChatListView: GetBlockedChatListView, userIdx: Long) {
        val blockedChatList = ArrayList<BlockedChatList>()
        val chatService = retrofit.create(ChatRetrofitInterface::class.java)

        chatService.getBlockedChatList(userIdx).enqueue(object : Callback<ServerResponse> {
            override fun onResponse(
                call: Call<ServerResponse>,
                response: Response<ServerResponse>
            ) {
                val resp = response.body()!!
                Log.d(SERVICE, "CHAT/getBlockedChatList/onResponse")

                when (resp.code) {
                    1000 -> {
                        val jsonArray = resp.result
                        if (jsonArray != null) {
                            // JsonArray parsing
                            for (i in 0 until jsonArray.size()) {
                                val jsonElement = jsonArray.get(i)
                                val blockedName =
                                    jsonElement.asJsonObject.get("blocked_name").asString
                                val blockedProfileImg =
                                    if (jsonElement.asJsonObject.get("blocked_profileImg").isJsonNull) null else jsonElement.asJsonObject.get(
                                        "blocked_profileImg"
                                    ).asString
                                val groupName =
                                    if (jsonElement.asJsonObject.get("groupName").isJsonNull) null else jsonElement.asJsonObject.get(
                                        "groupName"
                                    ).asString
                                val status = jsonElement.asJsonObject.get("status").asString

                                Log.d(SERVICE, "CHAT/postTime: ${groupName.isNullOrEmpty()}")
                                Log.d(SERVICE, "CHAT/postTime: $groupName")

                                val blockedChat = BlockedChatList(
                                    blockedName,
                                    blockedProfileImg,
                                    groupName,
                                    status
                                )
                                Log.d(SERVICE, "CHAT/blockedChatList: $blockedChatList")
                                blockedChatList.add(blockedChat)
                            }
                        }
                        getBlockedChatListView.onGetBlockedChatListSuccess(blockedChatList)
                    }
                    else -> getBlockedChatListView.onGetBlockedChatListFailure(
                        resp.code,
                        resp.message
                    )
                }
            }

            override fun onFailure(call: Call<ServerResponse>, t: Throwable) {
                Log.d(SERVICE, "CHAT/${t.message}")
                getBlockedChatListView.onGetBlockedChatListFailure(400, "네트워크 오류")
            }
        })
    }
}
