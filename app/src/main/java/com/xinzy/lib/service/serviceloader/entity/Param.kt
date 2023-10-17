package com.xinzy.lib.service.serviceloader.entity

import android.os.Parcel
import android.os.Parcelable

data class Param(
    val first: Int,
    val second: Int
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(first)
        parcel.writeInt(second)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Param> {
        override fun createFromParcel(parcel: Parcel): Param {
            return Param(parcel)
        }

        override fun newArray(size: Int): Array<Param?> {
            return arrayOfNulls(size)
        }
    }

}
