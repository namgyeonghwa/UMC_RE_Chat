package com.chatsoone.rechat.ui.main.hiddenfolder

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.chatsoone.rechat.ApplicationClass.Companion.FRAG
import com.chatsoone.rechat.ApplicationClass.Companion.currentWindowMetricsPointCompat
import com.chatsoone.rechat.R
import com.chatsoone.rechat.base.BaseFragment
import com.chatsoone.rechat.data.entity.Icon
import com.chatsoone.rechat.data.local.AppDatabase
import com.chatsoone.rechat.data.remote.FolderList
import com.chatsoone.rechat.data.remote.folder.FolderService
import com.chatsoone.rechat.databinding.FragmentMyHiddenFolderBinding
import com.chatsoone.rechat.databinding.ItemIconBinding
import com.chatsoone.rechat.databinding.ItemMyHiddenFolderBinding
import com.chatsoone.rechat.ui.adapter.IconRVAdapter
import com.chatsoone.rechat.ui.chat.FolderContentActivity
import com.chatsoone.rechat.ui.main.MainActivity
import com.chatsoone.rechat.ui.view.FolderAPIView
import com.chatsoone.rechat.ui.view.HiddenFolderListView
import com.chatsoone.rechat.util.getID
import com.google.gson.Gson
import java.io.ByteArrayOutputStream

class MyHiddenFolderFragment :
    BaseFragment<FragmentMyHiddenFolderBinding>(FragmentMyHiddenFolderBinding::inflate),
    HiddenFolderListView, FolderAPIView {
    private lateinit var database: AppDatabase
    private lateinit var myHiddenFolderRVAdapter: MyHiddenFolderRVAdapter
    private lateinit var iconRVAdapter: IconRVAdapter
    private lateinit var mPopupWindow: PopupWindow
    private lateinit var folderService: FolderService

    private val userID = getID()
    private var mContext: MainActivity? = null
    private var iconList = ArrayList<Icon>()
    private var hiddenFolderList = ArrayList<FolderList>()

    override fun initAfterBinding() {
        database = AppDatabase.getInstance(requireContext())!!
        folderService = FolderService()
    }

    override fun onResume() {
        super.onResume()
        initFolder()
    }

    // 폴더 리스트 초기화
    private fun initFolder() {
        myHiddenFolderRVAdapter = MyHiddenFolderRVAdapter(this, requireContext())
        folderService.getHiddenFolderList(this, userID)
    }

    private fun initRecyclerView() {
        // RecyclerView 초기화
        myHiddenFolderRVAdapter.addFolderList(this.hiddenFolderList)
        binding.myHiddenFolderRecyclerView.adapter = myHiddenFolderRVAdapter

        myHiddenFolderRVAdapter.setMyItemClickListener(object :
            MyHiddenFolderRVAdapter.MyItemClickListener {

            // 숨김 폴더 다시 해제하기
            override fun onShowFolder(folderIdx: Int) {
                folderService.unhideFolder(this@MyHiddenFolderFragment, userID, folderIdx)
            }

            // 폴더 삭제하기
            override fun onRemoveFolder(folderIdx: Int) {
                folderService.deleteFolder(this@MyHiddenFolderFragment, userID, folderIdx)
            }

            // 폴더 클릭 시 이동
            override fun onFolderClick(view: View, position: Int) {
                // 선택한 폴더의 포지션을 가져와서
                val selectedFolder = myHiddenFolderRVAdapter.getSelectedFolder(position)
                val selectedFolderJson = Gson().toJson(selectedFolder)

                // FolderContentActivity로 해당 폴더 정보 보내기
                val intent = Intent(context, FolderContentActivity::class.java)
                intent.putExtra("folderData", selectedFolderJson)
                startActivity(intent)
            }

            // 폴더 롱클릭 시 팝업 메뉴
            override fun onFolderLongClick(popupMenu: PopupMenu) {
                popupMenu.show()
            }

            // 폴더 이름 롱클릭 시 이름 변경
            override fun onFolderNameLongClick(
                itemHiddenFolderBinding: ItemMyHiddenFolderBinding,
                position: Int,
                folderIdx: Int
            ) {
                changeFolderName(itemHiddenFolderBinding, position, folderIdx)
            }
        })
    }

    // 이름 바꾸기 팝업 윈도우
    @SuppressLint("InflateParams")
    fun changeFolderName(
        itemHiddenFolderBinding: ItemMyHiddenFolderBinding,
        position: Int,
        folderIdx: Int
    ) {
        val size = activity?.windowManager?.currentWindowMetricsPointCompat()
        val width = ((size?.x ?: 0) * 0.8f).toInt()
        val inflater =
            activity?.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_window_change_name, null)

        mPopupWindow = PopupWindow(popupView, width, WindowManager.LayoutParams.WRAP_CONTENT)
        mPopupWindow.animationStyle = 0
        mPopupWindow.isFocusable = true
        mPopupWindow.isOutsideTouchable = true
        mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        mPopupWindow.setOnDismissListener(PopupWindowDismissListener())
        binding.myHiddenFolderBackgroundView.visibility = View.VISIBLE

        // 기존 폴더 이름을 팝업 윈도우의 EditText에 넘겨준다.
        var text: String = itemHiddenFolderBinding.itemHiddenFolderTv.text.toString()
        mPopupWindow.contentView.findViewById<EditText>(R.id.popup_window_change_name_et)
            .setText(text)

        // 입력 완료했을 때 누르는 버튼
        mPopupWindow.contentView.findViewById<AppCompatButton>(R.id.popup_window_change_name_button)
            .setOnClickListener {
                // 바뀐 폴더 이름을 뷰와 RoomDB에 각각 적용해준다.
                text =
                    mPopupWindow.contentView.findViewById<EditText>(R.id.popup_window_change_name_et).text.toString()
                itemHiddenFolderBinding.itemHiddenFolderTv.text = text

                // 폴더 이름 바꾸기
                folderService.changeFolderName(
                    this,
                    userID,
                    folderIdx,
                    FolderList(folderIdx, text, hiddenFolderList[position].folderImg)
                )
                mPopupWindow.dismiss()  // 팝업 윈도우 종료
            }
    }

    // 아이콘 바꾸기 팝업 윈도우
    @SuppressLint("InflateParams")
    fun changeIcon(
        itemHiddenFolderBinding: ItemMyHiddenFolderBinding,
        position: Int,
        folderIdx: Int
    ) {
        val size = activity?.windowManager?.currentWindowMetricsPointCompat()
        val width = ((size?.x ?: 0) * 0.8f).toInt()
        val height = ((size?.y ?: 0) * 0.6f).toInt()
        val inflater =
            activity?.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_window_change_icon, null)

        mPopupWindow = PopupWindow(popupView, width, height)
        mPopupWindow.animationStyle = 0
        mPopupWindow.isFocusable = true
        mPopupWindow.isOutsideTouchable = true
        mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        mPopupWindow.setOnDismissListener(PopupWindowDismissListener())
        binding.myHiddenFolderBackgroundView.visibility = View.VISIBLE

        // 데이터베이스로부터 아이콘 리스트 불러와 연결해주기
        iconList = database.iconDao().getIconList() as ArrayList
        iconRVAdapter = IconRVAdapter(iconList)
        popupView.findViewById<RecyclerView>(R.id.popup_window_change_icon_recycler_view).adapter =
            iconRVAdapter

        iconRVAdapter.setMyItemClickListener(object : IconRVAdapter.MyItemClickListener {
            // 아이콘을 선택했을 경우
            override fun onIconClick(
                itemIconBinding: ItemIconBinding,
                iconPosition: Int
            ) {
                // 선택한 아이콘으로 폴더 이미지 변경
                val selectedIcon = iconList[iconPosition]
                itemHiddenFolderBinding.itemHiddenFolderIv.setImageResource(selectedIcon.iconImage)

                val iconBitmap = BitmapFactory.decodeResource(resources, selectedIcon.iconImage)
                val baos = ByteArrayOutputStream()
                iconBitmap.compress(Bitmap.CompressFormat.PNG, 70, baos)

                val iconBitmapAsByte = baos.toByteArray()
                val iconBitmapAsString = Base64.encodeToString(iconBitmapAsByte, Base64.DEFAULT)

                // 폴더 아이콘 바꾸기
                folderService.changeFolderIcon(
                    this@MyHiddenFolderFragment,
                    userID,
                    folderIdx,
                    FolderList(folderIdx, hiddenFolderList[position].folderName, iconBitmapAsString)
                )
                mPopupWindow.dismiss()  // 팝업 윈도우 종료
            }
        })
    }

    inner class PopupWindowDismissListener() : PopupWindow.OnDismissListener {
        override fun onDismiss() {
            binding.myHiddenFolderBackgroundView.visibility = View.INVISIBLE
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = this.activity as MainActivity
    }

    override fun onDetach() {
        super.onDetach()
        mContext = null
    }

    // 숨김 폴더목록 가져오기 성공했을 때
    override fun onHiddenFolderListSuccess(hiddenFolderList: ArrayList<FolderList>) {
        Log.d(FRAG, "HIDDENFOLDER/onHiddenFolderListSuccess")
        this.hiddenFolderList.clear()
        this.hiddenFolderList = hiddenFolderList
        initRecyclerView()
    }

    // 숨김 폴더목록 가져오기 실패했을 때
    override fun onHiddenFolderListFailure(code: Int, message: String) {
        Log.d(FRAG, "HIDDENFOLDER/onHiddenFolderListFailure/code: $code, message: $message")

        // FixMe: 사용자에게 서버와의 네트워크가 불안정하다는 것을 알려주는 팝업창 같은 걸 띄우는 게 어떨까?
    }

    // folder API 성공했을 때
    override fun onFolderAPISuccess() {
        Log.d(FRAG, "HIDDENFOLDER/onFolderAPISuccess")
    }

    // folder API 실패했을 때
    override fun onFolderAPIFailure(code: Int, message: String) {
        Log.d(FRAG, "HIDDENFOLDER/onFolderAPIFailure/code: $code, messgae: $message")

        // FixMe: 사용자에게 서버와의 네트워크가 불안정하다는 것을 알려주는 팝업창 같은 걸 띄우는 게 어떨까?
    }
}
