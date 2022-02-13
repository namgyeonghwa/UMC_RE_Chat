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
    private val tag = "SERVICE/CHAT"
    // 채팅 추가하기
    fun addChat(addChatView: AddChatView, userIdx: Long, chat: Chat) {
        val chatService = retrofit.create(ChatRetrofitInterface::class.java)

        chatService.addChat(userIdx, chat).enqueue(object: Callback<ChatResponse> {
            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                val resp = response.body()!!
                Log.d(tag, "addChat()")

                when (resp.code) {
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

        chatService.getChatList(userIdx).enqueue(object: Callback<ChatListResponse> {
            // 응답이 왔을 때
            override fun onResponse(call: Call<ChatListResponse>, response: Response<ChatListResponse>) {
                val resp = response.body()!!

                when(resp.code) {
                    1000 -> {
                        val jsonArray = resp.result
                        if(jsonArray != null) {
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
            override fun onFailure(call: Call<ChatListResponse>, t: Throwable) {
                Log.d(tag, t.message.toString())
                getChatListView.onGetChatListFailure(400, "네트워크 오류")
            }
        })
    }

//    // 갠톡 or 단톡 채팅 가져오기
//    fun getChat(getChatView: GetChatView, userIdx: Long, chatIdx: Int, groupName: String?) {
//        val chatList = ArrayList<ChatList>()
//        val chatService = retrofit.create(ChatRetrofitInterface::class.java)
//
//        // 응답 처리
//        chatService.getChat(userIdx, chatIdx, groupName).enqueue(object: Callback<ChatResponse> {
//            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
//                val resp = response.body()!!
//
//                when(resp.code) {
//                    1000 -> {
//                        // 응답 성공했을 때 response parameters parsing
//                        val jsonArray = resp.result
//                        if(jsonArray != null) {
//                            // JsonArray Parsing
//                            for(i in 0 until jsonArray.size()) {
//                                val jsonElement = jsonArray.get(i)
//                                val chatIdx = jsonElement.asJsonObject.get("chatIdx").asInt
//                                val nickname = jsonElement.asJsonObject.get("nickname").asString
//                                val profileImgUrl = if(jsonElement.asJsonObject.get("profileImgUrl").isJsonNull) null else jsonElement.asJsonObject.get("profileImgUrl").asString
//                                val message = jsonElement.asJsonObject.get("message").asString
//                                val postTime = jsonElement.asJsonObject.get("post_time").asString
//                                val groupName = if(jsonElement.asJsonObject.get("groupName").isJsonNull) null else jsonElement.asJsonObject.get("groupName").asString
//
//                                val chat = ChatList(chatIdx, nickname, groupName, profileImgUrl, postTime, message, 1)
//                                chatList.add(chat)
//                                Log.d(tag, "getChat()/chat: $chat")
//                                Log.d(tag, "getChat()/chatList: $chatList")
//                            }
//                            getChatView.onGetChatSuccess(chatList)
//                            Log.d(tag, "getChat()/onResponse()/success")
//                        }
//                    }
//                    else -> getChatView.onGetChatFailure(resp.code, resp.message)
//                }
//            }
//
//            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
//                Log.d(tag, t.message.toString())
//                getChatView.onGetChatFailure(400, "네트워크 오류")
//            }
//        })
//    }
//
//    // 폴더 안의 채팅 가져오기
//    fun getFolderContent(getFolderContentView: GetFolderContentView, userIdx: Long, folderIdx: Int) {
//        val folderContentList = ArrayList<FolderContent>()
//        val chatService = retrofit.create(ChatRetrofitInterface::class.java)
//
//        chatService.getFolderContent(userIdx, folderIdx).enqueue(object: Callback<ChatResponse> {
//            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
//                val resp = response.body()!!
//
//                when(resp.code) {
//                    1000 -> {
//                        val jsonArray = resp.result
//                        if(jsonArray != null) {
//                            // JsonArray parsing
//                            for(i in 0 until jsonArray.size()) {
//                                val jsonElement = jsonArray.get(i)
//                                val folderName = jsonElement.asJsonObject.get("folderName").asString
//                                val nickname = jsonElement.asJsonObject.get("nickname").asString
//                                val profileImg = if(jsonElement.asJsonObject.get("profileImgUrl").isJsonNull) null else jsonElement.asJsonObject.get("profileImgUrl").asString
//                                val message = jsonElement.asJsonObject.get("message").asString
//                                val chatDate = if(jsonElement.asJsonObject.get("chat_date").isJsonNull) null else jsonElement.asJsonObject.get("chat_date").asString
//                                val postTime = jsonElement.asJsonObject.get("post_time").asString
//
//                                Log.d(tag, "postTime: ${postTime.isNullOrEmpty()}")
//                                Log.d(tag, "postTime: $postTime")
//
//                                val folderContent = FolderContent(folderName, nickname, profileImg, message, null, postTime)
//                                Log.d(tag, "folderContent: $folderContent")
//                                folderContentList.add(folderContent)
//                            }
//                        }
//                        getFolderContentView.onGetFolderContentSuccess(folderContentList)
//                    }
//                    else -> getFolderContentView.onGetFolderContentFailure(resp.code, resp.message)
//                }
//            }
//
//            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
//                Log.d(tag, t.message.toString())
//                getFolderContentView.onGetFolderContentFailure(400, "네트워크 오류")
//            }
//        })
//    }
//
//    // 선택한 채팅 삭제하기
//    fun deleteChat(deleteChatView: DeleteChatView, userIdx: Long, chatIdx: Int) {
//        val chatService = retrofit.create(ChatRetrofitInterface::class.java)
//
//        chatService.deleteChat(userIdx, chatIdx).enqueue(object: Callback<ChatResponse> {
//            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
//                val resp = response.body()!!
//                Log.d(tag, "deleteChat()/onResponse()")
//
//                when(resp.code) {
//                    1000 -> deleteChatView.onDeleteChatSuccess()
//                    else -> deleteChatView.onDeleteChatFailure(resp.code, resp.message)
//                }
//            }
//
//            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
//                Log.d(tag, t.message.toString())
//                deleteChatView.onDeleteChatFailure(400, "네트워크 오류")
//            }
//        })
//    }
//
//    // 선택한 채팅목록의 모든 채팅 삭제하기
//    fun deleteChatList(deleteChatListView: DeleteChatListView, userIdx: Long, chatIdx: Int, groupName: String?) {
//        val chatService = retrofit.create(ChatRetrofitInterface::class.java)
//
//        chatService.deleteChatList(userIdx, chatIdx, groupName).enqueue(object: Callback<ChatResponse> {
//            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
//                val resp = response.body()!!
//                Log.d(tag, "deleteChatList()/onResponse()")
//
//                when(resp.code) {
//                    1000 -> deleteChatListView.onDeleteChatListSuccess()
//                    else -> deleteChatListView.onDeleteChatListFailure(resp.code, resp.message)
//                }
//            }
//
//            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
//                Log.d(tag, t.message.toString())
//                deleteChatListView.onDeleteChatListFailure(400, "네트워크 오류")
//            }
//        })
//    }
//
//    // 폴더에 채팅 추가하기
//    fun addChatToFolder(addChatToFolderView: AddChatToFolderView, userIdx: Long, chatIdx: Int, folderIdx: Int) {
//        val chatService = retrofit.create(ChatRetrofitInterface::class.java)
//
//        chatService.addChatToFolder(userIdx, chatIdx, folderIdx).enqueue(object: Callback<ChatResponse> {
//            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
//                val resp = response.body()!!
//                Log.d(tag, "addChatToFolder()/onResponse()")
//
//                when(resp.code) {
//                    1000 -> addChatToFolderView.onAddChatToFolderSuccess()
//                    else -> addChatToFolderView.onAddChatToFolderFailure(resp.code, resp.message)
//                }
//            }
//
//            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
//                Log.d(tag, t.message.toString())
//                addChatToFolderView.onAddChatToFolderFailure(400, "네트워크 오류")
//            }
//        })
//    }
//
//    // 폴더에 채팅목록의 채팅(갠톡/단톡) 모두 추가하기
//    fun addChatListToFolder(addChatListToFolderView: addChatListToFolderView, userIdx: Long, chatIdx: Int, groupName: String?, folderIdx: Int) {
//        val chatService = retrofit.create(ChatRetrofitInterface::class.java)
//
//        chatService.addChatListToFolder(userIdx, chatIdx, groupName, folderIdx).enqueue(object: Callback<ChatResponse> {
//            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
//                val resp = response.body()!!
//                Log.d(tag, "addChatListToFolder()/onResponse()")
//
//                when(resp.code) {
//                    1000 -> addChatListToFolderView.onAddChatListToFolderSuccess()
//                    else -> addChatListToFolderView.onAddChatListToFolderFailure(resp.code, resp.message)
//                }
//            }
//
//            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
//                Log.d(tag, t.message.toString())
//                addChatListToFolderView.onAddChatListToFolderFailure(400, "네트워크 오류")
//            }
//        })
//    }
//
//    // 폴더에서 채팅 삭제하기
//    fun deleteChatFromFolder(deleteChatFromFolderView: deleteChatFromFolderView, userIdx: Long, chatIdx: Int, folderIdx: Int) {
//        val chatService = retrofit.create(ChatRetrofitInterface::class.java)
//
//        chatService.deleteChatFromFolder(userIdx, chatIdx, folderIdx).enqueue(object: Callback<ChatResponse> {
//            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
//                val resp = response.body()!!
//                Log.d(tag, "deleteChatFromFolder()/onResponse()")
//
//                when(resp.code) {
//                    1000 -> deleteChatFromFolderView.onDeleteChatFromFolderSuccess()
//                    else -> deleteChatFromFolderView.onDeleteChatFromFolderFailure(resp.code, resp.message)
//                }
//            }
//
//            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
//                Log.d(tag, t.message.toString())
//                deleteChatFromFolderView.onDeleteChatFromFolderFailure(400, "네트워크 오류")
//            }
//        })
//    }
//
//    // 채팅목록/유저 차단하기
//    fun block(blockView: blockView, userIdx: Long, chatName: String, groupName: String?) {
//        val chatService = retrofit.create(ChatRetrofitInterface::class.java)
//
//        chatService.block(userIdx, chatName, groupName).enqueue(object: Callback<ChatResponse> {
//            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
//                val resp = response.body()!!
//                Log.d(tag, "block()/onResponse()")
//
//                when(resp.code) {
//                    1000 -> blockView.onBlockSuccess()
//                    else -> blockView.onBlockFailure(resp.code, resp.message)
//                }
//            }
//
//            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
//                Log.d(tag, t.message.toString())
//                blockView.onBlockFailure(400, "네트워크 오류")
//            }
//        })
//    }
//
//    // 채팅목록/유저 차단 해제하기
//    fun unblock(unblockView: unblockView, userIdx: Long, chatName: String, groupName: String?) {
//        val chatService = retrofit.create(ChatRetrofitInterface::class.java)
//
//        chatService.unblock(userIdx, chatName, groupName).enqueue(object: Callback<ChatResponse> {
//            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
//                val resp = response.body()!!
//                Log.d(tag, "unblock()/onResponse()")
//
//                when(resp.code) {
//                    1000 -> unblockView.onUnblockSuccess()
//                    else -> unblockView.onUnblockFailure(resp.code, resp.message)
//                }
//            }
//
//            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
//                Log.d(tag, t.message.toString())
//                unblockView.onUnblockFailure(400, "네트워크 오류")
//            }
//        })
//    }
//
//    // 차단된 톡방 목록 가져오기
//    fun getBlockedChatList(getBlockedChatListView: getBlockedChatListView, userIdx: Long) {
//        val blockedChatList = ArrayList<BlockedChatList>()
//        val chatService = retrofit.create(ChatRetrofitInterface::class.java)
//
//        chatService.getBlockedChatList(userIdx).enqueue(object: Callback<ChatResponse> {
//            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
//                val resp = response.body()!!
//                Log.d(tag, "getBlockedChatList()/onResponse()")
//
//                when(resp.code) {
//                    1000 -> {
//                        val jsonArray = resp.result
//                        if(jsonArray != null) {
//                            // JsonArray parsing
//                            for(i in 0 until jsonArray.size()) {
//                                val jsonElement = jsonArray.get(i)
//                                val blockedName = jsonElement.asJsonObject.get("blocked_name").asString
//                                val blockedProfileImg = jsonElement.asJsonObject.get("blocked_profileImg").asString
//                                val groupName = if(jsonElement.asJsonObject.get("groupName").isJsonNull) null else jsonElement.asJsonObject.get("groupName").asString
//                                val status = jsonElement.asJsonObject.get("status").asString
//
//                                Log.d(tag, "postTime: ${groupName.isNullOrEmpty()}")
//                                Log.d(tag, "postTime: $groupName")
//
//                                val blockedChat = BlockedChatList(blockedName, blockedProfileImg, groupName, status)
//                                Log.d(tag, "blockedChatList: $blockedChatList")
//                                blockedChatList.add(blockedChat)
//                            }
//                        }
//                        getBlockedChatListView.onGetBlockedChatListSuccess(blockedChatList)
//                    }
//                    else -> getBlockedChatListView.onGetBlockedChatListFailure(resp.code, resp.message)
//                }
//            }
//
//            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
//                Log.d(tag, t.message.toString())
//                getBlockedChatListView.onGetBlockedChatListFailure(400, "네트워크 오류")
//            }
//        })
//    }
}