package com.dailypay.app.data.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

// IMPORTANT: Do NOT replace these with fake placeholders. Keep your project's actual URL and anon key.
val supabase = createSupabaseClient(
    supabaseUrl = "https://your-project-id.supabase.co", // <-- YOUR REAL SUPABASE URL
    supabaseKey = "your-actual-anon-key"                 // <-- YOUR REAL ANON KEY
) {
    install(Postgrest) {
        defaultSchema = "public"
    }
    install(Auth)
    install(Storage)
}
