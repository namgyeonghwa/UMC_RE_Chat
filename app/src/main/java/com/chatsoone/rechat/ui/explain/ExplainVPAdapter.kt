package com.chatsoone.rechat.ui.explain

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ExplainVPAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    private val fragmentList: ArrayList<Fragment> = ArrayList()

    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment = fragmentList[position]

    fun addFragment(fragment: Fragment) {
        // 외부에서 직접 리스트를 접근하지 못하기 때문에 함수 사용
        fragmentList.add(fragment)

        // 추가된 곳의 인덱스를 뷰페이저에게 알려주는 함수
        notifyItemInserted(fragmentList.size - 1)
    }
}
