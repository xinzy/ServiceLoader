package com.xinzy.lib.service.serviceloader.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.xinzy.lib.service.serviceloader.ApiDescriptor

class RemoteService : Service() {

    private lateinit var client: CalculateClient

    override fun onCreate() {
        super.onCreate()
        Log.d("RemoteService", "onCreate: ")

        client = CalculateClient()
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d("RemoteService", "onBind: ")
        return ApiDescriptor(client, 1)
    }
}