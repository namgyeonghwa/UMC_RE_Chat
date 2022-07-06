package com.chatsoone.rechat.ui.explain

import androidx.viewpager2.widget.ViewPager2
import com.chatsoone.rechat.R
import com.chatsoone.rechat.base.BaseFragment
import com.chatsoone.rechat.databinding.FragmentExplainBinding

class ExplainFragment : BaseFragment<FragmentExplainBinding>(FragmentExplainBinding::inflate) {

    override fun initAfterBinding() {
        initViewPager()
    }

    private fun initViewPager() {
        val explainAdapter = ExplainVPAdapter(this)

        // addFragment. 설명 이미지 추가하기
        explainAdapter.addFragment(ExplainInFragment(R.drawable.expain_01))
        explainAdapter.addFragment(ExplainInFragment(R.drawable.expain_06))
        explainAdapter.addFragment(ExplainInFragment(R.drawable.expain_02))
        explainAdapter.addFragment(ExplainInFragment(R.drawable.expain_03))
        explainAdapter.addFragment(ExplainInFragment(R.drawable.expain_04))
        explainAdapter.addFragment(ExplainInFragment(R.drawable.expain_05))
        explainAdapter.addFragment(ExplainInFragment(R.drawable.expain_07))
        explainAdapter.addFragment(ExplainInFragment(R.drawable.explain_08))
        explainAdapter.addFragment(ExplainInFragment(R.drawable.expain_09))

        binding.explainVp.adapter = explainAdapter
        binding.explainVp.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.explainIndicator.setViewPager2(binding.explainVp)
    }
}
