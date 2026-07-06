package com.foodmaster.app

import android.app.Application
import com.foodmaster.app.di.AppContainer
import com.foodmaster.app.di.DefaultAppContainer

class FoodMasterApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
