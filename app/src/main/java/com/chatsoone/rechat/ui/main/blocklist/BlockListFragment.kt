package com.chatsoone.rechat.ui.main.blocklist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.chatsoone.rechat.data.entity.BlockedChatList
import com.chatsoone.rechat.data.local.AppDatabase
import com.chatsoone.rechat.databinding.FragmentBlockListBinding
import com.chatsoone.rechat.ui.main.MainActivity
import com.chatsoone.rechat.util.getID

class BlockListFragment : Fragment() {
    private lateinit var binding: FragmentBlockListBinding
    private lateinit var database: AppDatabase
    private lateinit var blockListRVAdapter: BlockListRVAdapter

    private val userID = getID()
    private var blockList = ArrayList<BlockedChatList>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBlockListBinding.inflate(layoutInflater, container, false)
        database = AppDatabase.getInstance(requireContext())!!
//        database = activity?.let { AppDatabase.getInstance(it) }!!

        initData()
        initRecyclerView()

        return binding.root
    }

    private fun initData() {
        // 모든 차단된 목록을 가져온다.
        database.chatDao().getBlockedChatList(userID).observe(viewLifecycleOwner) {
            // viewLifecycleOwner <-> this
            blockList.clear()
            blockList.addAll(it)
        }
    }

    private fun initRecyclerView() {
        val linearLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)
        linearLayoutManager.stackFromEnd = true
        binding.blockListRecyclerView.layoutManager = linearLayoutManager

        blockListRVAdapter = BlockListRVAdapter(
            this.activity as MainActivity,
            blockList,
            object : BlockListRVAdapter.MyClickListener {
                override fun onRemoveItem(blockList: BlockedChatList) {
                    // 지우기
                    if (blockList.groupName == null || blockList.groupName == "null") {
                        // 개인톡
                        database.chatDao().unblockOneChat(userID, blockList.blockedName)
                    } else {
                        // 그룹톡
                        database.chatDao().unblockOrgChat(userID, blockList.groupName)
                    }
                }
            })

        binding.blockListRecyclerView.adapter = blockListRVAdapter
    }
}