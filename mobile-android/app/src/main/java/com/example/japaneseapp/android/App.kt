package com.example.japaneseapp.android

import android.app.Application
import com.example.japaneseapp.android.data.token.TokenStore

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        TokenStore.init(this)
    }
}
