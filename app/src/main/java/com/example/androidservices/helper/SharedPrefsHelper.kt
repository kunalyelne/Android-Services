package com.example.androidservices.helper

import android.content.Context
import android.content.SharedPreferences

const val PREFS_NAME = "AndroidServicesSharedPrefs"

class SharedPrefs(context: Context) {

  private val sharedPreferences: SharedPreferences by lazy {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  }
}