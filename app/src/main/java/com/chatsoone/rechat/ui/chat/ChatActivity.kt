package com.chatsoone.rechat.ui.chat

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chatsoone.rechat.ApplicationClass
import com.chatsoone.rechat.ApplicationClass.Companion.ACT
import com.chatsoone.rechat.ApplicationClass.Companion.currentWindowMetricsPointCompat
import com.chatsoone.rechat.R
import com.chatsoone.rechat.data.entity.ChatList
import com.chatsoone.rechat.data.entity.Folder
import com.chatsoone.rechat.data.local.AppDatabase
import com.chatsoone.rechat.databinding.ActivityChatBinding
import com.chatsoone.rechat.databinding.ItemFolderListBinding
import com.chatsoone.rechat.ui.ChatViewModel
import com.chatsoone.rechat.ui.FolderListRVAdapter
import com.chatsoone.rechat.ui.pattern.CreatePatternActivity
import com.chatsoone.rechat.ui.pattern.InputPatternActivity
import com.chatsoone.rechat.util.DiffUtilCallback
import com.chatsoone.rechat.util.getID

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var database: AppDatabase
    private lateinit var chatListData: ChatList
    private lateinit var chatRVAdapter: ChatRVAdapter
    private lateinit var mPopupWindow: PopupWindow

    private var userID = getID()
    private var isFabOpen = false   // FAB(FloatingActionButton)가 열렸는지 체크해주는 변수
    private var chatList = ArrayList<ChatList>()
    private var folderList = ArrayList<Folder>()
    private val chatViewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getInstance(this)!!

        setFabClose()   // fab 버튼 초기화
        initData()
        initFolder()
        initRecyclerView()
        initClickListener()
    }

    // home fragment로부터 데이터를 가져온다.
    private fun initData() {
        // -1: 모든 채팅 목록, 1: 특정 채팅방 목록
        val isAll = getSharedPreferences("chatAll", MODE_PRIVATE).getInt("chatAll", 0)

        // fragment -> activity 데이터 받아오기
        if (intent.hasExtra("chatListJson")) {
            chatListData = intent.getSerializableExtra("chatListJson") as ChatList
            Log.d(ACT, "CHAT/chatListData: $chatListData")

            if (chatListData.groupName == null || chatListData.groupName == "null") {
                binding.chatNameTv.text = chatListData.nickName
            } else {
                binding.chatNameTv.text = chatListData.groupName
            }
        }
    }

    // 폴더 목록 초기화
    private fun initFolder() {
        database.folderDao().getFolderList(userID).observe(this) {
            folderList.clear()
            folderList.addAll(it as ArrayList<Folder>)
        }
    }

    // recycler view 초기화
    private fun initRecyclerView() {
        val size = windowManager.currentWindowMetricsPointCompat()

        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        linearLayoutManager.stackFromEnd = true
        binding.chatChatRecyclerView.layoutManager = linearLayoutManager

        chatRVAdapter = ChatRVAdapter(this, size, object : ChatRVAdapter.MyItemClickListener {

            override fun onRemoveChat(chatIdx: Int) {
                // 채팅 삭제
                AppDatabase.getInstance(this@ChatActivity)!!.chatDao().deleteByChatIdx(chatIdx)
            }

            override fun onChooseChatClick(view: View, position: Int) {
                chatRVAdapter.setChecked(position)
            }
        })

        binding.chatChatRecyclerView.adapter = chatRVAdapter
        observeMode()
        observeData()
    }

    // chat view model mode live data 관찰
    private fun observeMode() {
        chatViewModel.mode.observe(this) {
            if (it == 0) {
                // 일반 모드
                chatRVAdapter.clearSelectedItemList()
                chatRVAdapter.addItem(chatList)
            } else {
                // 선택 모드
                chatRVAdapter.clearSelectedItemList()
                chatRVAdapter.addItem(chatList)
            }
            // 모든 데이터의 viewType 바꿔주기
            chatRVAdapter.setViewType(currentMode = it)
        }
    }

    private fun observeData() {
        if (chatListData.groupName == "null")
            database.chatDao().getOneChatList(userID, chatListData.chatIdx).observe(this) {
                if (it.isEmpty()) {
                    val data = database.chatDao().getOneChatNoLiveList(userID, chatListData.chatIdx)
                    chatRVAdapter.addItem(data)
                    chatList.clear()
                    chatList.addAll(data)
                    binding.chatChatRecyclerView.scrollToPosition(0)
                } else {
                    chatRVAdapter.addItem(it)
                    chatList.clear()
                    chatList.addAll(it)
                    binding.chatChatRecyclerView.scrollToPosition(0)
                }

            }
        else
            database.chatDao().getOrgChatList(userID, chatListData.chatIdx).observe(this) {
                if (it.isEmpty()) {
                    val data = database.chatDao().getOrgChatNoLiveList(userID, chatListData.chatIdx)
                    chatRVAdapter.addItem(data)
                    chatList.clear()
                    chatList.addAll(data)
                    binding.chatChatRecyclerView.scrollToPosition(0)
                } else {
                    chatRVAdapter.addItem(it)
                    chatList.clear()
                    chatList.addAll(it)
                    binding.chatChatRecyclerView.scrollToPosition(0)
                }
            }
    }

    private fun initClickListener() {
        // 메인 fab 버튼 눌렀을 때
        binding.chatMainFab.setOnClickListener {
            if (chatViewModel.mode.value == 0) chatViewModel.setMode(mode = 1)
            else chatViewModel.setMode(mode = 0)

            if (isFabOpen) {
                // fab 버튼이 열려있는 경우 (선택 모드에서 클릭했을 때)
                // 폴더로 보내는 팝업창을 띄운다.
                // 여기서 view는 클릭된 뷰를 의미한다.
                popupWindowToFolder()
            } else {
                // fab 버튼이 닫혀있는 경우 (일반 모드에서 클릭했을 때)
                // fab 버튼을 연다.
                setFabOpen()
            }
        }

        // 삭제하는 경우
        binding.chatDeleteFab.setOnClickListener {
            val data = chatRVAdapter.removeSelectedItemList()
            if (data != null) chatListData = data

            // 일반 모드로
            setFabClose()
            chatRVAdapter.clearSelectedItemList()
        }

        // 뒤로 가기 버튼
        binding.chatBackIv.setOnClickListener {
            finish()
        }

        // 선택 모드 취소 버튼 클릭 시
        binding.chatCancelFab.setOnClickListener {
            // 일반 모드로
            setFabClose()
            chatRVAdapter.clearSelectedItemList()
        }
    }

    private fun setFabOpen() {
        isFabOpen = true
        chatViewModel.setMode(mode = 1)

        ObjectAnimator.ofFloat(binding.chatCancelFab, "translationY", -450f).apply { start() }
        ObjectAnimator.ofFloat(binding.chatDeleteFab, "translationY", -250f).apply { start() }

        binding.chatMainFab.setImageResource(R.drawable.ic_cloud_move)
        binding.chatCancelFab.isClickable = true
        binding.chatCancelFab.visibility = View.VISIBLE
        binding.chatDeleteFab.isClickable = true
        binding.chatDeleteFab.visibility = View.VISIBLE
    }

    private fun setFabClose() {
        isFabOpen = false
        chatViewModel.setMode(mode = 0)

        ObjectAnimator.ofFloat(binding.chatCancelFab, "translationY", 0f).apply { start() }
        ObjectAnimator.ofFloat(binding.chatDeleteFab, "translationY", 0f).apply { start() }

        binding.chatMainFab.setImageResource(R.drawable.ic_cloud)
        binding.chatCancelFab.isClickable = false
        binding.chatCancelFab.visibility = View.INVISIBLE
        binding.chatDeleteFab.isClickable = false
        binding.chatDeleteFab.visibility = View.INVISIBLE
        binding.chatBackgroundView.visibility = View.INVISIBLE  // 필요한가?
    }

    // 선택한 채팅을 폴더로 보낼 수 있는 메뉴뉴
    @SuppressLint("InflateParams")
    private fun popupWindowToFolder() {
        // 팝업 윈도우 사이즈를 잘못 맞추면 아이템들이 안 뜨므로 하드 코딩으로 사이즈 조정해주기
        // 아이콘 16개 (기본)
        val size = windowManager.currentWindowMetricsPointCompat()
        val width = (size.x * 0.8f).toInt()
        val height = (size.y * 0.4f).toInt()

        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_window_to_folder, null)
        mPopupWindow = PopupWindow(popupView, width, height)

        // 애니메이션 설정 (-1: 설정 안 함, 0: 설정)
        mPopupWindow.animationStyle = 0
        mPopupWindow.animationStyle = R.style.Animation

        // 외부 영역 선택 시 팝업 윈도우 종료
        mPopupWindow.isFocusable = true
        mPopupWindow.isOutsideTouchable = true
        mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        mPopupWindow.setOnDismissListener(PopupWindowDismissListener())
        binding.chatBackgroundView.visibility = View.VISIBLE

        val recyclerView =
            popupView.findViewById<RecyclerView>(R.id.popup_window_to_folder_menu_recycler_view)

        // 폴더 목록 불러오기
        val folderListRVAdapter = FolderListRVAdapter(this@ChatActivity)
        recyclerView.adapter = folderListRVAdapter
        folderListRVAdapter.setMyItemClickListener(object :
            FolderListRVAdapter.MyItemClickListener {
            override fun onFolderClick(itemBinding: ItemFolderListBinding, itemPosition: Int) {
                // 이동하고 싶은 폴더 클릭 시 폴더로 채팅 이동 (뷰에는 그대로 남아 있도록)
                val selectedFolder = folderList[itemPosition]
                if (selectedFolder.status == ApplicationClass.HIDDEN) {

                    // 읽는 용도
                    val lockSPF = getSharedPreferences("lock", 0)
                    val pattern = lockSPF.getString("pattern", "0")

                    // 패턴 모드 확인
                    // 0: 숨긴 폴더 목록을 확인하기 위한 입력 모드
                    // 1: 메인 화면의 설정창 -> 변경 모드
                    // 2: 폴더 화면의 설정창 -> 변경 모드
                    // 3: 메인 화면 폴더로 보내기 -> 숨김 폴더 눌렀을 경우

                    // 쓰는 용도
                    val modeSPF = getSharedPreferences("mode", 0)
                    val editor = modeSPF.edit()

                    // 여기서는 3번 모드
                    editor.putInt("mode", 3)
                    editor.apply()

                    if (pattern.equals("0")) {
                        // 패턴이 설정되어 있지 않은 경우 패턴 설정 페이지로
                        val intent = Intent(this@ChatActivity, CreatePatternActivity::class.java)
                        startActivity(intent)
                    } else {
                        // 패턴이 설정되어 있는 경우 입력 페이지로 (보안을 위해)
                        val intent = Intent(this@ChatActivity, InputPatternActivity::class.java)
                        startActivity(intent)
                    }
                }

                // 만약 비밀번호가 틀렸을경우 제대로 취소가 되는지 확인
                // 폴더로 이동시키는 코드 작성
                val selectedChatIdx = chatRVAdapter.getSelectedItemList()
                for (i in selectedChatIdx) {
                    database.folderContentDao().insertChat(folderList[itemPosition].idx, i)
                }

                // 팝업 윈도우를 꺼주는 역할
                mPopupWindow.dismiss()
                setFabClose()
                chatRVAdapter.clearSelectedItemList()
            }
        })

        // folder list recycler view adapter live data
        database.folderDao().getFolderList(userID).observe(this) {
            folderListRVAdapter.addFolderList(it as ArrayList<Folder>)
        }
    }

    private fun updateList(selectedChatList: List<ChatList>?) {
        selectedChatList?.let {
            val diffCallback = DiffUtilCallback(chatList, selectedChatList)
            val diffResult = DiffUtil.calculateDiff(diffCallback)

            this.chatList.run {
                clear()
                addAll(selectedChatList)
                diffResult.dispatchUpdatesTo(chatRVAdapter)
            }
        }
    }

    override fun onBackPressed() {
        finish()
    }

    inner class PopupWindowDismissListener() : PopupWindow.OnDismissListener {
        override fun onDismiss() {
            binding.chatBackgroundView.visibility = View.INVISIBLE
        }
    }
}