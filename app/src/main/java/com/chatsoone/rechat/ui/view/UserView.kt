package com.chatsoone.rechat.ui.view

interface UserView {
    fun onAddKakaoUserSuccess()
    fun onAddKakaoUserFailure(code: Int, message: String)
}
