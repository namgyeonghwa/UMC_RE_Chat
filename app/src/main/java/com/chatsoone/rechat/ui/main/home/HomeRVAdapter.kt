package com.chatsoone.rechat.ui.main.home

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.chatsoone.rechat.ApplicationClass.Companion.RV
import com.chatsoone.rechat.ApplicationClass.Companion.TAG
import com.chatsoone.rechat.ApplicationClass.Companion.convertDate
import com.chatsoone.rechat.ApplicationClass.Companion.loadBitmap
import com.chatsoone.rechat.R
import com.chatsoone.rechat.data.local.AppDatabase
import com.chatsoone.rechat.data.remote.ChatList
import com.chatsoone.rechat.data.remote.ChatListViewType
import com.chatsoone.rechat.data.remote.chat.ChatService
import com.chatsoone.rechat.databinding.ItemChatListChooseBinding
import com.chatsoone.rechat.databinding.ItemChatListDefaultBinding
import com.chatsoone.rechat.ui.main.folder.MyFolderRVAdapter
import com.chatsoone.rechat.ui.view.ChatView
import com.chatsoone.rechat.util.getID
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class HomeRVAdapter(
    private val context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), ChatView {
    private val userID = getID()
    private var chatList = ArrayList<ChatList>()
    private var selectedItemList: SparseBooleanArray = SparseBooleanArray(0)
    private val chatService = ChatService()

    // 클릭 인터페이스
    interface MyItemClickListener {
        // 기본 모드에서 채팅을 클릭했을 때
        fun onDefaultChatClick(view: View, position: Int, chat: ChatList)

        // 선택 모드에서 채팅을 클릭했을 때
        fun onChooseChatClick(itemBinding: ItemChatListChooseBinding, position: Int)

        // 프로필 사진 눌렀을 때 선택 모드로 전환되게끔
        fun onProfileClick(itemBinding: ItemChatListDefaultBinding, position: Int)
    }

    // 리스너 객체를 저장하는 변수
    private lateinit var mItemClickListener: MyItemClickListener

    // 리스너 객체를 외부에서 전달받는 함수
    fun setMyItemClickListener(itemClickListener: MyItemClickListener) {
        mItemClickListener = itemClickListener
    }

    // 기본 모드 뷰홀더
    inner class DefaultViewHolder(private val binding: ItemChatListDefaultBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.itemChatListDefaultLayout.setOnClickListener {
                mItemClickListener.onDefaultChatClick(
                    itemView,
                    position = bindingAdapterPosition,
                    chatList[bindingAdapterPosition]
                )
            }

            // 프로필이 선택된 경우
            binding.itemChatListProfileIv.setOnClickListener {
                updateSelectedItemList(position = bindingAdapterPosition)
                mItemClickListener.onProfileClick(
                    binding,
                    position = bindingAdapterPosition
                )
            }
        }

        fun bind(chat: ChatList) {
            if (!isItemSelected(bindingAdapterPosition)) {
                // 선택된 아이템이 아닌 경우
                Log.d(RV, "HOME/default/not selected: $bindingAdapterPosition")
                if (chat.profileImg != null && chat.profileImg!!.isNotEmpty() && chat.groupName != null)
                    binding.itemChatListProfileIv.setImageBitmap(
                        loadBitmap(
                            chat.profileImg!!,
                            context
                        )
                    )
                else if (chat.groupName != null || chat.groupName != "null")
                    binding.itemChatListProfileIv.setImageResource(R.drawable.ic_profile_default)
            } else {
                // 선택된 아이템인 경우
                Log.d(RV, "HOME/default/selected: $bindingAdapterPosition")
                binding.itemChatListProfileIv.setImageResource(R.drawable.ic_check_circle)
            }

            binding.itemChatListNameTv.text = chat.chatName
            binding.itemChatListContentTv.text = chat.latestMessage
            binding.itemChatListDateTimeTv.text = chat.latestTime?.let { convertDate(it) }

//            // 새로 온 채팅인 경우 NEW 표시
//            if (chat.isNew) binding.itemChatListNewCv.visibility = View.VISIBLE
//            else binding.itemChatListNewCv.visibility = View.INVISIBLE
        }
    }

    // 선택 모드 뷰홀더
    inner class ChooseViewHolder(
        private val binding: ItemChatListChooseBinding,
        private val mItemClickListener: MyItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            // 선택 모드에서 클릭했을 때
            binding.itemChatListChooseLayout.setOnClickListener {
                updateSelectedItemList(position = bindingAdapterPosition)
                mItemClickListener.onChooseChatClick(
                    binding,
                    position = bindingAdapterPosition
                )
            }
        }

        fun bind(chat: ChatList) {
            if (!isItemSelected(bindingAdapterPosition)) {
                // 선택되지 않은 아이템인 경우
                Log.d(RV, "HOME/choose/not selected: $bindingAdapterPosition")
                if (chat.profileImg != null && chat.profileImg!!.isNotEmpty() && chat.groupName != null) binding.itemChatListProfileIv.setImageBitmap(
                    loadBitmap(chat.profileImg!!, context)
                ) else if (chat.groupName != null || chat.groupName != "null") binding.itemChatListProfileIv.setImageResource(
                    R.drawable.ic_profile_default
                )
            } else {
                // 선택된 아이템인 경우
                Log.d(RV, "HOME/choose/selected: $bindingAdapterPosition")
                binding.itemChatListProfileIv.setImageResource(R.drawable.ic_check_circle)
            }

            binding.itemChatListNameTv.text = chat.chatName
            binding.itemChatListContentTv.text = chat.latestMessage
            binding.itemChatListDateTimeTv.text = chat.latestTime?.let { convertDate(it) }
        }
    }

    // 뷰홀더를 생성해줘야 할 때 호출
    // 아이템 뷰 객체를 만들어서 뷰 홀더에 전달
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ChatListViewType.DEFAULT -> {
                // 기본 모드
                DefaultViewHolder(
                    ItemChatListDefaultBinding.inflate(
                        LayoutInflater.from(viewGroup.context), viewGroup, false
                    )
                )
            }
            else -> {
                // 선택 모드
                ChooseViewHolder(
                    ItemChatListChooseBinding.inflate(
                        LayoutInflater.from(viewGroup.context), viewGroup, false
                    ),
                    mItemClickListener = mItemClickListener
                )
            }
        }
    }

    // 뷰홀더에 데이터 바인딩 할 때마다 호출
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (chatList[position].viewType) {
            ChatListViewType.DEFAULT -> {
                // 기본 모드
                (holder as DefaultViewHolder).bind(chatList[position])
                (holder).itemView.isSelected = isItemSelected(position)
            }
            else -> {
                // 선택 모드
                (holder as ChooseViewHolder).bind(chatList[position])
                (holder).itemView.isSelected = isItemSelected(position)
            }
        }
    }

    // 선택된 아이템 삭제
    @SuppressLint("NotifyDataSetChanged")
    fun removeSelectedItemList() {
        // 체크 안 된 것들로 교체한 후 HomeFragment에는 선택 안 된 것들만 남도록 한다.
        val checkedItemList = chatList.filter { chatList -> chatList.isChecked }

        for (i in checkedItemList) {
            chatService.deleteChatList(this, userID, i.chatIdx, i.groupName)
        }
        notifyDataSetChanged()
    }

    // 선택된 아이템 차단
    @SuppressLint("NotifyDataSetChanged")
    fun blockSelectedItemList() {
        // 체크 안 된 것들로 교체한 후 HomeFragment에는 선택 안 된 것들만 남도록 한다.
        val checkedItemList = chatList.filter { chatlist -> chatlist.isChecked }

        for (i in checkedItemList) {
            chatService.block(this, userID, i.chatName, i.groupName)
        }
        notifyDataSetChanged()
    }

    // 선택된 아이템 초기화
    @SuppressLint("NotifyDataSetChanged")
    fun clearSelectedItemList() {
        selectedItemList.clear()
        notifyDataSetChanged()
    }

    // will toggle the selection of items
    private fun updateSelectedItemList(position: Int) {
        // 선택된 아이템들로 리스트 갱신
        if (selectedItemList.get(position, false)) {
            selectedItemList.delete(position)
        } else {
            selectedItemList.put(position, true)
        }

        // 채팅 리스트 업데이트
        chatList[position].isChecked = !chatList[position].isChecked

        // 선택된 itmelist들의 로그
        notifyItemChanged(position)
    }

    // 뷰타입 설정
    @SuppressLint("NotifyDataSetChanged")
    fun setAllViewType(currentMode: Int) {
        val newChatList = ArrayList<ChatList>()

        for (i in 0 until chatList.size) {
            if (currentMode == 0) {
                // 일반 모드 (= 이동 모드)
                chatList[i].viewType = ChatListViewType.DEFAULT
            } else {
                // 선택 모드
                chatList[i].viewType = ChatListViewType.CHOOSE
            }
            newChatList.add(chatList[i])
        }
        this.chatList = newChatList
        notifyDataSetChanged()
    }

    // 아이템 뷰가 선택되었는지를 알려주는 함수
    private fun isItemSelected(position: Int): Boolean {
        return selectedItemList.get(position, false)
    }

    // 데이터셋의 크기를 알려주는 함수
    override fun getItemCount(): Int = this.chatList.size

    // 직접 설정한 뷰타입으로 설정되게 만든다.
    override fun getItemViewType(position: Int): Int = chatList[position].viewType!!

    fun setViewType(position: Int, mode: Int) {
        if (mode == 0) chatList[position].viewType = ChatListViewType.DEFAULT
        else chatList[position].viewType = ChatListViewType.CHOOSE

        notifyItemChanged(position)
    }

    // Add Data
    @SuppressLint("NotifyDataSetChanged")
    fun addItem(chats: List<ChatList>) {
        chatList.clear()
        chatList.addAll(chats as ArrayList)
        notifyDataSetChanged()
    }

    // 선택된 chatIdx를 가져온다.
    fun getSelectedItem(): ArrayList<ChatList> {
        // chatlist에서 checked 된 list들의 chatIdx를 저장하고 가져온다
        val selectedList = chatList.filter { chatlist -> chatlist.isChecked }
        return selectedList as ArrayList<ChatList>
    }

    override fun onChatSuccess() {
        Log.d(RV, "HOME/onChatSuccess")
    }

    override fun onChatFailure(code: Int, message: String) {
        Log.d(RV, "HOME/onChatFailure/code $code, message: $message")
    }
}
