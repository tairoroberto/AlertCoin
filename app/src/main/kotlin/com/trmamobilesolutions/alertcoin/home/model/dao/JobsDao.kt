package com.trmamobilesolutions.alertcoin.home.model.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.trmamobilesolutions.alertcoin.home.model.domain.Job
import io.reactivex.Flowable

/**
 * Created by tairo on 12/12/17 3:03 PM.
 */
@Dao
interface JobsDao {
    @Query("SELECT * FROM jobs")
    fun getAll(): LiveData<List<Job>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(jobs: List<Job>?)

    @Query("select * from jobs where id = :id")
    fun getByID(id: Int): Flowable<Job>

    @Query("select * from jobs where id = :id")
    fun loadByIdSync(id: Int): Job

    @Query("DELETE FROM jobs")
    fun deleteAll()
}