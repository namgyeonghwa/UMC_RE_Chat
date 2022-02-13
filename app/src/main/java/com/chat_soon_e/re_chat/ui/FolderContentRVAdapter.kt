package com.chat_soon_e.re_chat.ui

import android.annotation.SuppressLint
import android.graphics.Point
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class FolderContentRVAdapter(private val mContext: FolderContentActivity, private val size: Point, private val mItemClickListener: MyClickListener)
    : RecyclerView.Adapter<FolderContentRVAdapter.ViewHolder>() {
    var chatList = ArrayList<ChatList>()
    private lateinit var popupMenu: PopupMenu
    private val tag = "RV/FOLDER_CONTENT"

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
                popupMenu = PopupMenu(mContext, binding.itemChatDefaultMessageTv, Gravity.START, 0, R.style.MyFolderOptionPopupMenuTheme)
                popupMenu.menuInflater.inflate(R.menu.popup_chat_option_menu, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener { item ->
                    when(item?.itemId) {
                        R.id.popup_chat_option_menu_delete -> {
                            mItemClickListener.onRemoveChat(chatList[bindingAdapterPosition].chatIdx)
                            removeChat(bindingAdapterPosition)
                        }
                    }
                    false
                }
                mItemClickListener.onChatLongClick(popupMenu)
                return@setOnLongClickListener false
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(chat: ChatList) {
            Log.d(tag, "chat.nickName: ${chat.nickName}")
            Log.d(tag, "chat.message: ${chat.message}")
            Log.d(tag, "chat.postTime: ${chat.postTime}")

            binding.itemChatDefaultMessageTv.maxWidth = (size.x * 0.6f).toInt()
            binding.itemChatDefaultMessageTv.minHeight = (size.y * 0.05f).toInt()

            binding.itemChatDefaultNameTv.text = chat.nickName
            binding.itemChatDefaultMessageTv.text = chat.message
            if(chat.profileImg==null||chat.profileImg=="null") binding.itemChatDefaultProfileIv.setImageResource(R.drawable.chat_defualt_profile)
            else binding.itemChatDefaultProfileIv.setImageBitmap(loadBitmap(chat.profileImg!!, mContext))
            binding.itemChatDefaultDateTimeTv.text = convertDate(binding, chat.postTime)
        }
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun convertDate(binding: ItemChatBinding, date :String): String {
        val str: String
        val today = Date()

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val dateAsDate = simpleDateFormat.parse(date)!!
//        val simpleDateFormat2 = SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN)
//        val dateAsString = simpleDateFormat2.format(dateAsDate!!)

        // 오늘이 아니라면 날짜만
        if(dateAsDate.year == today.year && dateAsDate.month == today.month && dateAsDate.date==today.date){
            val time = SimpleDateFormat("a h:mm")
            str = time.format(dateAsDate).toString()
        } else{
            // simpleDateFormat은 thread에 안전하지 않습니다.
            // DateTimeFormatter을 사용합시다. 아! Date를 LocalDate로도 바꿔야합니다!
            // val time_formatter=DateTimeFormatter.ofPattern("MM월 dd일")
            // date.format(time_formatter)
            val time = SimpleDateFormat("yyyy년 M월 d일")
            str = time.format(dateAsDate).toString()

            binding.itemChatDefaultNewDateTimeLayout.visibility = View.VISIBLE
            binding.itemChatDefaultNewDateTimeTv.text = str
        }
        return str
    }
}