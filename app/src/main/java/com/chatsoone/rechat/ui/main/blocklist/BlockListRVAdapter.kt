package com.chatsoone.rechat.ui.main.blocklist

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.chatsoone.rechat.ApplicationClass.Companion.loadBitmap
import com.chatsoone.rechat.data.remote.BlockedChatList
import com.chatsoone.rechat.databinding.ItemBlockListBinding

class BlockListRVAdapter(
    private val mContext: Context,
    private val myClickListener: MyClickListener
) : RecyclerView.Adapter<BlockListRVAdapter.ViewHolder>() {
    var blockList = ArrayList<BlockedChatList>()

    interface MyClickListener {
        fun onRemoveItem(blockList: BlockedChatList)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BlockListRVAdapter.ViewHolder {
        val binding = ItemBlockListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(blockList[position])
    }

    override fun getItemCount(): Int = blockList.size

    // Add Data
    @SuppressLint("NotifyDataSetChanged")
    fun addItem(block: List<BlockedChatList>) {
        blockList.clear()
        blockList.addAll(block as ArrayList)
        notifyDataSetChanged()
    }

    private fun removeBlock(position: Int) {
        blockList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount);
    }

    // 디폴트 뷰홀더
    inner class ViewHolder(val binding: ItemBlockListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(block: BlockedChatList) {
            binding.itemBlockCancelIv.setOnClickListener {
                myClickListener.onRemoveItem(blockList[bindingAdapterPosition])
                removeBlock(bindingAdapterPosition) // bindingAdapterPosition not position
            }

            if (block.groupName != null && block.groupName != "null") binding.itemBlockNameTv.text =
                block.groupName
            else binding.itemBlockNameTv.text = block.blockedName

            if (block.blockedProfileImg != null && block.blockedProfileImg != "null") binding.itemBlockProfileIv.setImageBitmap(
                loadBitmap(block.blockedProfileImg, mContext)
            )
        }
    }
}
