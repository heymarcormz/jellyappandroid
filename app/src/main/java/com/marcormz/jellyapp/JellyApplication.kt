package com.marcormz.jellyapp

import android.app.Application
import com.marcormz.jellyapp.analytics.FirebaseAppsFlyerAnalytics
import com.marcormz.jellyapp.data.HttpStockBackend
import com.marcormz.jellyapp.data.StockBackend

class JellyApplication : Application() {
    lateinit var analytics: FirebaseAppsFlyerAnalytics
        private set

    val stockBackend: StockBackend = HttpStockBackend()

    override fun onCreate() {
        super.onCreate()
        analytics = FirebaseAppsFlyerAnalytics(this).also { it.initialize() }
    }
}
