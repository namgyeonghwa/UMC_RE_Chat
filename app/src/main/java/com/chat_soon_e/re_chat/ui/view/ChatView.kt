package com.chat_soon_e.re_chat.ui.view

import com.chat_soon_e.re_chat.data.entities.ChatList
import com.chat_soon_e.re_chat.data.remote.chat.BlockedChatList
import com.chat_soon_e.re_chat.data.remote.chat.Chat
import com.chat_soon_e.re_chat.data.remote.chat.FolderContent

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
    fun onGetChatSuccess(chatList: ArrayList<ChatList>)
    fun onGetChatFailure(code: Int, message: String)
}

// 폴더 안의 채팅 가져오기
interface GetFolderContentView {
    fun onGetFolderContentSuccess(folderContentList: ArrayList<FolderContent>)
    fun onGetFolderContentFailure(code: Int, message: String)
}

// 선택한 채팅 삭제하기
interface DeleteChatView {
    fun onDeleteChatSuccess()
    fun onDeleteChatFailure(code: Int, message: String)
}

// 선택한 채팅목록의 모든 채팅 바꾸기
interface DeleteChatListView {
    fun onDeleteChatListSuccess()
    fun onDeleteChatListFailure(code: Int, message: String)
}

// 폴더에 채팅 추가하기
interface AddChatToFolderView {
    fun onAddChatToFolderSuccess()
    fun onAddChatToFolderFailure(code: Int, message: String)
}

// 폴더에 채팅목록의 채팅(갠톡/단톡) 모두 추가하기
interface addChatListToFolderView {
    fun onAddChatListToFolderSuccess()
    fun onAddChatListToFolderFailure(code: Int, message: String)
}

// 폴더에서 채팅 삭제하기
interface deleteChatFromFolderView {
    fun onDeleteChatFromFolderSuccess()
    fun onDeleteChatFromFolderFailure(code: Int, message: String)
}

// 채팅목록/유저 차단하기
interface blockView {
    fun onBlockSuccess()
    fun onBlockFailure(code: Int, message: String)
}

// 채팅목록/유저 차단 해제하기
interface unblockView {
    fun onUnblockSuccess()
    fun onUnblockFailure(code: Int, message: String)
}

// 차단된 톡방 목록 가져오기
interface getBlockedChatListView {
    fun onGetBlockedChatListSuccess(blockedChatList: ArrayList<BlockedChatList>)
    fun onGetBlockedChatListFailure(code: Int, message: String)
}