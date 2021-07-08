package com.flywith24.navigation.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.flywith24.navigation.R
import com.flywith24.navigation.instanceId
import com.flywith24.navigation.printLog

class HomeFragment : Fragment(R.layout.fragment_main) {

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.title).text = "This is home Fragment\n${this.instanceId()}"
        view.findViewById<View>(R.id.viewGroup).setOnClickListener {
            findNavController().navigate(R.id.action_home_to_homeChild)
        }
        printLog(HomeFragment::class, savedInstanceState)
    }
}