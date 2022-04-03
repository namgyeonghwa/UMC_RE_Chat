package com.chatsoone.rechat.ui.main.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.SparseBooleanArray
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chatsoone.rechat.ApplicationClass
import com.chatsoone.rechat.ApplicationClass.Companion.FRAG
import com.chatsoone.rechat.ApplicationClass.Companion.currentWindowMetricsPointCompat
import com.chatsoone.rechat.R
import com.chatsoone.rechat.data.entity.ChatList
import com.chatsoone.rechat.data.entity.Folder
import com.chatsoone.rechat.data.entity.Icon
import com.chatsoone.rechat.data.local.AppDatabase
import com.chatsoone.rechat.databinding.*
import com.chatsoone.rechat.ui.ChatViewModel
import com.chatsoone.rechat.ui.FolderListRVAdapter
import com.chatsoone.rechat.ui.ItemViewModel
import com.chatsoone.rechat.ui.chat.ChatActivity
import com.chatsoone.rechat.ui.main.MainActivity
import com.chatsoone.rechat.ui.pattern.CreatePatternActivity
import com.chatsoone.rechat.ui.pattern.InputPatternActivity
import com.chatsoone.rechat.util.getID

class HomeFragment : Fragment(), LifecycleObserver {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var database: AppDatabase
    private lateinit var homeRVAdapter: HomeRVAdapter
    private lateinit var folderListRVAdapter: FolderListRVAdapter
    private lateinit var mPopupWindow: PopupWindow

    private val userID = getID()
    private var iconList = ArrayList<Icon>()
    private var folderList = ArrayList<Folder>()
    private var chatList = ArrayList<ChatList>()
    private val chatViewModel by activityViewModels<ChatViewModel>()
    private val selectedItemViewModel by activityViewModels<ItemViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        database = AppDatabase.getInstance(requireContext())!!
        folderListRVAdapter = FolderListRVAdapter(requireContext())
        initRecyclerView()
        initClickListener()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatViewModel.setMode(mode = 0)

        // observe mode
        chatViewModel.mode.observe(viewLifecycleOwner, Observer {
            // 모든 데이터의 뷰 타입 변경
            homeRVAdapter.setAllViewType(it)

            if (it == 0) setDefaultMode()
            else setChooseMode()

            Log.d(FRAG, "HOME/mode: $it")
        })

        // observe chat
        database.chatDao().getRecentChat(userID).observe(viewLifecycleOwner, Observer {
            Log.d(ApplicationClass.FRAG, "HOME/getRecentChat: $it")
            homeRVAdapter.addItem(it)
            chatList.clear()
            chatList.addAll(it)
            binding.homeRecyclerView.scrollToPosition(homeRVAdapter.itemCount - 1)
        })

        // live data 반영 (폴더/보관함 목록)
        database.folderDao().getFolderList(userID).observe(viewLifecycleOwner) {
            folderList.addAll(it)
            folderListRVAdapter.addFolderList(folderList)
        }
    }

    // recycler view 초기화
    private fun initRecyclerView() {
        val linearLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)
        linearLayoutManager.stackFromEnd = true
        binding.homeRecyclerView.layoutManager = linearLayoutManager

        homeRVAdapter = HomeRVAdapter(
            this.activity as MainActivity,
            object : HomeRVAdapter.MyItemClickListener {
                // 기본 모드 (클릭 시 ChatActivity로 이동)
                override fun onDefaultChatClick(view: View, position: Int, chat: ChatList) {
                    checkNewChat(position)

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

                // 선택 모드로 전환되게끔 (default에서 프로필 클릭 시 선택 모드로 전환)
                override fun onProfileClick(
                    itemBinding: ItemChatListDefaultBinding,
                    position: Int
                ) {
                    chatViewModel.setMode(1)
//                    itemBinding.itemChatListProfileIv.setImageResource(R.drawable.ic_check_circle)
//                    homeRVAdapter.setChecked(position)
//                    homeRVAdapter.setDefaultChecked(itemBinding, position)
                    selectedItemViewModel.setSelectedItemList(homeRVAdapter.getSelectedItem())
                }

                // 선택 모드 (클릭 시 프로필 변경 & 선택한 뷰 리스트에 넣어주기)
                override fun onChooseChatClick(
                    itemBinding: ItemChatListChooseBinding,
                    position: Int
                ) {
//                    itemBinding.itemChatListProfileIv.setImageResource(R.drawable.ic_check_circle)
//                    homeRVAdapter.setChecked(position)
//                    homeRVAdapter.setChooseChecked(itemBinding, position)
                    selectedItemViewModel.setSelectedItemList(homeRVAdapter.getSelectedItem())
                }
            })

        binding.homeRecyclerView.adapter = homeRVAdapter
    }

    // 새로 온 채팅을 확인했을 때
    private fun checkNewChat(position: Int) {
        database.chatDao().updateIsNew(chatList[position].chatIdx, 0)
        database.chatListDao().updateIsNew(chatList[position].chatIdx, 0)
    }

    // 기본 모드 세팅
    private fun setDefaultMode() {
        homeRVAdapter.clearSelectedItemList()
        selectedItemViewModel.setSelectedItemList(homeRVAdapter.getSelectedItem())

        binding.homeTitleTv.text = "전체 채팅"  // 디자인에 따라 변경
        binding.homeCancelIv.visibility = View.GONE
        binding.homeDeleteIv.visibility = View.GONE
        binding.homeBlockIv.visibility = View.GONE
        binding.homeCloudIv.visibility = View.VISIBLE
        binding.homeLayout.setBackgroundColor(Color.parseColor("#F2F2F2"))
    }

    // 선택 모드 세팅
    private fun setChooseMode() {
        selectedItemViewModel.setSelectedItemList(homeRVAdapter.getSelectedItem())

        binding.homeTitleTv.text = null // 디자인에 따라 변경
        binding.homeCancelIv.visibility = View.VISIBLE
        binding.homeDeleteIv.visibility = View.VISIBLE
        binding.homeBlockIv.visibility = View.VISIBLE
        binding.homeCloudIv.visibility = View.GONE
        binding.homeLayout.setBackgroundColor(Color.parseColor("#FFFFFF"))
    }

    // click listener 초기화
    private fun initClickListener() {
        Log.d(FRAG, "HOME/initClickListener")

        // 선택 모드 취소 버튼 클릭했을 때 기본 모드로 세팅
        binding.homeCancelIv.setOnClickListener {
            chatViewModel.setMode(0)
        }

        // 차단하기 클릭했을 때
        binding.homeBlockIv.setOnClickListener {
            val selectedChatList = homeRVAdapter.getSelectedItem()
            for (i in selectedChatList) {
                if (i.groupName != "null") i.groupName?.let { it1 ->
                    database.chatDao().blockOrgChat(userID, it1)
                }
                else database.chatDao().blockOneChat(userID, i.groupName!!)
            }

            homeRVAdapter.blockSelectedItemList()
            chatViewModel.setMode(0)    // 혹은 바로 setDefaultMode() 가능
            Toast.makeText(requireContext(), "차단되었습니다.", Toast.LENGTH_SHORT).show()
        }

        // 삭제하기 클릭했을 때
        binding.homeDeleteIv.setOnClickListener {
            homeRVAdapter.removeSelectedItemList()
            chatViewModel.setMode(0)    // 혹은 바로 setDefaultMode() 가능
            Toast.makeText(requireContext(), "삭제되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}