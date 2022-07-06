package com.chatsoone.rechat.ui.chat

import android.util.Log
import android.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import com.chatsoone.rechat.ApplicationClass.Companion.ACT
import com.chatsoone.rechat.ApplicationClass.Companion.currentWindowMetricsPointCompat
import com.chatsoone.rechat.base.BaseActivity
import com.chatsoone.rechat.data.remote.FolderContent
import com.chatsoone.rechat.data.remote.FolderList
import com.chatsoone.rechat.data.remote.chat.ChatService
import com.chatsoone.rechat.data.remote.folder.FolderService
import com.chatsoone.rechat.databinding.ActivityFolderContentBinding
import com.chatsoone.rechat.ui.view.ChatView
import com.chatsoone.rechat.ui.view.GetFolderContentView
import com.chatsoone.rechat.util.getID
import com.google.gson.Gson

class FolderContentActivity :
    BaseActivity<ActivityFolderContentBinding>(ActivityFolderContentBinding::inflate),
    GetFolderContentView, ChatView {
    private lateinit var folderContentRVAdapter: FolderContentRVAdapter
    private lateinit var folderService: FolderService
    private lateinit var chatService: ChatService

    private var folderContentList = ArrayList<FolderContent>()
    lateinit var folderInfo: FolderList
    private val userID = getID()

    override fun initAfterBinding() {
        folderService = FolderService()
        chatService = ChatService()

        initData()
        initClickListener()
    }

    // 데이터 초기화
    private fun initData() {
        // 전 페이지에서 데이터 가져오는 부분
        if (intent.hasExtra("folderData")) {
            val folderJson = intent.getStringExtra("folderData")
            folderInfo = Gson().fromJson(folderJson, FolderList::class.java)

            Log.d(ACT, "FOLDERCONTENT/folderInfo: $folderInfo")
            binding.folderContentNameTv.text = folderInfo.folderName
        }

        initFolderContent()
    }

    private fun initFolderContent() {
        // 휴대폰 윈도우 사이즈를 가져온다.
        val size = windowManager.currentWindowMetricsPointCompat()
        // RecyclerView click listener 초기화
        folderContentRVAdapter =
            FolderContentRVAdapter(this, size, object : FolderContentRVAdapter.MyClickListener {
                // 채팅 삭제
                override fun onRemoveChat(chatIdx: Int) {
                    chatService.deleteChatFromFolder(
                        this@FolderContentActivity,
                        userID,
                        chatIdx,
                        folderInfo.folderIdx
                    )
                }

                // 채팅 롱클릭 시 팝업 메뉴
                override fun onChatLongClick(popupMenu: PopupMenu) {
                    popupMenu.show()
                }
            })
        chatService.getFolderContent(this, userID, folderInfo.folderIdx)
    }

    // recycler view 초기화
    private fun initRecyclerView() {
        folderContentRVAdapter.addItem(this.folderContentList)
        binding.folderContentRecyclerView.adapter = folderContentRVAdapter

        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        linearLayoutManager.stackFromEnd = true
        binding.folderContentRecyclerView.layoutManager = linearLayoutManager
    }

    private fun initClickListener() {
        // 뒤로 가기 버튼 눌렀을 때
        binding.folderContentBackIv.setOnClickListener {
            finish()
        }
    }

    override fun onGetFolderContentSuccess(folderContents: ArrayList<FolderContent>) {
        Log.d(ACT, "FOLDERCONTENT/onGetFolderContentSuccess/folderContents: $folderContents")
        this.folderContentList.clear()
        this.folderContentList.addAll(folderContents)
        initRecyclerView()
    }

    override fun onGetFolderContentFailure(code: Int, message: String) {
        Log.d(ACT, "FOLDERCONTENT/onGetFolderContentFailure/code: $code, message: $message")
    }

    override fun onChatSuccess() {
        Log.d(ACT, "FOLDERCONTENT/onChatSuccess")
    }

    override fun onChatFailure(code: Int, message: String) {
        Log.d(ACT, "FOLDERCONTENT/onChatFailure/code: $code, message: $message")
    }
}
