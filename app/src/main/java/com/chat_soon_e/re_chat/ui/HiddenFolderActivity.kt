package com.chat_soon_e.re_chat.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Insets
import android.graphics.Point
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.PopupWindow
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.chat_soon_e.re_chat.R
import com.chat_soon_e.re_chat.data.entities.Folder
import com.chat_soon_e.re_chat.data.entities.Icon
import com.chat_soon_e.re_chat.data.local.AppDatabase
import com.chat_soon_e.re_chat.data.remote.folder.FolderService
import com.chat_soon_e.re_chat.data.remote.folder.HiddenFolderList
import com.chat_soon_e.re_chat.databinding.ActivityHiddenFolderBinding
import com.chat_soon_e.re_chat.databinding.ItemHiddenFolderBinding
import com.chat_soon_e.re_chat.databinding.ItemIconBinding
import com.chat_soon_e.re_chat.ui.view.HiddenFolderListView
import com.chat_soon_e.re_chat.ui.view.UnhideFolderView
import com.chat_soon_e.re_chat.utils.getID
import com.google.gson.Gson

class HiddenFolderActivity: BaseActivity<ActivityHiddenFolderBinding>(ActivityHiddenFolderBinding::inflate),
    HiddenFolderListView, UnhideFolderView {
    private lateinit var database: AppDatabase
    private lateinit var hiddenFolderRVAdapter: HiddenFolderRVAdapter
    private lateinit var iconRVAdapter: ChangeIconRVAdapter
    private lateinit var mPopupWindow: PopupWindow

    private var hiddenFolderList = ArrayList<Folder>()
    private var iconList = ArrayList<Icon>()
    private val userID = getID()
    private val tag = "ACT/HIDDEN-FOLDER"

    override fun initAfterBinding() {
        initFolder()
        initClickListener()
    }

    // 폴더 리스트 초기화
    private fun initFolder() {
        database = AppDatabase.getInstance(this)!!

        val folderService = FolderService()
        folderService.getHiddenFolderList(this, userID)

        // RecyclerView 초기화
        hiddenFolderRVAdapter = HiddenFolderRVAdapter(this)
        binding.hiddenFolderListRecyclerView.adapter = hiddenFolderRVAdapter

        database.folderDao().getHiddenFolder(userID).observe(this){
            hiddenFolderRVAdapter.addFolderList(it as ArrayList<Folder>)
        }

        hiddenFolderRVAdapter.setMyItemClickListener(object: HiddenFolderRVAdapter.MyItemClickListener {
            // 폴더 숨김 해제
            override fun onShowFolder(folderIdx: Int) {
                // 데이터베이스에 폴더 상태를 HIDDEN에서 ACTIVE로 바꿔준다.
                database.folderDao().updateFolderHide(folderIdx)

                val folderService = FolderService()
                folderService.unhideFolder(this@HiddenFolderActivity, userID, folderIdx)
            }

            // 폴더 삭제
            override fun onRemoveFolder(folderIdx: Int) {
                // 데이터베이스에 폴더 상태를 HIDDEN에서 DELETED로 바꿔준다.
                database.folderDao().deleteFolder(folderIdx)
            }

            // 폴더 클릭 시 이동
            override fun onFolderClick(view: View, position: Int) {
                val selectedFolder = hiddenFolderRVAdapter.getSelectedFolder(position)
                val gson = Gson()
                val selectedFolderJson = gson.toJson(selectedFolder)

                // 폴더 정보 보내기
                val intent = Intent(this@HiddenFolderActivity, FolderContentActivity::class.java)
                intent.putExtra("folderData", selectedFolderJson)
                startActivity(intent)
            }

            // 폴더 롱클릭 시 팝업 메뉴
            override fun onFolderLongClick(popupMenu: PopupMenu) {
                popupMenu.show()
            }

            // 폴더 이름 롱클릭 시 이름 변경
            override fun onFolderNameLongClick(itemHiddenFolderBinding: ItemHiddenFolderBinding, folderIdx: Int) {
                changeFolderName(itemHiddenFolderBinding, folderIdx)
            }
        })
    }

    // 클릭 리스너 초기화
    private fun initClickListener() {
        // 내폴더 아이콘 눌렀을 때
        binding.hiddenFolderMyFolderIv.setOnClickListener {
            startNextActivity(MyFolderActivity::class.java)
            finish()
        }
    }

    // 이름 바꾸기 팝업 윈도우
    @SuppressLint("InflateParams")

    fun changeFolderName(itemHiddenFolderBinding: ItemHiddenFolderBinding, folderIdx:Int) {
        val size = windowManager.currentWindowMetricsPointCompat()
        val width = (size.x * 0.8f).toInt()
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_window_change_name, null)

        mPopupWindow = PopupWindow(popupView, width, WindowManager.LayoutParams.WRAP_CONTENT)
        mPopupWindow.animationStyle = 0
        mPopupWindow.isFocusable = true
        mPopupWindow.isOutsideTouchable = true
        mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        mPopupWindow.setOnDismissListener(PopupWindowDismissListener())
        binding.hiddenFolderBackgroundView.visibility = View.VISIBLE

        // 기존 폴더 이름을 팝업 윈도우의 EditText에 넘겨준다.
        var text: String = itemHiddenFolderBinding.itemHiddenFolderTv.text.toString()
        mPopupWindow.contentView.findViewById<EditText>(R.id.popup_window_change_name_et).setText(text)

        database = AppDatabase.getInstance(this@HiddenFolderActivity)!!

        // 입력 완료했을 때 누르는 버튼
        mPopupWindow.contentView.findViewById<AppCompatButton>(R.id.popup_window_change_name_button).setOnClickListener {
            // 바뀐 폴더 이름을 뷰와 RoomDB에 각각 적용해준다.
            text = mPopupWindow.contentView.findViewById<EditText>(R.id.popup_window_change_name_et).text.toString()
            itemHiddenFolderBinding.itemHiddenFolderTv.text = text
            database.folderDao().updateFolderName(folderIdx,text)
            mPopupWindow.dismiss()  // 팝업 윈도우 종료
        }
    }

    // 아이콘 바꾸기 팝업 윈도우
    @SuppressLint("InflateParams")
    fun changeIcon(itemHiddenFolderBinding: ItemHiddenFolderBinding, position: Int, folderListFromAdapter: ArrayList<Folder>) {
        val size = windowManager.currentWindowMetricsPointCompat()
        val width = (size.x * 0.8f).toInt()
        val height = (size.y * 0.6f).toInt()
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_window_change_icon, null)

        mPopupWindow = PopupWindow(popupView, width, height)
        mPopupWindow.animationStyle = 0
        mPopupWindow.isFocusable = true
        mPopupWindow.isOutsideTouchable = true
        mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        mPopupWindow.setOnDismissListener(PopupWindowDismissListener())
        binding.hiddenFolderBackgroundView.visibility = View.VISIBLE

        // 데이터베이스로부터 아이콘 리스트 불러와 연결해주기
        database = AppDatabase.getInstance(this@HiddenFolderActivity)!!
        iconList = database.iconDao().getIconList() as ArrayList
        iconRVAdapter = ChangeIconRVAdapter(iconList)
        popupView.findViewById<RecyclerView>(R.id.popup_window_change_icon_recycler_view).adapter = iconRVAdapter
        iconRVAdapter.setMyItemClickListener(object: ChangeIconRVAdapter.MyItemClickListener {
            // 아이콘을 선택했을 경우
            override fun onIconClick(itemIconBinding: ItemIconBinding, iconPosition: Int) {//icon 포지션
                // 선택한 아이콘으로 폴더 이미지 변경
                val selectedIcon = iconList[iconPosition]
                itemHiddenFolderBinding.itemHiddenFolderIv.setImageResource(selectedIcon.iconImage)

                database.folderDao().updateFolderIcon(folderListFromAdapter[position].idx, selectedIcon.iconImage)

                mPopupWindow.dismiss()  // 팝업 윈도우 종료
            }
        })
    }

    // 디바이스 크기에 사이즈를 맞추기 위한 함수
    private fun WindowManager.currentWindowMetricsPointCompat(): Point {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val windowInsets = currentWindowMetrics.windowInsets
            var insets: Insets = windowInsets.getInsets(WindowInsets.Type.navigationBars())
            windowInsets.displayCutout?.run {
                insets = Insets.max(insets, Insets.of(safeInsetLeft, safeInsetTop, safeInsetRight, safeInsetBottom))
            }
            val insetsWidth = insets.right + insets.left
            val insetsHeight = insets.top + insets.bottom
            Point(currentWindowMetrics.bounds.width() - insetsWidth, currentWindowMetrics.bounds.height() - insetsHeight)
        } else{
            Point().apply {
                defaultDisplay.getSize(this)
            }
        }
    }

    inner class PopupWindowDismissListener(): PopupWindow.OnDismissListener {
        override fun onDismiss() {
            binding.hiddenFolderBackgroundView.visibility = View.INVISIBLE
        }
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onHiddenFolderListSuccess(hiddenFolderList: ArrayList<HiddenFolderList>) {
        Log.d(tag, "onHiddenFolderListSuccess()/hiddenFolderList: $hiddenFolderList")
//        for(i in 0 until hiddenFolderList.size) {
//            database.folderDao().insert(Folder(userID, hiddenFolderList[i].folderName, hiddenFolderList[i].folderImg))
//            this.hiddenFolderList.add(Folder(userID, hiddenFolderList[i].folderName, hiddenFolderList[i].folderImg))
//        }
//        hiddenFolderRVAdapter.addFolderList(this.hiddenFolderList)
    }

    override fun onHiddenFolderListFailure(code: Int, message: String) {
        Log.d(tag, "onHiddenFolderListFailure()/code: $code, message: $message")
    }

    override fun onUnhideFolderSuccess() {
        Log.d(tag, "onUnhideFolderSuccess()/hiddenFolderList: $hiddenFolderList")
    }

    override fun onUnhideFolderFailure(code: Int, message: String) {
        Log.d(tag, "onUnhideFolderFailure()/code: $code, message: $message")
    }
}