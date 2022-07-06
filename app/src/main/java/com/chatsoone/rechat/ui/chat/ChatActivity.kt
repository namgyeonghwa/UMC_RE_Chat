package com.chatsoone.rechat.ui.chat

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chatsoone.rechat.ApplicationClass.Companion.ACT
import com.chatsoone.rechat.ApplicationClass.Companion.currentWindowMetricsPointCompat
import com.chatsoone.rechat.ApplicationClass.Companion.showToast
import com.chatsoone.rechat.R
import com.chatsoone.rechat.base.BaseActivity
import com.chatsoone.rechat.data.remote.ChatList
import com.chatsoone.rechat.data.remote.FolderList
import com.chatsoone.rechat.data.remote.chat.ChatService
import com.chatsoone.rechat.data.remote.folder.FolderService
import com.chatsoone.rechat.databinding.ActivityChatBinding
import com.chatsoone.rechat.databinding.ItemFolderListBinding
import com.chatsoone.rechat.ui.adapter.FolderListRVAdapter
import com.chatsoone.rechat.ui.view.ChatView
import com.chatsoone.rechat.ui.view.FolderListView
import com.chatsoone.rechat.ui.view.GetChatView
import com.chatsoone.rechat.ui.viewmodel.ChatTypeViewModel
import com.chatsoone.rechat.util.getID

class ChatActivity : BaseActivity<ActivityChatBinding>(ActivityChatBinding::inflate), ChatView,
    GetChatView, FolderListView {
    private lateinit var chatListData: ChatList
    private lateinit var chatRVAdapter: ChatRVAdapter
    private lateinit var mPopupWindow: PopupWindow
    private lateinit var folderListRVAdapter: FolderListRVAdapter
    private lateinit var folderService: FolderService
    private lateinit var chatService: ChatService

    private var userID = getID()
    private var isFabOpen = false   // FAB(FloatingActionButton)가 열렸는지 체크해주는 변수
    private var chatList = ArrayList<ChatList>()
    private var folderList = ArrayList<FolderList>()
    private val chatTypeViewModel: ChatTypeViewModel by viewModels()

    override fun initAfterBinding() {
        folderService = FolderService()
        chatService = ChatService()

        setFabClose()
        initData()
        initClickListener()
    }

    private fun setFabOpen() {
        isFabOpen = true
        chatTypeViewModel.setMode(mode = 1)

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
        chatTypeViewModel.setMode(mode = 0)

        ObjectAnimator.ofFloat(binding.chatCancelFab, "translationY", 0f).apply { start() }
        ObjectAnimator.ofFloat(binding.chatDeleteFab, "translationY", 0f).apply { start() }

        binding.chatMainFab.setImageResource(R.drawable.ic_cloud)
        binding.chatCancelFab.isClickable = false
        binding.chatCancelFab.visibility = View.INVISIBLE
        binding.chatDeleteFab.isClickable = false
        binding.chatDeleteFab.visibility = View.INVISIBLE
        binding.chatBackgroundView.visibility = View.INVISIBLE  // 필요한가?
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
                binding.chatNameTv.text = chatListData.chatName
            } else {
                binding.chatNameTv.text = chatListData.groupName
            }
        }

        initChat()
    }

    // 갠톡 or 단톡 가져오기
    private fun initChat() {
        val size = windowManager.currentWindowMetricsPointCompat()
        chatRVAdapter = ChatRVAdapter(this, size, object : ChatRVAdapter.MyItemClickListener {
            // 채팅 삭제
            override fun onRemoveChat() {
                Log.d("chatPositionCheck", "지우려는 채팅들 chatLIst: $chatList")

                // Server API: 채팅들 지우기
                // 선택된 chatIdx들 모두 가져와서 지우기
                val selectedList = chatRVAdapter.getSelectedItemList()
                for (i in selectedList) {
                    chatService.deleteChat(this@ChatActivity, userID, i)
                }

                initChat()
                // init
                //chatService.getChat(this@ChatActivity, userID, chatListData.chatIdx, chatListData.groupName)
            }

            // 선택 모드
            override fun onChooseChatClick(view: View, position: Int) {
                chatRVAdapter.setChecked(position)
                Log.d("chatPositionCheck", "selected position $position")
            }
        })
        chatService.getChat(this, userID, chatListData.chatIdx, chatListData.groupName)
    }

    // recycler view 초기화
    private fun initRecyclerView() {
        chatRVAdapter.addItem(chatList)

        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        linearLayoutManager.stackFromEnd = true
        binding.chatChatRecyclerView.layoutManager = linearLayoutManager

        observeMode()
        binding.chatChatRecyclerView.adapter = chatRVAdapter

        setFabClose()   // 폴더 선택 모드를 해제하기 위해
    }

    // chat view model mode live data 관찰
    private fun observeMode() {
        chatTypeViewModel.mode.observe(this) {
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

    private fun initClickListener() {
        // 메인 fab 버튼 눌렀을 때
        binding.chatMainFab.setOnClickListener {
            if (chatTypeViewModel.mode.value == 0) chatTypeViewModel.setMode(mode = 1)
            else chatTypeViewModel.setMode(mode = 0)

            if (isFabOpen) {
                // fab 버튼이 열려있는 경우 (선택 모드에서 클릭했을 때)
                // 폴더로 보내는 팝업창을 띄운다.
                // 여기서 view는 클릭된 뷰를 의미한다.
                initFolder()
            } else {
                // fab 버튼이 닫혀있는 경우 (일반 모드에서 클릭했을 때)
                // fab 버튼을 연다.
                setFabOpen()
            }
        }

        // 삭제하는 경우
        binding.chatDeleteFab.setOnClickListener {
            showToast(this, "삭제되었습니다.")

            val data = chatRVAdapter.removeChat()
            if (data != null) chatListData = data

            // 일반 모드로
            setFabClose()
            chatRVAdapter.clearSelectedItemList()

            initChat()
        }

        // 뒤로 가기 버튼
        binding.chatBackIv.setOnClickListener {
            finish()
        }

        // 선택 모드 취소 버튼 클릭 시
        binding.chatCancelFab.setOnClickListener {
            setFabClose()
            initChat()
        }
    }

    // 폴더 목록 초기화
    private fun initFolder() {
        folderListRVAdapter = FolderListRVAdapter(this)
        folderService.getFolderList(this, userID)
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

        // RecyclerView 초기화
        // 더미 데이터와 어댑터 연결
        folderListRVAdapter.addFolderList(this.folderList)
        recyclerView.adapter = folderListRVAdapter

        folderListRVAdapter.setMyItemClickListener(object :
            FolderListRVAdapter.MyItemClickListener {
            override fun onFolderClick(itemBinding: ItemFolderListBinding, itemPosition: Int) {
                // 이동하고 싶은 폴더 클릭 시 폴더로 채팅 이동 (뷰에는 그대로 남아 있도록)
                // 폴더로 이동시키는 코드 작성
                val selectedChatIdx = chatRVAdapter.getSelectedItemList()

                // Server API: 폴더에 한개의 채팅들 삽입
                for (i in selectedChatIdx) {
                    chatService = ChatService()
                    chatService.addChatToFolder(
                        this@ChatActivity,
                        userID,
                        i,
                        folderList[itemPosition]
                    )
                }
                mPopupWindow.dismiss()
                binding.chatBackgroundView.visibility = View.INVISIBLE
                setFabClose()

                chatRVAdapter.clearSelectedItemList()
                chatTypeViewModel.setMode(mode = 0)
            }
        })
    }

    override fun onBackPressed() {
        finish()
    }

    inner class PopupWindowDismissListener() : PopupWindow.OnDismissListener {
        override fun onDismiss() {
            binding.chatBackgroundView.visibility = View.INVISIBLE
        }
    }

    override fun onGetChatSuccess(chats: ArrayList<ChatList>) {
        // 성공시
        Log.d(ACT, "CHAT/onGetChatSuccess(): $chats")
        // ????????????????????
        Log.d("afterDeleteChat", "reset_chat: " + chatRVAdapter.chatList.toString())
        this.chatList.clear()
        this.chatList.addAll(chats)
        initRecyclerView()
    }

    override fun onGetChatFailure(code: Int, message: String) {
        // 실패시
        Log.d(ACT, "CHAT/onGetChatFailure/code: $code, message: $message")
    }

    override fun onFolderListSuccess(folderList: ArrayList<FolderList>) {
        // 성공 시
        Log.d(ACT, "CHAT/onFolderListSuccess/folderList: $folderList")
        this.folderList.clear()
        this.folderList.addAll(folderList)
        popupWindowToFolder()
    }

    override fun onFolderListFailure(code: Int, message: String) {
        // 실패시
        // 폴더 리스트를 "null"로 설정해줘야 할까? 채팅 폴더이동이 끝나고 "RVAdapter"에 넣는 부분 때문에
        Log.d(ACT, "CHAT/onFolderListFailure/code: $code, message: $message")
    }

    override fun onChatSuccess() {
        Log.d(ACT, "CHAT/onChatSuccess")
    }

    override fun onChatFailure(code: Int, message: String) {
        Log.d(ACT, "CHAT/onChatFailure/code: $code, message: $message")
    }
}
