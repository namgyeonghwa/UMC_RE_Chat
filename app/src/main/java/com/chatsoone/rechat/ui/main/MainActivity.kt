package com.chatsoone.rechat.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.SparseBooleanArray
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.chatsoone.rechat.ApplicationClass
import com.chatsoone.rechat.ApplicationClass.Companion.ACT
import com.chatsoone.rechat.ApplicationClass.Companion.currentWindowMetricsPointCompat
import com.chatsoone.rechat.NotificationListener
import com.chatsoone.rechat.R
import com.chatsoone.rechat.data.entity.ChatList
import com.chatsoone.rechat.data.entity.Folder
import com.chatsoone.rechat.data.entity.Icon
import com.chatsoone.rechat.data.local.AppDatabase
import com.chatsoone.rechat.databinding.ActivityMainBinding
import com.chatsoone.rechat.databinding.ItemFolderListBinding
import com.chatsoone.rechat.ui.ChatViewModel
import com.chatsoone.rechat.ui.FolderListRVAdapter
import com.chatsoone.rechat.ui.ItemViewModel
import com.chatsoone.rechat.ui.LockViewModel
import com.chatsoone.rechat.ui.explain.ExplainActivity
import com.chatsoone.rechat.ui.main.blocklist.BlockListFragment
import com.chatsoone.rechat.ui.main.folder.MyFolderFragment
import com.chatsoone.rechat.ui.main.hiddenfolder.MyHiddenFolderFragment
import com.chatsoone.rechat.ui.main.home.HomeFragment
import com.chatsoone.rechat.ui.pattern.CreatePatternActivity
import com.chatsoone.rechat.ui.pattern.InputPatternActivity
import com.chatsoone.rechat.ui.setting.PrivacyInfoActivity
import com.chatsoone.rechat.util.getID
import com.chatsoone.rechat.util.permissionGrantred
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.navigation.NavigationView

class MainActivity : NavigationView.OnNavigationItemSelectedListener,
    AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: AppDatabase
    private lateinit var selectedItemList: ArrayList<ChatList>  // chat index
    private lateinit var folderListRVAdapter: FolderListRVAdapter
    private lateinit var mPopupWindow: PopupWindow

    private var userID = getID()
    private var iconList = ArrayList<Icon>()
    private var folderList = ArrayList<Folder>()
    private var permission: Boolean = true

    private val chatViewModel: ChatViewModel by viewModels<ChatViewModel>()
    private val selectedItemViewModel: ItemViewModel by viewModels<ItemViewModel>()
    private val lockViewModel: LockViewModel by viewModels()

    // 광고
    lateinit var mAdview: AdView
    lateinit var adRequest: AdRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getInstance(this)!!
        initIcon()
        initFolder()
        initChatViewModel()
        initSelectedItemViewModel()
        lockViewModel.setMode(0)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()

        initAds()
        initHiddenFolder()
        initBottomNavigationView()
        initDrawerLayout()
        initClickListener()
    }

    // 아이콘 초기화
    private fun initIcon() {
        iconList = database.iconDao().getIconList() as ArrayList

        if (iconList.isEmpty()) {
            // 아이콘 목록 추가
            // database.iconDao().insert(Icon())
            // iconList = database.iconDao().getIconList() as ArrayList
        }
    }

    // 폴더 초기화
    private fun initFolder() {
        // 폴더 초기 세팅 부분 생략
        database.folderDao().getFolderList(userID).observe(this) {
            Log.d(ApplicationClass.ACT, "MAIN/folderList: $folderList")
            folderList = it as ArrayList<Folder>
        }
    }

    private fun initChatViewModel() {
        // observe mode
        chatViewModel.mode.observe(this) {
            if (it == 0) setDefaultMode()
            else setChooseMode()
            Log.d(ACT, "MAIN/mode: $it")
        }
    }

    private fun setDefaultMode() {
        binding.mainLayout.mainBnvCenterDefaultIv.visibility = View.VISIBLE
        binding.mainLayout.mainBnvCenterChooseIv.visibility = View.GONE
    }

    private fun setChooseMode() {
        binding.mainLayout.mainBnvCenterDefaultIv.visibility = View.GONE
        binding.mainLayout.mainBnvCenterChooseIv.visibility = View.VISIBLE
    }

    private fun initSelectedItemViewModel() {
        selectedItemViewModel.list.observe(this) {
            selectedItemList = it
            Log.d(ACT, "MAIN/selectedItemList: $selectedItemList")
        }
    }

    // 숨긴 폴더 초기화
    private fun initHiddenFolder() {
        val spf = getSharedPreferences("lock_correct", 0)

        if (spf.getInt("correct", 0) == 1) replaceFragment(MyHiddenFolderFragment())
        else if (spf.getInt("correct", 0) == -1) replaceFragment(MyFolderFragment())
        else replaceFragment(BlockListFragment())
    }

    // 프래그먼트 교체
    private fun replaceFragment(fragment: Fragment) {
        this.supportFragmentManager.beginTransaction()
            .replace(R.id.main_frame_layout, fragment).commitAllowingStateLoss()
    }

    // 광고 초기화
    private fun initAds() {
        MobileAds.initialize(this)
        val headerView = binding.mainNavigationView.getHeaderView(0)
        mAdview = headerView.findViewById<AdView>(R.id.adViews)
        adRequest = AdRequest.Builder().build()
        mAdview.loadAd(adRequest)
    }

    private fun initBottomNavigationView() {
        Log.d(ACT, "MAIN/initBottomNavigationView")

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.main_frame_layout, HomeFragment())
            .commitAllowingStateLoss()

        binding.mainLayout.mainBnv.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.main_bnv_home -> {
                    // 전체 채팅
                    replaceFragment(HomeFragment())
                    return@setOnItemSelectedListener true
                }

                R.id.main_bnv_block_list -> {
                    // 차단 목록
                    replaceFragment(BlockListFragment())
                    return@setOnItemSelectedListener true
                }

                R.id.main_bnv_folder -> {
                    // 보관함
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frame_layout, MyFolderFragment())
                        .commitAllowingStateLoss()

                    return@setOnItemSelectedListener true
                }

                R.id.main_bnv_hidden_folder -> {
                    // 숨긴 보관함
                    val lockSPF = getSharedPreferences("lock", 0)
                    val pattern = lockSPF.getString("pattern", "0")

                    // 패턴 모드 설정
                    // 0: 숨긴 폴더 목록을 확인하기 위한 입력 모드
                    // 1: 메인 화면의 설정창 -> 변경 모드
                    // 2: 폴더 화면의 설정창 -> 변경 모드
                    // 3: 메인 화면 폴더 리스트에서 숨김 폴더 클릭 시
                    val modeSPF = getSharedPreferences("mode", 0)
                    val editor = modeSPF.edit()

                    // 여기서는 0번 모드
                    editor.putInt("mode", 0)
                    editor.apply()

                    if (pattern.equals("0")) {   // 패턴이 설정되어 있지 않은 경우 패턴 설정 페이지로
                        Toast.makeText(this, "패턴이 설정되어 있지 않습니다.\n패턴을 설정해주세요.", Toast.LENGTH_SHORT)
                            .show()
                        startActivity(Intent(this@MainActivity, CreatePatternActivity::class.java))
                    } else {
                        startActivity(Intent(this@MainActivity, InputPatternActivity::class.java))
                    }

                    // 올바른 패턴인지 확인
                    // 1: 올바른 패턴
                    // 2: 올바르지 않은 패턴
                    initHiddenFolder()
                    return@setOnItemSelectedListener true
                }
            }
            false
        }
    }

    private fun initDrawerLayout() {
        binding.mainNavigationView.setNavigationItemSelectedListener(this)
        val menuItem = binding.mainNavigationView.menu.findItem(R.id.navi_setting_alarm_item)
        val drawerSwitch =
            menuItem.actionView.findViewById(R.id.main_drawer_alarm_switch) as SwitchCompat

        // 알림 권한 허용 여부에 따라 스위치 초기 상태 지정
        if (permissionGrantred(this)) {
            // 알림 권한이 허용되어 있는 경우
            drawerSwitch.toggle()
            drawerSwitch.isChecked = true
            permission = true
        } else {
            // 알림 권한이 허용되어 있지 않은 경우
            drawerSwitch.isChecked = false
            permission = false
        }

        drawerSwitch.setOnClickListener {
            if (drawerSwitch.isChecked) {
                // 알림 권한을 허용했을 때
                permission = true
                Log.d(ACT, "MAIN/drawerSwitch.isChecked == true")
                startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))

                if (permissionGrantred(this)) {
                    Toast.makeText(this, "알림 권한을 허용합니다.", Toast.LENGTH_SHORT).show()
                    Log.d(ACT, "MAIN/inPermission")
                    startForegroundService(Intent(this, NotificationListener::class.java))
                }
            } else {
                // 알림 권한을 허용하지 않았을 때
                permission = false
                Log.d(ACT, "MAIN/drawerSwitch.isChecked == false")
                startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                if (!permissionGrantred(this)) {
                    stopService(Intent(this, NotificationListener::class.java))
                    Toast.makeText(this, "알림 권한을 허용하지 않습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initClickListener() {
        // 설정 메뉴창을 여는 메뉴 아이콘 클릭시 설정 메뉴창 열리도록
        binding.mainLayout.mainSettingMenuIv.setOnClickListener {
            if (!binding.mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                // 설정 메뉴창이 닫혀있을 때
                mAdview.loadAd(adRequest)
                binding.mainDrawerLayout.openDrawer(GravityCompat.START)
            }
        }

        // 설정 메뉴창에 있는 메뉴 아이콘 클릭했을 때 설정 메뉴 닫히도록
        val headerView = binding.mainNavigationView.getHeaderView(0)
        headerView.findViewById<ImageView>(R.id.main_drawer_setting_menu_iv).setOnClickListener {
            binding.mainDrawerLayout.closeDrawer(GravityCompat.START)
        }

        // 선택 모드일 때
        binding.mainLayout.mainBnvCenterChooseIv.setOnClickListener {
            if(selectedItemList.isNotEmpty()) {
                openPopupWindow()
            }
        }
    }

    // 설정 메뉴 창의 네비게이션 드로어의 아이템들에 대한 이벤트를 처리
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // 알림 설정
            R.id.navi_setting_alarm_item -> {
                Toast.makeText(this, "알림 설정", Toast.LENGTH_SHORT).show()
            }

            // 패턴 변경하기
            R.id.navi_setting_pattern_item -> {
                val lockSPF = getSharedPreferences("lock", 0)
                val pattern = lockSPF.getString("pattern", "0")

                // 앱 삭제할때 같이 DB 저장 X
                // 패턴 모드 설정
                // 0: 숨긴 폴더 목록을 확인하기 위한 입력 모드
                // 1: 메인 화면의 설정창 -> 변경 모드
                // 2: 폴더 화면의 설정창 -> 변경 모드
                // 3: 메인 화면 폴더로 보내기 -> 숨김 폴더 눌렀을 경우
                val modeSPF = getSharedPreferences("mode", 0)
                val editor = modeSPF.edit()
                editor.putInt("mode", 1)
                editor.apply()

                if (pattern.equals("0")) {   // 패턴이 설정되어 있지 않은 경우 패턴 설정 페이지로
                    val intent = Intent(this@MainActivity, CreatePatternActivity::class.java)
                    startActivity(intent)
                } else {    // 패턴이 설정되어 있는 경우 입력 페이지로 (보안을 위해)
                    val intent = Intent(this@MainActivity, InputPatternActivity::class.java)
                    startActivity(intent)
                }
            }
            // 사용 방법 도움말
            R.id.navi_setting_helper_item -> {
                ApplicationClass.mSharedPreferences = getSharedPreferences("explain", MODE_PRIVATE)
                val editor = ApplicationClass.mSharedPreferences.edit()
                editor.putInt("explain_from_menu", 1)
                editor.apply()

                val intent = Intent(this, ExplainActivity::class.java)
                startActivity(intent)
            }

            // 개인정보 처리방침
            R.id.navi_setting_privacy_item -> {
                val intent = Intent(this, PrivacyInfoActivity::class.java)
                startActivity(intent)
            }
        }
        return false
    }

    // 폴더로 보내기 팝업 윈도우
    @SuppressLint("InflateParams")
    private fun openPopupWindow() {
        val size = windowManager.currentWindowMetricsPointCompat()
        val width = (size.x * 0.8f).toInt()
        val height = (size.y * 0.4f).toInt()

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_window_to_folder, null)

        mPopupWindow = PopupWindow(popupView, width, height)
        mPopupWindow.animationStyle = R.style.Animation
        mPopupWindow.isFocusable = true
        mPopupWindow.isOutsideTouchable = true
        mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        mPopupWindow.setOnDismissListener(PopupWindowDismissListener())
        binding.mainLayout.mainBgV.visibility = View.VISIBLE

        // 폴더 목록 recycler view 초기화
        initFolderListRecyclerView(popupView)
    }

    // 폴더/보관함 보여주는 recycler view 초기화
    private fun initFolderListRecyclerView(popupView: View) {
        folderListRVAdapter = FolderListRVAdapter(this)

        val folderListRV =
            popupView.findViewById<RecyclerView>(R.id.popup_window_to_folder_menu_recycler_view)

        folderListRV.adapter = folderListRVAdapter
        folderListRVAdapter.setMyItemClickListener(object :
            FolderListRVAdapter.MyItemClickListener {
            override fun onFolderClick(itemBinding: ItemFolderListBinding, itemPosition: Int) {
                // 이동하고 싶은 폴더/보관함 클릭했을 때 해당 폴더/보관함으로 채팅 이동
                val selectedFolder = folderList[itemPosition]

                // 숨긴 보관함 같은 경우
                if (selectedFolder.status == ApplicationClass.HIDDEN) {
                    val lockSPF = getSharedPreferences("lock", 0)
                    val pattern = lockSPF.getString("pattern", "0")

                    // 패턴 모드 확인
                    // 0: 숨긴 폴더 목록을 확인하기 위한 입력 모드
                    // 1: 메인 화면의 설정창 -> 변경 모드
                    // 2: 폴더 화면의 설정창 -> 변경 모드
                    // 3: 메인 화면 폴더로 보내기 -> 숨김 폴더 눌렀을 경우
                    val modeSPF = getSharedPreferences("mode", 0)
                    val editor = modeSPF.edit()

                    // 여기서는 3번 모드
                    editor.putInt("mode", 3)
                    editor.apply()

                    if (pattern.equals("0")) {
                        // 패턴이 설정되어 있지 않은 경우 패턴 설정 페이지로
                        val intent =
                            Intent(this@MainActivity, CreatePatternActivity::class.java)
                        startActivity(intent)
                    } else {
                        // 패턴이 설정되어 있는 경우 입력 페이지로 (보안을 위해)
                        val intent = Intent(this@MainActivity, InputPatternActivity::class.java)
                        startActivity(intent)
                    }
                }

                Log.d(ACT, "MAIN/selectedChatList: $selectedItemList")

                val folderIdx = folderList[itemPosition].idx

                // 갠톡 이동: folderIdx, otherUserIdx
                // 단톡 이동: folderIdx, userIdx, groupName
                for (i in selectedItemList) {
                    if (i.groupName != "null") database.folderContentDao()
                        .insertOrgChat(i.chatIdx, folderIdx, userID)
                    else database.folderContentDao().insertOtOChat(folderIdx, i.chatIdx)
                }

                // 팝업 윈도우 종료
                mPopupWindow.dismiss()
            }
        })
    }

    // 드로어가 나와있을 때 뒤로 가기 버튼을 한 경우 뒤로 가기 버튼에 대한 이벤트를 처리
    override fun onBackPressed() {
        if (binding.mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.mainDrawerLayout.closeDrawers()
        } else if (chatViewModel.mode.value == 1) {
            chatViewModel.setMode(mode = 0)
        } else {
            super.onBackPressed()
        }
    }

    // 팝업창 닫을 때
    inner class PopupWindowDismissListener() : PopupWindow.OnDismissListener {
        override fun onDismiss() {
            chatViewModel.setMode(0)    // 혹은 바로 setDefaultMode() 가능
            binding.mainLayout.mainBgV.visibility = View.INVISIBLE
        }
    }
}