package com.trmamobilesolutions.alertcoin.base.api


import com.trmamobilesolutions.alertcoin.home.model.domain.Job
import io.reactivex.Flowable
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Created by tairo on 11/10/17 12:23 AM.
 */
interface ApiService {

    @GET("remote-dev+engineer+senior+digital-nomad-jobs.json")
    fun getAll(): Flowable<List<Job>>

    @GET("{path}")
    fun search(@Path("path") path: String): Flowable<List<Job>>
}