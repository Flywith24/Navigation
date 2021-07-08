package com.flywith24.navigation.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.flywith24.navigation.R
import com.flywith24.navigation.printLog

class HomeChildFragment : Fragment(R.layout.fragment_child) {

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.viewGroup).background =
            ContextCompat.getDrawable(requireContext(), R.color.purple_200)
        view.findViewById<TextView>(R.id.title).text = "Home \nChild Fragment"
        printLog(HomeChildFragment::class, savedInstanceState)
    }
}