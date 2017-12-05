package com.trmamobilesolutions.alertcoin.home.model

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context
import com.trmamobilesolutions.alertcoin.base.converter.Converters
import com.trmamobilesolutions.alertcoin.home.model.dao.ExchangeDao
import com.trmamobilesolutions.alertcoin.home.model.domain.ExchangesItem

@Database(entities = arrayOf(ExchangesItem::class), version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun ExchangeDAO(): ExchangeDao

    companion object {

        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context?): AppDatabase =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: buildDatabase(context as Context).also { INSTANCE = it }
                }

        private fun buildDatabase(context: Context) =
                Room.databaseBuilder(context.applicationContext,
                        AppDatabase::class.java, "alertcoin.db")
                        .build()
    }
}
