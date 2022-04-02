package com.chatsoone.rechat.ui

import android.util.SparseBooleanArray
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chatsoone.rechat.data.entity.ChatList

class ItemViewModel : ViewModel() {
    private var _selectedItemList = MutableLiveData<ArrayList<ChatList>>()
    val list get() = _selectedItemList

    init {
        _selectedItemList.value = ArrayList<ChatList>()
    }

    fun setSelectedItemList(selectedItemList: ArrayList<ChatList>) {
        _selectedItemList.value = selectedItemList
    }
}