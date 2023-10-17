package com.xinzy.lib.service.serviceloader

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.xinzy.lib.service.serviceloader.entity.Param
import com.xinzy.lib.service.serviceloader.service.Calculate

class MainActivity : AppCompatActivity() {

    private var calculateService: Calculate? = null


    private val connectionMonitor = object : RemoteApi.ConnectionMonitor {
        override fun onConnected(api: RemoteApi) {
            calculateService = api.asInterface(Calculate::class.java)

            Log.d("MainActivity", "onConnected $api, $calculateService")
        }

        override fun onDisconnected(api: RemoteApi) {
            calculateService = null
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.connectService).setOnClickListener {
            val intent = Intent("com.xinzy.serviceloader.RemoteService")
            intent.setPackage(packageName)
            RemoteApi.create(this, intent, connectionMonitor)
        }
        findViewById<Button>(R.id.testAdd).setOnClickListener {
            calculateService?.let {
                val result = it.add(1, 3)
                Log.d("MainActivity", "onCreate: $result")
            }
        }
        findViewById<Button>(R.id.testCallback).setOnClickListener {
            calculateService?.let {
                it.setCallback(object : ICallback.Stub() {
                    override fun callback(key: String?, value: Int) {
                        Log.d("MainActivity", "callback: $key, $value ")
                    }
                })
            }
        }
        findViewById<Button>(R.id.testMulti).setOnClickListener {
            calculateService?.let {
//                val result = it.multi(Param(2, 3))
//                Log.d("MainActivity", "onCreate: $result")
            }
        }
    }
}