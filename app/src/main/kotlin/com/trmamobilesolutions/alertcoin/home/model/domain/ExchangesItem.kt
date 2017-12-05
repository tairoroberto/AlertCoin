package com.trmamobilesolutions.alertcoin.home.model.domain

data class ExchangesItem(val high: Double = 0.0,
                         val vol: Double = 0.0,
                         val last: Double = 0.0,
                         val money: Double = 0.0,
                         val low: Int = 0,
                         val legend: String = "",
                         val vwap: Double = 0.0,
                         val name: String = "",
                         val trades: Int = 0,
                         val open: Int = 0)