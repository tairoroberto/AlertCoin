package com.trmamobilesolutions.alertcoin.home.model

import android.arch.lifecycle.LiveData
import com.trmamobilesolutions.alertcoin.CustomApplication.Companion.context
import com.trmamobilesolutions.alertcoin.base.api.Api
import com.trmamobilesolutions.alertcoin.home.model.domain.Job
import io.reactivex.Flowable

/**
 * Created by tairo on 12/11/17.
 */
class HomeModel {
    fun listAll(): Flowable<List<Job>> {
        return Api(context).getApiService().getAll()
    }

    fun search(query: String): Flowable<List<Job>> {
        return Api(context).getApiService().search(query)
    }

    fun listFromBD(): LiveData<List<Job>> {
        return AppDatabase.getInstance(context).jobsDAO().getAll()
    }
}
