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

        // observe mode
        chatViewModel.mode.observe(viewLifecycleOwner, Observer {
            if (it == 0) setDefaultMode()
            else setChooseMode()

            Log.d(FRAG, "HOME/mode: $it")
            // 모든 데이터의 뷰 타입 변경
            homeRVAdapter.setViewType(it)
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
                    homeRVAdapter.clearSelectedItemList()
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

                // 선택 모드 (클릭 시 프로필 변경 & 선택한 뷰 리스트에 넣어주기)
                override fun onChooseChatClick(itemBinding: ItemChatListChooseBinding, position: Int) {
//                    itemBinding.itemChatListProfileIv.setImageResource(R.drawable.ic_check_circle)
                    homeRVAdapter.setChecked(position)
                    selectedItemViewModel.setSelectedItemList(homeRVAdapter.getSelectedItem())
                }

                // 선택 모드로 전환되게끔 (default에서 프로필 클릭 시 선택 모드로 전환)
                override fun onProfileClick(itemBinding: ItemChatListDefaultBinding, position: Int) {
//                    itemBinding.itemChatListProfileIv.setImageResource(R.drawable.ic_check_circle)
                    homeRVAdapter.setChecked(position)
                    chatViewModel.setMode(1)
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
//        homeRVAdapter.clearSelectedItemList()

        binding.homeTitleTv.text = null // 디자인에 따라 변경
        binding.homeCancelIv.visibility = View.VISIBLE
        binding.homeDeleteIv.visibility = View.VISIBLE
        binding.homeBlockIv.visibility = View.VISIBLE
        binding.homeCloudIv.visibility = View.GONE
        binding.homeLayout.setBackgroundColor(Color.parseColor("#FFFFFF"))
    }

    // 폴더로 보내기 팝업 윈도우
    @SuppressLint("InflateParams")
    private fun openPopupWindow() {
        val size = requireActivity().windowManager.currentWindowMetricsPointCompat()
        val width = (size.x * 0.8f).toInt()
        val height = (size.y * 0.4f).toInt()

        val inflater =
            requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_window_to_folder, null)

        mPopupWindow = PopupWindow(popupView, width, height)
        mPopupWindow.animationStyle = R.style.Animation
        mPopupWindow.isFocusable = true
        mPopupWindow.isOutsideTouchable = true
        mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        mPopupWindow.setOnDismissListener(PopupWindowDismissListener())
        binding.homeBgV.visibility = View.VISIBLE

        // 폴더 목록 recycler view 초기화
        initFolderListRecyclerView(popupView)
    }

    // 폴더/보관함 보여주는 recycler view 초기화
    private fun initFolderListRecyclerView(popupView: View) {
        val folderListRV =
            popupView.findViewById<RecyclerView>(R.id.popup_window_to_folder_menu_recycler_view)

        folderListRV.adapter = folderListRVAdapter
        folderListRVAdapter.setMyItemClickListener(object :
            FolderListRVAdapter.MyItemClickListener {
            override fun onFolderClick(itemBinding: ItemFolderListBinding, itemPosition: Int) {
                // 이동하고 싶은 폴더/보관함 클릭했을 때 해당 폴더/보관함으로 채팅 이동
                val selectedFolder = folderList[itemPosition]

                // 숨긴 보관함 같은 경우
                if (selectedFolder.status == ApplicationClass.HIDDEN) {
                    val lockSPF = requireActivity().getSharedPreferences("lock", 0)
                    val pattern = lockSPF.getString("pattern", "0")

                    // 패턴 모드 확인
                    // 0: 숨긴 폴더 목록을 확인하기 위한 입력 모드
                    // 1: 메인 화면의 설정창 -> 변경 모드
                    // 2: 폴더 화면의 설정창 -> 변경 모드
                    // 3: 메인 화면 폴더로 보내기 -> 숨김 폴더 눌렀을 경우
                    val modeSPF = requireActivity().getSharedPreferences("mode", 0)
                    val editor = modeSPF.edit()

                    // 여기서는 3번 모드
                    editor.putInt("mode", 3)
                    editor.apply()

                    if (pattern.equals("0")) {
                        // 패턴이 설정되어 있지 않은 경우 패턴 설정 페이지로
                        val intent =
                            Intent(requireContext(), CreatePatternActivity::class.java)
                        startActivity(intent)
                    } else {
                        // 패턴이 설정되어 있는 경우 입력 페이지로 (보안을 위해)
                        val intent = Intent(requireContext(), InputPatternActivity::class.java)
                        startActivity(intent)
                    }
                }

                val selectedChatList = homeRVAdapter.getSelectedItem()
                Log.d(ApplicationClass.FRAG, "HOME/selectedChatList: $selectedChatList")

                val folderIdx = folderList[itemPosition].idx

                // 갠톡 이동: folderIdx, otherUserIdx
                // 단톡 이동: folderIdx, userIdx, groupName
                for (i in selectedChatList) {
                    if (i.groupName != "null") database.folderContentDao()
                        .insertOrgChat(i.chatIdx, folderIdx, userID)
                    else database.folderContentDao().insertOtOChat(folderIdx, i.chatIdx)
                }

                // 팝업 윈도우 종료
                mPopupWindow.dismiss()
            }
        })
    }

    // click listener 초기화
    private fun initClickListener() {
        Log.d(ApplicationClass.FRAG, "HOME/initClickListener")

        // 선택 모드 취소 버튼 클릭했을 때 기본 모드로 세팅
        binding.homeCancelIv.setOnClickListener {
            homeRVAdapter.clearSelectedItemList()
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

//        // 하단 중앙 버튼 클릭 시 (선택 모드일 때만 가능하게 할 건지 의논 필요)
//        val activityMainBnvBinding = ActivityMainBnvBinding.inflate(layoutInflater)
//        activityMainBnvBinding.mainBnvCenterDefaultIv.setOnClickListener {
//            openPopupWindow()
//        }
    }

    // 팝업창 닫을 때
    inner class PopupWindowDismissListener() : PopupWindow.OnDismissListener {
        override fun onDismiss() {
            chatViewModel.setMode(0)    // 혹은 바로 setDefaultMode() 가능
            binding.homeBgV.visibility = View.INVISIBLE
        }
    }
}