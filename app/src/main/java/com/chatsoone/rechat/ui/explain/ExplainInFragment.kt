package com.chatsoone.rechat.ui.explain

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.chatsoone.rechat.databinding.FragmentExplainInBinding

class ExplainInFragment(private val imageResource: Int) : Fragment() {
    private lateinit var binding: FragmentExplainInBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentExplainInBinding.inflate(layoutInflater)
        binding.explainInIv.setImageResource(imageResource)
        return binding.root
    }
}