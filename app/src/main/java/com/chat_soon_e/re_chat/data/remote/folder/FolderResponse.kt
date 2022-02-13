package com.chat_soon_e.re_chat.data.remote.folder

import com.google.gson.JsonArray
import com.google.gson.annotations.SerializedName

data class FolderResponse(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: JsonArray?
)

// 전체 폴더목록 가져오기 (숨김폴더 제외)
data class FolderList(
    @SerializedName("folder_name") val folderName: String,
    @SerializedName("folderImg") val folderImg: String?
)

// 숨김 폴더목록 가져오기
data class HiddenFolderList(
    @SerializedName("folderName") val folderName: String,
    @SerializedName("folderImg") val folderImg: String?
)