package com.deerbrain.basesdcard.utilities

import android.app.Application
import android.content.Context

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        //realm init


        context = applicationContext

        //FirebaseApp.initializeApp(context)
    }

    companion object{
        lateinit var context: Context
    }

}