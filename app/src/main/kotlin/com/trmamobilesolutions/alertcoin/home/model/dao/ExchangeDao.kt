package com.trmamobilesolutions.alertcoin.home.model.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.trmamobilesolutions.alertcoin.home.model.domain.ExchangesItem
import io.reactivex.Flowable

/**
 * Created by tairo on 12/12/17 3:03 PM.
 */
@Dao
interface ExchangeDao {
    @Query("SELECT * FROM exchanges")
    fun getAll(): LiveData<List<ExchangesItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(jobs: List<ExchangesItem>?)

    @Query("select * from exchanges where name = :name")
    fun getByID(name: String): Flowable<ExchangesItem>

    @Query("select * from exchanges where name = :name")
    fun loadByIdSync(name: String): ExchangesItem

    @Query("DELETE FROM exchanges")
    fun deleteAll()
}