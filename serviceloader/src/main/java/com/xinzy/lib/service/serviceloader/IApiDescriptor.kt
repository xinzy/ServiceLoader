package com.xinzy.lib.service.serviceloader

import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.Parcelable
import android.os.RemoteException

interface IApiDescriptor : IInterface {

    @Throws(RemoteException::class)
    fun getVersion(): Int

    @Throws(RemoteException::class)
    fun getInterface(): IBinder?

    @Throws(RemoteException::class)
    fun getTypeInfo(): TypeInfo?


    abstract class Stub : Binder(), IApiDescriptor {

        init {
            attachInterface(this, DESCRIPTOR)
        }

        override fun asBinder(): IBinder {
            return this
        }

        override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            return when (code) {
                TRANSACTION_getVersion -> {
                    data.enforceInterface(DESCRIPTOR)
                    val result = getVersion()
                    reply?.writeNoException()
                    reply?.writeInt(result)
                    true
                }

                TRANSACTION_getInterface -> {
                    data.enforceInterface(DESCRIPTOR)
                    val result = getInterface()
                    reply?.writeNoException()
                    reply?.writeStrongBinder(result)
                    true
                }

                TRANSACTION_getTypeInfo -> {
                    data.enforceInterface(DESCRIPTOR)
                    val result = getTypeInfo()
                    reply?.writeNoException()
                    if (result != null) {
                        reply?.writeInt(1)
                        reply?.let { result.writeToParcel(it, Parcelable.PARCELABLE_WRITE_RETURN_VALUE) }
                    } else {
                        reply?.writeInt(0)
                    }
                    true
                }

                IBinder.LAST_CALL_TRANSACTION -> {
                    reply?.writeString(DESCRIPTOR)
                    true
                }

                else -> super.onTransact(code, data, reply, flags)
            }
        }


        class Proxy(private val remote: IBinder) : IApiDescriptor {

            override fun getVersion(): Int {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()

                return try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    remote.transact(TRANSACTION_getVersion, data, reply, 0)
                    reply.readException()

                    reply.readInt()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            override fun getInterface(): IBinder? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()

                return try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    remote.transact(TRANSACTION_getInterface, data, reply, 0)
                    reply.readException()

                    reply.readStrongBinder()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            override fun getTypeInfo(): TypeInfo? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()

                return try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    remote.transact(TRANSACTION_getTypeInfo, data, reply, 0)
                    reply.readException()
                    val flag = reply.readInt()

                    if (flag == 1) TypeInfo.CREATOR.createFromParcel(reply) else null
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            override fun asBinder(): IBinder = remote

            fun getInterfaceDescriptor(): String = DESCRIPTOR
        }

        companion object {
            const val DESCRIPTOR = "com.xinzy.lib.service.serviceloader.IApiDescriptor"

            const val TRANSACTION_getVersion = 1

            const val TRANSACTION_getInterface = 2

            const val TRANSACTION_getTypeInfo = 3

            @JvmStatic
            fun asInterface(obj: IBinder): IApiDescriptor {
                val iin = obj.queryLocalInterface(DESCRIPTOR)
                return if (iin != null && iin is IApiDescriptor) iin else Proxy(obj)
            }
        }
    }

}