package com.trmamobilesolutions.alertcoin.home.model.domain

import com.google.gson.annotations.SerializedName

data class Exchange(var id: Int = 0,
                    var fees: Fees?,
                    var color: String = "",
                    var name: String = "",
                    @SerializedName("url_book")
                    var urlBook: String = "",
                    var url: String = "")