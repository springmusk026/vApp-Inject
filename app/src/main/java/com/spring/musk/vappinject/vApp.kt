package com.spring.musk.vappinject

import android.app.Application
import android.content.Context
import com.fvbox.lib.FCore

class vApp : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        FCore.get().init(this,true)
    }

    override fun onCreate() {
        super.onCreate()
        if(FCore.isClient()) {
            return
        }

        FCore.get().setHidePath(true)
        FCore.get().setHideRoot(true)
        FCore.get().setHideVPN(true)

    }
}