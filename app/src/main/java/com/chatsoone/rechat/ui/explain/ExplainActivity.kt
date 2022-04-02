package com.chatsoone.rechat.ui.explain

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.chatsoone.rechat.ApplicationClass.Companion.mSharedPreferences
import com.chatsoone.rechat.R
import com.chatsoone.rechat.databinding.ActivityExplainBinding
import com.chatsoone.rechat.ui.permission.PermissionActivity
import com.chatsoone.rechat.util.permissionGrantred

class ExplainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityExplainBinding
    private var isExplain = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExplainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initFragment()
        initClickListener()
    }

    override fun onStart() {
        super.onStart()

        if (getSharedPreferences("explain", MODE_PRIVATE).getInt("explain_from_menu", 0) == 1) {
            binding.explainCheckbox.visibility = View.INVISIBLE
            return
        }

        if (getSharedPreferences("explain", MODE_PRIVATE)
                .getInt("explain_from_menu", 0) == 0 && getSharedPreferences(
                "explain",
                MODE_PRIVATE
            ).getInt("explain", 0) == 2
        ) {
            finish()
        }
    }

    private fun initFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.explain_fl, ExplainFragment())
            .commitAllowingStateLoss()
    }

    private fun initClickListener() {
        mSharedPreferences = getSharedPreferences("explain", MODE_PRIVATE)

        // x 버튼 클릭시
        binding.explainExitBtnIv.setOnClickListener {
            close()
        }
    }

    override fun onBackPressed() {
        close()
    }

    private fun close() {
        if (getSharedPreferences("explain", MODE_PRIVATE).getInt("explain_from_menu", 0) != 1) {
            isExplain = if (binding.explainCheckbox.isChecked) 2 else 1

            val editor = mSharedPreferences.edit()
            editor.putInt("explain", isExplain)
            editor.apply()

            if (!permissionGrantred(this)) {
                val intent = Intent(this, PermissionActivity::class.java)
                startActivity(intent)
            } else {
                finish()
            }
        } else {
            mSharedPreferences = getSharedPreferences("explain", MODE_PRIVATE)
            val editor = mSharedPreferences.edit()
            editor.putInt("explain_from_menu", 0)
            editor.apply()
            finish()
        }
    }
}