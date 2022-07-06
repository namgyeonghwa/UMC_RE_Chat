package com.chatsoone.rechat.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chatsoone.rechat.ApplicationClass.Companion.RV
import com.chatsoone.rechat.R
import com.chatsoone.rechat.data.remote.FolderList
import com.chatsoone.rechat.databinding.ItemFolderListBinding

class FolderListRVAdapter(private val mContext: Context) :
    RecyclerView.Adapter<FolderListRVAdapter.ViewHolder>() {
    private val folderList = ArrayList<FolderList>()

    // 클릭 인터페이스
    interface MyItemClickListener {
        fun onFolderClick(itemBinding: ItemFolderListBinding, itemPosition: Int)
    }

    // 리스너 객체를 저장하는 변수
    private lateinit var mItemClickListener: MyItemClickListener

    // 리스너 객체를 외부에서 전달받는 함수
    fun setMyItemClickListener(itemClickListener: MyItemClickListener) {
        mItemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemFolderListBinding =
            ItemFolderListBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(folderList[position])

        holder.itemView.setOnClickListener {
            mItemClickListener.onFolderClick(holder.binding, position)
        }
    }

    // RecyclerView에 데이터 연결
    @SuppressLint("NotifyDataSetChanged")
    fun addFolderList(folderList: ArrayList<FolderList>) {
        this.folderList.clear()
        this.folderList.addAll(folderList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = folderList.size

    inner class ViewHolder(val binding: ItemFolderListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(folder: FolderList) {
            binding.itemFolderListTv.text = folder.folderName

            if (folder.folderImg != null) {
                val folderImgId = getFolderImgResource(folder.folderImg)
                if (folderImgId != 0) binding.itemFolderListIv.setImageResource(folderImgId)
                else binding.itemFolderListIv.setImageResource(R.drawable.folder_bear)  // bear를 default로
            } else {
                binding.itemFolderListIv.setImageResource(R.drawable.folder_bear)
            }
        }
    }

    private fun getFolderImgResource(folderImgString: String): Int {
        // res/drawable/파일명.png
        val folderImgStringArray = folderImgString.split("/", ".")
        val folderImgName = folderImgStringArray[2]
        Log.d(RV, "FOLDERLIST/folderImgStringArray: $folderImgStringArray")
        Log.d(RV, "FOLDERLIST/folderImgName: $folderImgName")

        val folderImgID =
            mContext.resources.getIdentifier(folderImgName, "drawable", mContext.packageName)
        Log.d(RV, "FOLDERLIST/folderImgID: $folderImgID")
        return folderImgID
    }
}
