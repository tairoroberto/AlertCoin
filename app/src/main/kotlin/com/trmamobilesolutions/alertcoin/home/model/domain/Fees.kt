package com.trmamobilesolutions.alertcoin.home.model.domain

import com.google.gson.annotations.SerializedName

data class Fees(@SerializedName("in_BTC")
                var inBTC: List<Int>?,
                @SerializedName("trade_book")
                var tradeBook: List<Double>?,
                @SerializedName("out_BRL")
                var outBRL: List<Double>?,
                @SerializedName("in_BRL")
                var inBRL: List<Double>?,
                @SerializedName("out_BTC")
                var outBTC: List<Int>?,
                @SerializedName("trade_market")
                var tradeMarket: List<Double>?)