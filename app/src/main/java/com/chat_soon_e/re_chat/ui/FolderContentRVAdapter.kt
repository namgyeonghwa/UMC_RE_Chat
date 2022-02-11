package com.chat_soon_e.re_chat.ui

import android.annotation.SuppressLint
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.chat_soon_e.re_chat.ApplicationClass.Companion.dateToString
import com.chat_soon_e.re_chat.ApplicationClass.Companion.loadBitmap
import com.chat_soon_e.re_chat.R
import com.chat_soon_e.re_chat.data.entities.Chat
import com.chat_soon_e.re_chat.data.entities.ChatList
import com.chat_soon_e.re_chat.data.entities.Folder
import com.chat_soon_e.re_chat.data.local.AppDatabase
import com.chat_soon_e.re_chat.databinding.ItemChatBinding

class FolderContentRVAdapter(private val mContext: FolderContentActivity, private val mItemClickListener: MyClickListener)
    : RecyclerView.Adapter<FolderContentRVAdapter.ViewHolder>() {
    var chatList = ArrayList<ChatList>()
    private lateinit var popup: PopupMenu

    // 클릭 인터페이스
    interface MyClickListener {
        fun onRemoveChat(chatIdx:Int)
        fun onChatLongClick(popupMenu: PopupMenu)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): FolderContentRVAdapter.ViewHolder {
        val binding: ItemChatBinding = ItemChatBinding.inflate(
            LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: FolderContentRVAdapter.ViewHolder, position: Int) {
        holder.bind(chatList[position])

    }

    override fun getItemCount(): Int = chatList.size

    private fun removeChat(position: Int) {
        chatList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount);
    }

    //AddData
    @SuppressLint("NotifyDataSetChanged")
    fun addItem(chat: List<ChatList>){
        chatList.clear()
        chatList.addAll(chat as ArrayList)

        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemChatBinding): RecyclerView.ViewHolder(binding.root) {
        init {
            binding.itemChatDefaultMessageTv.setOnLongClickListener {
                popup = PopupMenu(mContext, binding.itemChatDefaultMessageTv, Gravity.START, 0, R.style.MyFolderOptionPopupMenuTheme)
                popup.menuInflater.inflate(R.menu.popup_chat_option_menu, popup.menu)
                popup.setOnMenuItemClickListener { item ->
                    when(item?.itemId) {
                        R.id.popup_chat_option_menu_delete -> {
                            mItemClickListener.onRemoveChat(chatList[bindingAdapterPosition].chatIdx)
                            removeChat(bindingAdapterPosition)
                        }
                    }
                    false
                }
                mItemClickListener.onChatLongClick(popup)
                return@setOnLongClickListener false
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(chat: ChatList) {
            binding.itemChatDefaultNameTv.text = chat.nickName
            binding.itemChatDefaultMessageTv.text = chat.message
            //binding.itemChatDefaultDateTimeTv.text = dateToString(chat.postTime!!) 선우님
            if(chat.profileImg==null||chat.profileImg=="null")
                binding.itemChatDefaultProfileIv.setImageResource(R.drawable.ic_profile_black_no_circle)
            else
                binding.itemChatDefaultProfileIv.setImageBitmap(loadBitmap(chat.profileImg!!, mContext))
        }
    }
}