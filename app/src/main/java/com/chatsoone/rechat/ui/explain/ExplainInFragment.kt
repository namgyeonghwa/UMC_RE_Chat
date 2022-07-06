package com.chatsoone.rechat.ui.explain

import com.chatsoone.rechat.base.BaseFragment
import com.chatsoone.rechat.databinding.FragmentExplainInBinding

class ExplainInFragment(private val imageResource: Int) :
    BaseFragment<FragmentExplainInBinding>(FragmentExplainInBinding::inflate) {

    override fun initAfterBinding() {
        binding.explainInIv.setImageResource(imageResource)
    }
}
