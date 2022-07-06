package com.chatsoone.rechat.ui.main.home

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.chatsoone.rechat.ApplicationClass.Companion.FRAG
import com.chatsoone.rechat.ApplicationClass.Companion.showToast
import com.chatsoone.rechat.R
import com.chatsoone.rechat.base.BaseFragment
import com.chatsoone.rechat.data.entity.Icon
import com.chatsoone.rechat.data.local.AppDatabase
import com.chatsoone.rechat.data.remote.ChatList
import com.chatsoone.rechat.data.remote.FolderList
import com.chatsoone.rechat.data.remote.chat.ChatService
import com.chatsoone.rechat.databinding.*
import com.chatsoone.rechat.ui.adapter.FolderListRVAdapter
import com.chatsoone.rechat.ui.viewmodel.ItemViewModel
import com.chatsoone.rechat.ui.chat.ChatActivity
import com.chatsoone.rechat.ui.main.MainActivity
import com.chatsoone.rechat.ui.view.ChatView
import com.chatsoone.rechat.ui.view.GetChatListView
import com.chatsoone.rechat.ui.viewmodel.ChatTypeViewModel
import com.chatsoone.rechat.util.getID

class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate),
    GetChatListView, ChatView, LifecycleObserver {
    private lateinit var database: AppDatabase

    // RecyclerView adapter
    private lateinit var homeRVAdapter: HomeRVAdapter

    // Service
    private lateinit var chatService: ChatService

    private val userID = getID()
    private var chatList = ArrayList<ChatList>()
    private val chatViewModel by activityViewModels<ChatTypeViewModel>()
    private val selectedItemViewModel by activityViewModels<ItemViewModel>()

    override fun initAfterBinding() {
        database = AppDatabase.getInstance(requireContext())!!
        chatService = ChatService()
        homeRVAdapter = HomeRVAdapter(requireContext())

        initClickListener()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatViewModel.setMode(mode = 0)

        // observe mode
        chatViewModel.mode.observe(viewLifecycleOwner, Observer {
            // 모든 데이터의 뷰 타입 변경
            homeRVAdapter.setAllViewType(it)

            if (it == 0) {
                setDefaultMode()
            } else setChooseMode()

            Log.d(FRAG, "HOME/mode: $it")
        })
    }

    override fun onResume() {
        super.onResume()
        initChatList()
    }

    private fun initChatList() {
        chatService.getChatList(this, userID)
    }

    // recycler view 초기화
    private fun initRecyclerView() {
        homeRVAdapter.addItem(this.chatList)
        binding.homeRecyclerView.adapter = homeRVAdapter

        homeRVAdapter.setMyItemClickListener(object : HomeRVAdapter.MyItemClickListener {
            override fun onDefaultChatClick(view: View, position: Int, chat: ChatList) {
//                checkNewChat(position)

                val spf =
                    requireContext().getSharedPreferences("all_chat", Context.MODE_PRIVATE)
                val editor = spf.edit()
                editor.putInt("all_chat", 1)
                editor.apply()

                // ChatActivity로 데이터 전달
                val intent = Intent(activity as MainActivity, ChatActivity::class.java)
                intent.putExtra("chatListJson", chat)
                startActivity(intent)
            }

            override fun onChooseChatClick(itemBinding: ItemChatListChooseBinding, position: Int) {
                selectedItemViewModel.setSelectedItemList(homeRVAdapter.getSelectedItem())
                if (homeRVAdapter.getSelectedItem().size == 0) chatViewModel.setMode(0)
            }

            override fun onProfileClick(itemBinding: ItemChatListDefaultBinding, position: Int) {
                chatViewModel.setMode(1)
                selectedItemViewModel.setSelectedItemList(homeRVAdapter.getSelectedItem())
            }

        })

        val linearLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)
        linearLayoutManager.stackFromEnd = true
        binding.homeRecyclerView.layoutManager = linearLayoutManager
    }

    // 새로 온 채팅을 확인했을 때
    private fun checkNewChat(position: Int) {
        // API 추가하는 건 어떨지
    }

    // 기본 모드 세팅
    private fun setDefaultMode() {
        homeRVAdapter.clearSelectedItemList()
        selectedItemViewModel.setSelectedItemList(homeRVAdapter.getSelectedItem())
        binding.homeTitleTv.visibility = View.VISIBLE
        binding.homeSettingIv.visibility = View.GONE
        binding.homeLayout.setBackgroundColor(Color.parseColor("#B9E3FB"))
    }

    // 선택 모드 세팅
    private fun setChooseMode() {
        selectedItemViewModel.setSelectedItemList(homeRVAdapter.getSelectedItem())
        binding.homeTitleTv.visibility = View.VISIBLE // 디자인에 따라 변경
        binding.homeSettingIv.visibility = View.VISIBLE
        binding.homeLayout.setBackgroundColor(Color.parseColor("#B9E3FB"))
    }

    // click listener 초기화
    private fun initClickListener() {
        Log.d(FRAG, "HOME/initClickListener")

        // 선택 모드 취소 버튼 클릭했을 때 기본 모드로 세팅
        binding.homeSettingIv.setOnClickListener {
            showDialog()
        }
    }

    // Bottom dialog 보여주기
    private fun showDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.fragment_bottom_dialog)

        // 삭제 누를시
        dialog.findViewById<TextView>(R.id.bottom_dialog_delete_tv).setOnClickListener {
            homeRVAdapter.removeSelectedItemList()
            chatViewModel.setMode(0)    // 혹은 바로 setDefaultMode() 가능
            Toast.makeText(requireContext(), "삭제되었습니다.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        // 차단 누를시
        dialog.findViewById<TextView>(R.id.bottom_dialog_block_tv).setOnClickListener {
            val selectedChatList = homeRVAdapter.getSelectedItem()

            for (i in selectedChatList) {
                if (i.groupName != "null") {
                    // 그룹
                    i.groupName?.let { it1 -> chatService.block(this, userID, it1, it1) }
                } else {
                    // 개인
                    chatService.block(this, userID, i.chatName, null)
                }
            }

            homeRVAdapter.blockSelectedItemList()
            chatViewModel.setMode(0)    // 혹은 바로 setDefaultMode() 가능
            Toast.makeText(requireContext(), "차단되었습니다.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        // 취소 누를시
        dialog.findViewById<TextView>(R.id.bottom_dialog_cancel_tv).setOnClickListener {
            chatViewModel.setMode(0)    // 혹은 바로 setDefaultMode() 가능
            dialog.dismiss()
        }
        dialog.show()
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)

    }

    override fun onChatSuccess() {
        Log.d(FRAG, "HOME/onChatSuccess")
        initChatList()
    }

    override fun onChatFailure(code: Int, message: String) {
        Log.d(FRAG, "HOME/onGetChatListFailure/code: $code, message: $message")
    }

    override fun onGetChatListSuccess(chatList: ArrayList<ChatList>) {
        Log.d(FRAG, "HOME/onGetChatListSuccess/chatList: $chatList")
        this.chatList.clear()
        this.chatList = chatList
        initRecyclerView()
    }

    override fun onGetChatListFailure(code: Int, message: String) {
        Log.d(FRAG, "HOME/onGetChatListFailure/code: $code, message: $message")
        initRecyclerView()

        if (code == 400) showToast(requireContext(), "네트워크 오류");
    }
}