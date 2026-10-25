package com.dailypay.app

import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp

class DailyPayApp : Application() {

    companion object {
        lateinit var appContext: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        FirebaseApp.initializeApp(this)
    }
}
