package com.xinzy.lib.service.serviceloader

import android.os.IBinder
import android.os.IInterface

class ApiDescriptor(private val api: IInterface, private val version: Int) : IApiDescriptor.Stub() {

    private val typeInfo: TypeInfo

    init {
        val clazz = api.javaClass.superclass.interfaces[0]
        typeInfo = TypeInfo(clazz)
    }

    override fun getVersion(): Int = version

    override fun getInterface(): IBinder? = api.asBinder()

    override fun getTypeInfo(): TypeInfo = typeInfo
}