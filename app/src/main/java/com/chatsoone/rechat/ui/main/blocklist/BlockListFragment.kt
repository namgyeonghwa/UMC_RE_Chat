package com.chatsoone.rechat.ui.main.blocklist

import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.chatsoone.rechat.ApplicationClass.Companion.FRAG
import com.chatsoone.rechat.base.BaseFragment
import com.chatsoone.rechat.data.local.AppDatabase
import com.chatsoone.rechat.data.remote.BlockedChatList
import com.chatsoone.rechat.data.remote.chat.ChatService
import com.chatsoone.rechat.databinding.FragmentBlockListBinding
import com.chatsoone.rechat.ui.view.ChatView
import com.chatsoone.rechat.ui.view.GetBlockedChatListView
import com.chatsoone.rechat.util.getID

class BlockListFragment : BaseFragment<FragmentBlockListBinding>(FragmentBlockListBinding::inflate),
    GetBlockedChatListView, ChatView {
    private lateinit var blockListRVAdapter: BlockListRVAdapter
    private lateinit var chatService: ChatService
    lateinit var database: AppDatabase

    private val userID = getID()
    private var blockList = ArrayList<BlockedChatList>()

    override fun initAfterBinding() {
        chatService = ChatService()

        initData()
    }

    private fun initData() {
        blockListRVAdapter =
            BlockListRVAdapter(requireContext(), object : BlockListRVAdapter.MyClickListener {
                override fun onRemoveItem(blockList: BlockedChatList) {
                    // 삭제 오류 검토
                    chatService.unblock(
                        this@BlockListFragment,
                        userID,
                        blockList.blockedName,
                        blockList.groupName
                    )
//                chatService.getBlockedChatList(this@BlockListActivity, userID)
                }
            })

        chatService.getBlockedChatList(this, userID)
    }

    private fun initRecyclerView() {
        blockListRVAdapter.addItem(this.blockList)
        val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
        linearLayoutManager.stackFromEnd = true
        binding.blockListRecyclerView.layoutManager = linearLayoutManager
        binding.blockListRecyclerView.adapter = blockListRVAdapter
    }

    override fun onGetBlockedChatListSuccess(blockedChatList: ArrayList<BlockedChatList>) {
        Log.d(FRAG, "BLOCKLIST/onGetBlockedChatListSuccess/blockedChatList: $blockedChatList")
        this.blockList.clear()
        this.blockList.addAll(blockedChatList)
        initRecyclerView()
    }

    override fun onGetBlockedChatListFailure(code: Int, message: String) {
        Log.d(FRAG, "BLOCKLIST/onGetBlockedChatListFailure/code: $code, message: $message")
    }

    override fun onChatSuccess() {
        Log.d(FRAG, "BLOCKLIST/onChatSuccess")
    }

    override fun onChatFailure(code: Int, message: String) {
        Log.d(FRAG, "BLOCKLIST/onChatFailure/code: $code, message: $message")
    }
}
