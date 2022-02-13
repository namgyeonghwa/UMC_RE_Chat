package com.chat_soon_e.re_chat.ui.view

import com.chat_soon_e.re_chat.data.entities.ChatList

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