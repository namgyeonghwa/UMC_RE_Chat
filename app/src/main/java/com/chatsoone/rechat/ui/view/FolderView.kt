package com.chatsoone.rechat.ui.view

import com.chatsoone.rechat.data.remote.FolderList

// 공통 View
// 폴더 이름 바꾸기, 폴더 아이콘 바꾸기, 폴더 삭제하기, 폴더 숨기기, 숨김 폴더 다시 해제하기
interface FolderAPIView {
    fun onFolderAPISuccess()
    fun onFolderAPIFailure(code: Int, message: String)
}

interface CreateFolderView {
    fun onCreateFolderSuccess(name: String, icon: String)
    fun onCreateFolderFailure(code: Int, message: String)
}

// 전체 폴더목록 가져오기 (숨김폴더 제외)
interface FolderListView {
    fun onFolderListSuccess(folderList: ArrayList<FolderList>)
    fun onFolderListFailure(code: Int, message: String)
}

// 숨김 폴더목록 가져오기
interface HiddenFolderListView {
    fun onHiddenFolderListSuccess(hiddenFolderList: ArrayList<FolderList>)
    fun onHiddenFolderListFailure(code: Int, message: String)
}
