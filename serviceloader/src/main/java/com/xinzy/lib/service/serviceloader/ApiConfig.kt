package com.xinzy.lib.service.serviceloader

import android.content.Intent

data class ApiConfig(
    val name: String,
    val service: Intent,
    val version: String,
    val type: Int
) {

    val typeName: String
        get() = when (type) {
            CONNECT_BIND -> "BIND"
            CONNECT_HUB -> "HUB"
            CONNECT_START -> "START"
            else -> "UNKNOWN"
        }

    companion object {
        const val CONNECT_BIND = 1
        const val CONNECT_HUB = 2
        const val CONNECT_START = 3

        @JvmStatic
        fun create(service: Intent) = ApiConfig("", service, "", CONNECT_BIND)
    }
}
