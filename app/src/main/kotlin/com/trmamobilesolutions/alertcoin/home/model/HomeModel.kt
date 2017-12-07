package com.trmamobilesolutions.alertcoin.home.model

import android.arch.lifecycle.LiveData
import com.trmamobilesolutions.alertcoin.CustomApplication
import com.trmamobilesolutions.alertcoin.CustomApplication.Companion.context
import com.trmamobilesolutions.alertcoin.base.api.Api
import com.trmamobilesolutions.alertcoin.home.model.domain.ExchangesItem
import com.trmamobilesolutions.alertcoin.home.model.domain.Ticket
import io.reactivex.Flowable


/**
 * Created by tairo on 12/11/17.
 */
class HomeModel {
    fun listAll(application: CustomApplication): Flowable<Ticket> {
        return Api(context).getApiService().getAll()
    }

    fun search(query: String): Flowable<Ticket> {
        return Api(context).getApiService().search(query)
    }

    fun listFromBD(): LiveData<List<ExchangesItem>> {
        return AppDatabase.getInstance(context).ExchangeDAO().getAll()
    }
}
