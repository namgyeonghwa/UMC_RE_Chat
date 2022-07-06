package com.chatsoone.rechat.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ChatTypeViewModel : ViewModel() {
    private var _mode = MutableLiveData<Int>()
    val mode get() = _mode  // 0: 일반, 1: 선택

    init {
        _mode.value = 0
    }

    fun setMode(mode: Int) {
        _mode.value = mode
    }
}
