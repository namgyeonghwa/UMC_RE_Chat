package com.chatsoone.rechat.ui.chat

import android.os.Bundle
import android.util.Log
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.chatsoone.rechat.ApplicationClass.Companion.ACT
import com.chatsoone.rechat.ApplicationClass.Companion.currentWindowMetricsPointCompat
import com.chatsoone.rechat.data.entity.Folder
import com.chatsoone.rechat.data.local.AppDatabase
import com.chatsoone.rechat.databinding.ActivityFolderContentBinding
import com.chatsoone.rechat.util.getID
import com.google.gson.Gson

class FolderContentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFolderContentBinding
    private lateinit var database: AppDatabase
    private lateinit var folderContentRVAdapter: FolderContentRVAdapter
    private lateinit var folderInfo: Folder

    private val userID = getID()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFolderContentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getInstance(this)!!

        initData()
        initRecyclerView()
        initClickListener()
    }

    // 데이터 초기화
    private fun initData() {
        // 전 페이지에서 데이터 가져오는 부분
        if (intent.hasExtra("folderData")) {
            val folderJson = intent.getStringExtra("folderData")
            folderInfo = Gson().fromJson(folderJson, Folder::class.java)

            Log.d(ACT, "FOLDERCONTENT/folderInfo: $folderInfo")
            binding.folderContentNameTv.text = folderInfo.folderName
        }
    }

    // recycler view 초기화
    private fun initRecyclerView() {
        // 휴대폰 윈도우 사이즈를 가져온다.
        val size = windowManager.currentWindowMetricsPointCompat()

        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        linearLayoutManager.stackFromEnd = true
        binding.folderContentRecyclerView.layoutManager = linearLayoutManager

        // FolderContent 데이터를 RecyclerView 어댑터와 연결
        // userID: kakaoUserIdx, folderInfo.idx: folder index
        database.folderContentDao().getFolderChat(userID, folderInfo.idx).observe(this) {
            folderContentRVAdapter.addItem(it)
        }

        folderContentRVAdapter =
            FolderContentRVAdapter(this, size, object : FolderContentRVAdapter.MyClickListener {
                override fun onRemoveChat(chatIdx: Int) {
                    // 채팅 삭제
                    database.folderContentDao().deleteChat(folderInfo.idx, chatIdx)
                }

                override fun onChatLongClick(popupMenu: PopupMenu) {
                    // 채팅 롱클릭 시 팝업 메뉴
                    popupMenu.show()
                }
            })

        binding.folderContentRecyclerView.adapter = folderContentRVAdapter
    }

    private fun initClickListener() {
        // 뒤로 가기 버튼 눌렀을 때
        binding.folderContentBackIv.setOnClickListener {
            finish()
        }
    }
}