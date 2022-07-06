package com.chatsoone.rechat.ui.main.folder

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.chatsoone.rechat.ApplicationClass.Companion.FRAG
import com.chatsoone.rechat.ApplicationClass.Companion.currentWindowMetricsPointCompat
import com.chatsoone.rechat.R
import com.chatsoone.rechat.base.BaseFragment
import com.chatsoone.rechat.data.entity.Icon
import com.chatsoone.rechat.data.local.AppDatabase
import com.chatsoone.rechat.data.remote.FolderList
import com.chatsoone.rechat.data.remote.folder.FolderService
import com.chatsoone.rechat.databinding.FragmentMyFolderBinding
import com.chatsoone.rechat.databinding.ItemIconBinding
import com.chatsoone.rechat.databinding.ItemMyFolderBinding
import com.chatsoone.rechat.ui.adapter.IconRVAdapter
import com.chatsoone.rechat.ui.chat.FolderContentActivity
import com.chatsoone.rechat.ui.view.FolderAPIView
import com.chatsoone.rechat.ui.view.FolderListView
import com.chatsoone.rechat.ui.viewmodel.ChatTypeViewModel
import com.chatsoone.rechat.util.getID
import com.google.gson.Gson
import java.io.ByteArrayOutputStream

class MyFolderFragment : BaseFragment<FragmentMyFolderBinding>(FragmentMyFolderBinding::inflate),
    FolderListView, FolderAPIView {
    private lateinit var database: AppDatabase
    private lateinit var folderRVAdapter: MyFolderRVAdapter
    private lateinit var iconRVAdapter: IconRVAdapter
    private lateinit var mPopupWindow: PopupWindow
    private lateinit var folderService: FolderService
    private lateinit var itemMyFolderBinding: ItemMyFolderBinding

    private val userID = getID()
    private var iconList = ArrayList<Icon>()
    private var folderList = ArrayList<FolderList>()
    private val chatTypeViewModel by activityViewModels<ChatTypeViewModel>()

    override fun initAfterBinding() {
        database = AppDatabase.getInstance(requireContext())!!
        iconList = database.iconDao().getIconList() as ArrayList

        folderService = FolderService()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chatTypeViewModel.setMode(mode = 2)
    }

    override fun onResume() {
        super.onResume()
        initFolder()
    }

    // 전체 폴더 목록을 가져온다. (숨긴 폴더 제외)
    private fun initFolder() {
        folderRVAdapter = MyFolderRVAdapter(this@MyFolderFragment, requireContext())
        folderService.getFolderList(this, userID)
    }

    // RecyclerView 초기화
    private fun initRecyclerView() {
        folderRVAdapter.addFolderList(this.folderList)
        binding.myFolderFolderListRecyclerView.adapter = folderRVAdapter

        // click listener
        folderRVAdapter.setMyItemClickListener(object : MyFolderRVAdapter.MyItemClickListener {
            // 폴더 이름 롱클릭 시 폴더 이름 변경
            override fun onFolderNameLongClick(
                binding: ItemMyFolderBinding,
                position: Int,
                folderIdx: Int
            ) {
                itemMyFolderBinding = binding
                changeFolderName(itemMyFolderBinding, position, folderIdx)
            }

            // 폴더 아이콘 클릭 시 해당 폴더로 이동
            override fun onFolderClick(view: View, position: Int) {
                val selectedFolder = folderRVAdapter.getSelectedFolder(position)

                // folder 삽입 시 status 변경! null X
                val gson = Gson()
                val folderJson = gson.toJson(selectedFolder)

                // 폴더 정보를 보내기
                val intent = Intent(activity, FolderContentActivity::class.java)
                intent.putExtra("folderData", folderJson)
                startActivity(intent)
            }

            // 폴더 아이콘 롱클릭 시 팝업 메뉴 뜨도록
            override fun onFolderLongClick(mPopupMenu: PopupMenu) {
                mPopupMenu.show()
            }

            // 폴더 삭제하기
            override fun onRemoveFolder(folderIdx: Int) {
                folderService.deleteFolder(this@MyFolderFragment, userID, folderIdx)
            }

            // 폴더 숨기기
            @SuppressLint("NotifyDataSetChanged")
            override fun onHideFolder(folderIdx: Int) {
                folderService.hideFolder(this@MyFolderFragment, userID, folderIdx)
                folderRVAdapter.notifyDataSetChanged()
            }
        })
    }

    // 이름 바꾸기 팝업 윈도우를 띄워서 폴더 이름을 변경할 수 있도록 해준다.
    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    fun changeFolderName(itemBinding: ItemMyFolderBinding, position: Int, folderIdx: Int) {

        val size = activity?.windowManager?.currentWindowMetricsPointCompat()
        val width = ((size?.x ?: 0) * 0.8f).toInt()
        val height = ((size?.y ?: 0) * 0.4f).toInt()

        // 이름 바꾸기 팝업 윈도우
        val inflater =
            activity?.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_window_change_name, null)
        mPopupWindow = PopupWindow(popupView, width, WindowManager.LayoutParams.WRAP_CONTENT)

        mPopupWindow.animationStyle = 0
        mPopupWindow.animationStyle = R.style.Animation
        mPopupWindow.isFocusable = true
        mPopupWindow.isOutsideTouchable = true
        mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        binding.myFolderBackgroundView.visibility = View.VISIBLE
        mPopupWindow.setOnDismissListener(PopupWindowDismissListener())

        // 기존 폴더 이름을 팝업 윈도우의 EditText에 넘겨준다.
        var text: String = itemBinding.itemMyFolderTv.text.toString()
        mPopupWindow.contentView.findViewById<EditText>(R.id.popup_window_change_name_et)
            .setText(text)

        // 입력 완료했을 때 누르는 버튼
        mPopupWindow.contentView.findViewById<AppCompatButton>(R.id.popup_window_change_name_button)
            .setOnClickListener {
                text =
                    mPopupWindow.contentView.findViewById<EditText>(R.id.popup_window_change_name_et).text.toString()
                itemBinding.itemMyFolderTv.text = text

                val folderImg = folderList[position].folderImg

                folderService.changeFolderName(
                    this,
                    userID,
                    folderIdx,
                    FolderList(folderIdx, text, folderImg)
                )

                mPopupWindow.dismiss()

                binding.myFolderBackgroundView.visibility = View.INVISIBLE
            }
    }

    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    fun changeIcon(itemBinding: ItemMyFolderBinding, position: Int, folderIdx: Int) {
        // 팝업 윈도우 사이즈를 잘못 맞추면 아이템들이 안 뜨므로 하드 코딩으로 사이즈 조정해주기
        // 아이콘 16개 (기본)
        val size = activity?.windowManager?.currentWindowMetricsPointCompat()
        val width = ((size?.x ?: 0) * 0.8f).toInt()
        val height = ((size?.y ?: 0) * 0.6f).toInt()

        // 아이콘 바꾸기 팝업 윈도우
        val inflater =
            activity?.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_window_change_icon, null)
        mPopupWindow = PopupWindow(popupView, width, height)

        mPopupWindow.animationStyle = 0
        mPopupWindow.animationStyle = R.style.Animation
        mPopupWindow.isFocusable = true
        mPopupWindow.isOutsideTouchable = true
        mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        binding.myFolderBackgroundView.visibility = View.VISIBLE
        mPopupWindow.setOnDismissListener(PopupWindowDismissListener())

        // RecyclerView 초기화
        iconRVAdapter = IconRVAdapter(iconList)
        popupView.findViewById<RecyclerView>(R.id.popup_window_change_icon_recycler_view).adapter =
            iconRVAdapter

        iconRVAdapter.setMyItemClickListener(object : IconRVAdapter.MyItemClickListener {
            // 아이콘을 하나 선택했을 경우
            override fun onIconClick(
                itemIconBinding: ItemIconBinding,
                iconPosition: Int
            ) {//해당 파라미터는 아이콘 DB!
                // 선택한 아이콘으로 폴더 이미지 변경
                val selectedIcon = iconList[iconPosition]
                itemBinding.itemMyFolderIv.setImageResource(selectedIcon.iconImage)

                val value = TypedValue()
                resources.getValue(selectedIcon.iconImage, value, true)
                if (value.string != null) {
                    folderService.changeFolderName(
                        this@MyFolderFragment,
                        userID,
                        folderIdx,
                        FolderList(
                            folderIdx,
                            folderList[position].folderName,
                            value.string.toString()
                        )
                    )
                }

                mPopupWindow.dismiss()
            }
        })
    }

    // 새폴더 이름 설정
    @SuppressLint("InflateParams")
    private fun setFolderName() {
        val size = activity?.windowManager?.currentWindowMetricsPointCompat()
        val width = ((size?.x ?: 0) * 0.8f).toInt()
        val height = ((size?.y ?: 0) * 0.4f).toInt()

        val inflater =
            activity?.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_window_set_folder_name, null)
        mPopupWindow = PopupWindow(popupView, width, WindowManager.LayoutParams.WRAP_CONTENT)

        mPopupWindow.animationStyle = 0
        mPopupWindow.animationStyle = R.style.Animation
        mPopupWindow.isFocusable = true
        mPopupWindow.isOutsideTouchable = true
        mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        binding.myFolderBackgroundView.visibility = View.VISIBLE
        mPopupWindow.setOnDismissListener(PopupWindowDismissListener())

        // 입력 완료했을 때 누르는 버튼
        mPopupWindow.contentView.findViewById<AppCompatButton>(R.id.popup_window_set_name_button)
            .setOnClickListener {
                // 작성한 폴더 이름을 반영한 새폴더를 만들어준다.
                val name =
                    mPopupWindow.contentView.findViewById<EditText>(R.id.popup_window_set_name_et).text.toString()

                // 팝업 윈도우 종료
                mPopupWindow.dismiss()

                // 작성한 폴더 이름을 setFolderIcon 함수로 넘겨준다.
                setFolderIcon(name)
            }
    }

    // 새폴더 아이콘 설정
    @SuppressLint("InflateParams")
    private fun setFolderIcon(name: String) {
        // 팝업 윈도우 사이즈를 잘못 맞추면 아이템들이 안 뜨므로 하드 코딩으로 사이즈 조정해주기
        // 아이콘 16개 (기본)
        val size = activity?.windowManager?.currentWindowMetricsPointCompat()
        val width = ((size?.x ?: 0) * 0.8f).toInt()
        val height = ((size?.y ?: 0) * 0.6f).toInt()

        // 아이콘 바꾸기 팝업 윈도우
        val inflater =
            activity?.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_window_change_icon, null)
        mPopupWindow = PopupWindow(popupView, width, height)

        mPopupWindow.animationStyle = 0        // 애니메이션 설정 (-1: 설정 안 함, 0: 설정)
        mPopupWindow.animationStyle = R.style.Animation
        mPopupWindow.isFocusable = true
        mPopupWindow.isOutsideTouchable = true
        mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        binding.myFolderBackgroundView.visibility = View.VISIBLE    // 뒷배경 흐려지게
        mPopupWindow.setOnDismissListener(PopupWindowDismissListener())

        // RecyclerView 초기화
        iconRVAdapter = IconRVAdapter(iconList)
        popupView.findViewById<RecyclerView>(R.id.popup_window_change_icon_recycler_view).adapter =
            iconRVAdapter

        iconRVAdapter.setMyItemClickListener(object : IconRVAdapter.MyItemClickListener {
            // 아이콘을 하나 선택했을 경우
            override fun onIconClick(itemIconBinding: ItemIconBinding, iconPosition: Int) {
                val selectedIcon = iconList[iconPosition]

                val iconBitmap = BitmapFactory.decodeResource(resources, selectedIcon.iconImage)
                val baos = ByteArrayOutputStream()
                iconBitmap.compress(Bitmap.CompressFormat.PNG, 70, baos)

                val iconBitmapAsByte = baos.toByteArray()
                val iconBitmapAsString = Base64.encodeToString(iconBitmapAsByte, Base64.DEFAULT)

                val newFolderPosition = folderList.size - 1
                val value = TypedValue()
                resources.getValue(selectedIcon.iconImage, value, true)
                if (value.string != null) {
                    folderService.changeFolderName(
                        this@MyFolderFragment,
                        userID,
                        folderList[newFolderPosition].folderIdx,
                        FolderList(
                            folderList[newFolderPosition].folderIdx,
                            name,
                            value.string.toString()
                        )
                    )
                    folderService.changeFolderIcon(
                        this@MyFolderFragment,
                        userID,
                        folderList[newFolderPosition].folderIdx,
                        FolderList(
                            folderList[newFolderPosition].folderIdx,
                            name,
                            value.string.toString()
                        )
                    )
                }

                mPopupWindow.dismiss()
            }
        })
    }

    inner class PopupWindowDismissListener() : PopupWindow.OnDismissListener {
        override fun onDismiss() {
            binding.myFolderBackgroundView.visibility = View.INVISIBLE
        }
    }

    override fun onFolderAPISuccess() {
        Log.d(FRAG, "MYFOLDER/onFolderAPISuccess")
        initFolder()
    }

    override fun onFolderAPIFailure(code: Int, message: String) {
        Log.d(FRAG, "MYFOLDER/onFolderAPIFailure/code: $code, message: $message")
    }

    override fun onFolderListSuccess(folderList: ArrayList<FolderList>) {
        Log.d(FRAG, "MYFOLDER/onFolderListSuccess/folderList: $folderList")
        this.folderList.clear()
        this.folderList.addAll(folderList)
        initRecyclerView()
    }

    override fun onFolderListFailure(code: Int, message: String) {
        Log.d(FRAG, "MYFOLDER/onFolderListFailure/code: $code, message: $message")
        initRecyclerView()
    }
}
