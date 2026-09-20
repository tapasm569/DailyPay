package com.dailypay.app.data.remote

import com.dailypay.app.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage

object SupabaseClientProvider {

    /**
     * Singleton Supabase client configured with Postgrest (Database),
     * Storage (KYC/Avatars), and Realtime modules.
     */
    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Postgrest)
            install(Storage)
            install(Realtime)
        }
    }

    /**
     * Quick direct accessors for database and storage operations
     */
    val db: Postgrest
        get() = client.postgrest

    val storage: Storage
        get() = client.storage
}
