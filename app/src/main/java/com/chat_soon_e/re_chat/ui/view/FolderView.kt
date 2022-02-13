package com.chat_soon_e.re_chat.ui.view

import com.chat_soon_e.re_chat.data.remote.folder.FolderList
import com.chat_soon_e.re_chat.data.remote.folder.HiddenFolderList

// 전체 폴더목록 가져오기 (숨김폴더 제외)
interface FolderListView {
    fun onFolderListSuccess(folderList: ArrayList<FolderList>)
    fun onFolderListFailure(code: Int, message: String)
}

// 폴더 생성하기
interface CreateFolderView {
    fun onCreateFolderSuccess()
    fun onCreateFolderFailure(code: Int, message: String)
}

// 폴더 이름 바꾸기
interface ChangeFolderNameView {
    fun onChangeFolderNameSuccess()
    fun onChangeFolderNameFailure(code: Int, message: String)
}

// 폴더 아이콘 바꾸기
interface ChangeFolderIconView {
    fun onChangeFolderIconSuccess()
    fun onChangeFolderIconFailure(code: Int, message: String)
}

// 폴더 삭제하기
interface DeleteFolderView {
    fun onDeleteFolderSuccess()
    fun onDeleteFolderFailure(code: Int, message: String)
}

// 숨김 폴더목록 가져오기
interface HiddenFolderListView {
    fun onHiddenFolderListSuccess(hiddenFolderList: ArrayList<HiddenFolderList>)
    fun onHiddenFolderListFailure(code: Int, message: String)
}

// 폴더 숨기기
interface HideFolderView {
    fun onHideFolderSuccess()
    fun onHideFolderFailure(code: Int, message: String)
}

// 숨김 폴더 다시 해제하기
interface UnhideFolderView {
    fun onUnhideFolderSuccess()
    fun onUnhideFolderFailure(code: Int, message: String)
}