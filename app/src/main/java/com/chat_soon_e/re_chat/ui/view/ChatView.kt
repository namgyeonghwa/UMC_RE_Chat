package com.chat_soon_e.re_chat.ui.view

import com.chat_soon_e.re_chat.data.entities.ChatList
import com.chat_soon_e.re_chat.data.remote.chat.Chat

// 채팅 추가하기
interface AddChatView {
    fun onAddChatSuccess()
    fun onAddChatFailure(code: Int, message: String)
}

// 전체 채팅목록 가져오기 (메인화면)
interface GetChatListView {
    fun onGetChatListSuccess(chatList: ArrayList<ChatList>)
    fun onGetChatListFailure(code: Int, message: String)
}

// 갠톡 or 단톡 채팅 가져오기
interface GetChatView {
    fun onGetChatSuccess(chat: ArrayList<Chat>)
    fun onGetChatFailure(code: Int, message: String)
}

// 폴더 안의 채팅 가져오기

// 선택한 채팅 삭제하기

// 선택한 채팅목록의 모든 채팅 바꾸기

// 폴더에 채팅 추가하기

// 폴더에 채팅목록의 채팅(갠톡/단톡) 모두 추가하기

// 폴더에서 채팅 삭제하기

// 채팅목록/유저 차단하기

// 채팅목록/유저 차단 해제하기

// 차단된 톡방 목록 가져오기