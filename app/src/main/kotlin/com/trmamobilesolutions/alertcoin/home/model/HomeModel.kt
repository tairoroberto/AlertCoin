package com.trmamobilesolutions.alertcoin.home.model

import android.arch.lifecycle.LiveData
import com.google.gson.Gson
import com.trmamobilesolutions.alertcoin.CustomApplication
import com.trmamobilesolutions.alertcoin.CustomApplication.Companion.context
import com.trmamobilesolutions.alertcoin.R
import com.trmamobilesolutions.alertcoin.base.api.Api
import com.trmamobilesolutions.alertcoin.home.model.domain.ExchangesItem
import com.trmamobilesolutions.alertcoin.home.model.domain.Ticket
import io.reactivex.Flowable
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.StringWriter


/**
 * Created by tairo on 12/11/17.
 */
class HomeModel {
    fun listAll(application: CustomApplication): Flowable<Ticket> {
        //return Api(context).getApiService().getAll()

        val ticket: Flowable<Ticket> = Flowable.just(Gson().fromJson(getFromJson(application, R.raw.ticket), Ticket::class.java))
        return ticket
    }

    fun search(query: String): Flowable<Ticket> {
        return Api(context).getApiService().search(query)
    }

    fun listFromBD(): LiveData<List<ExchangesItem>> {
        return AppDatabase.getInstance(context).ExchangeDAO().getAll()
    }

    fun getFromJson(application: CustomApplication, id: Int) : String {
        val input = application.resources.openRawResource(id)
        val writer = StringWriter()

        input.use { input ->
            val reader = BufferedReader(InputStreamReader(input, "UTF-8"))

            var line = reader.readLine()
            while (line != null) {
                writer.write(line)
                line = reader.readLine()
            }
        }

        return writer.toString()
    }
}
