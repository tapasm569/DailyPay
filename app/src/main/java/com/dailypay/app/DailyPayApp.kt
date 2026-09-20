package com.dailypay.app

import android.app.Application
import android.content.Context
import com.dailypay.app.data.remote.SupabaseClientProvider

class DailyPayApp : Application() {

    companion object {
        lateinit var appContext: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext

        // Eagerly initialize the Supabase client on startup
        SupabaseClientProvider.client
    }
}
