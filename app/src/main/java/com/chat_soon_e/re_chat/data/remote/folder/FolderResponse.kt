package com.chat_soon_e.re_chat.data.remote.folder

import com.google.gson.JsonArray
import com.google.gson.annotations.SerializedName

data class FolderResponse(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: JsonArray?
)