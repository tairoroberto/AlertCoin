package com.trmamobilesolutions.alertcoin.home.model.domain

data class Total(val high: Int = 0,
                 val vol: Double = 0.0,
                 val last: Double = 0.0,
                 val money: Double = 0.0,
                 val low: Double = 0.0,
                 val vwap: Double = 0.0,
                 val trades: Int = 0)