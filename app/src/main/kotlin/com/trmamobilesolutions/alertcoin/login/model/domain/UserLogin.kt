package com.trmamobilesolutions.alertcoin.login.model.domain

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

/**
 * Created by tairo on 10/7/17.
 */
data class UserLogin(@SerializedName("email") var email: String = "",
                     @SerializedName("password") var senha: String = "") : Parcelable {
    constructor(source: Parcel) : this(
            source.readString(),
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(email)
        writeString(senha)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<UserLogin> = object : Parcelable.Creator<UserLogin> {
            override fun createFromParcel(source: Parcel): UserLogin = UserLogin(source)
            override fun newArray(size: Int): Array<UserLogin?> = arrayOfNulls(size)
        }
    }
}