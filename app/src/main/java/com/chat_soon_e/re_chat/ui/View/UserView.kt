package com.chat_soon_e.re_chat.ui.view

interface UserView {
    fun onAddKakaoUserSuccess()
    fun onAddKakaoUserFailure(code: Int, message: String)
}