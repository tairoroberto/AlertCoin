package com.trmamobilesolutions.alertcoin.home.model.domain

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable

@Entity(tableName = "exchanges")
data class ExchangesItem(

        @PrimaryKey
        @ColumnInfo(name = "id")
        var id: Int = 0,

        @ColumnInfo(name = "high")
        var high: Double = 0.0,

        @ColumnInfo(name = "vol")
        var vol: Double = 0.0,

        @ColumnInfo(name = "last")
        var last: Double = 0.0,

        @ColumnInfo(name = "money")
        var money: Double = 0.0,

        @ColumnInfo(name = "low")
        var low: Double = 0.0,

        @ColumnInfo(name = "legend")
        var legend: String = "",

        @ColumnInfo(name = "vwap")
        var vwap: Double = 0.0,

        @ColumnInfo(name = "name")
        var name: String = "",

        @ColumnInfo(name = "trades")
        var trades: Int = 0,

        @ColumnInfo(name = "open")
        var open: Double = 0.0) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readString(),
            parcel.readDouble(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readDouble())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeDouble(high)
        parcel.writeDouble(vol)
        parcel.writeDouble(last)
        parcel.writeDouble(money)
        parcel.writeDouble(low)
        parcel.writeString(legend)
        parcel.writeDouble(vwap)
        parcel.writeString(name)
        parcel.writeInt(trades)
        parcel.writeDouble(open)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ExchangesItem> {
        override fun createFromParcel(parcel: Parcel): ExchangesItem {
            return ExchangesItem(parcel)
        }

        override fun newArray(size: Int): Array<ExchangesItem?> {
            return arrayOfNulls(size)
        }
    }
}