package com.chat_soon_e.re_chat.ui

import androidx.recyclerview.widget.LinearLayoutManager
import com.chat_soon_e.re_chat.data.local.AppDatabase
import com.chat_soon_e.re_chat.data.remote.chat.BlockedChatList
import com.chat_soon_e.re_chat.databinding.ActivityBlockListBinding
import com.chat_soon_e.re_chat.utils.getID

class BlockListActivity:BaseActivity<ActivityBlockListBinding>(ActivityBlockListBinding::inflate) {
    lateinit var blockListRVAdapter: BlockListRVAdapter
    lateinit var database:AppDatabase
    private var blockedList=ArrayList<BlockedChatList>()
    private val userID=getID()

    override fun initAfterBinding() {
        //초기 설정
        initData()
        initRecyclerView()
    }
    private fun initData(){
        //모든 차단된 목록을 가져온다.
        database= AppDatabase.getInstance(this)!!
        val initData=ArrayList<BlockedChatList>()
        database.chatDao().getBlockedChatList(userID).observe(this){
            initData.clear()
            initData.addAll(it)
        }
        if(blockedList.isEmpty()){
            //현재 비어있는상태라면
            blockedList=initData
        }
    }
    private fun initRecyclerView() {
        database = AppDatabase.getInstance(this)!!

        val linearLayoutManager= LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        linearLayoutManager.stackFromEnd = true
        binding.blockListRecyclerView.layoutManager = linearLayoutManager

//        blockListRVAdapter = BlockListRVAdapter(this, blockedList
//            }

        }