package com.chat_soon_e.re_chat.ui.view

import com.chat_soon_e.re_chat.data.entities.ChatList
import com.chat_soon_e.re_chat.data.remote.chat.BlockedChatList
import com.chat_soon_e.re_chat.data.remote.chat.FolderContent

// 채팅 추가하기
interface ChatView {
    fun onChatSuccess()
    fun onChatFailure(code: Int, message: String)
}

// 전체 채팅목록 가져오기 (메인화면)
interface GetChatListView {
    fun onGetChatListSuccess(chatList: ArrayList<ChatList>)
    fun onGetChatListFailure(code: Int, message: String)
}

// 갠톡 or 단톡 채팅 가져오기
interface GetChatView {
    fun onGetChatSuccess(chats: ArrayList<ChatList>)
    fun onGetChatFailure(code: Int, message: String)
}

// 폴더 안의 채팅 가져오기
interface GetFolderContentView {
    fun onGetFolderContentSuccess(folderContents: ArrayList<FolderContent>)
    fun onGetFolderContentFailure(code: Int, message: String)
}

// 차단된 톡방 목록 가져오기
interface GetBlockedChatListView {
    fun onGetBlockedChatListSuccess(blockedChatList: ArrayList<BlockedChatList>)
    fun onGetBlockedChatListFailure(code: Int, message: String)
}