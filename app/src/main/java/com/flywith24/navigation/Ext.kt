package com.flywith24.navigation

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import kotlin.reflect.KClass

fun printLog(fragment: KClass<out Fragment>, savedInstanceState: Bundle?) {
    Log.i(TAG, "当前 Fragment: ${fragment.java.simpleName} savedInstanceState = null ? ${savedInstanceState == null}")
}

fun Any.instanceId():String = Integer.toHexString(this.hashCode())

private const val TAG = "Navigation"