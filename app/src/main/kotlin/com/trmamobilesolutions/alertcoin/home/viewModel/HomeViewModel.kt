package com.trmamobilesolutions.alertcoin.home.viewModel

import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.trmamobilesolutions.alertcoin.CustomApplication
import com.trmamobilesolutions.alertcoin.home.model.AppDatabase
import com.trmamobilesolutions.alertcoin.home.model.HomeModel
import com.trmamobilesolutions.alertcoin.home.model.domain.ExchangesItem
import com.trmamobilesolutions.alertcoin.home.model.domain.Ticket
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.doAsync


/**
 * Created by tairo on 12/12/17.
 */
class HomeViewModel(application: CustomApplication, private val appDatabase: AppDatabase) : AndroidViewModel(application) {


    private var model = HomeModel()

    private val disposables = CompositeDisposable()

    private val response: MutableLiveData<Ticket> = MutableLiveData()

    private val responseFromDataBase: MutableLiveData<List<ExchangesItem>> = MutableLiveData()

    private val loadingStatus = MutableLiveData<Boolean>()

    private val errorStatus = MutableLiveData<String>()

    fun getLoadingStatus(): MutableLiveData<Boolean> {
        return loadingStatus
    }

    fun getErrorStatus(): MutableLiveData<String> {
        return errorStatus
    }

    fun getResponse(): MutableLiveData<Ticket> {
        return response
    }

    fun getResponseFromDataBase(): MutableLiveData<List<ExchangesItem>> {
        return responseFromDataBase
    }

    fun getAllJobs() {
        loadResponse(model.listAll(getApplication()))
    }

    fun getAllJobsDataBase() {
        loadResponseFromDataBase(model.listFromBD())
    }

    fun search(query: String) {
        loadResponse(model.search(query.toLowerCase()))
    }

    fun loadResponse(jobsResponse: Flowable<Ticket>) {
        disposables.add(jobsResponse
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe({ s -> loadingStatus.setValue(true) })
                .doAfterTerminate({ loadingStatus.setValue(false) })
                .subscribe(
                        { ticket ->
                            response.value = ticket
                            doAsync {
                                appDatabase.ExchangeDAO().deleteAll()
                                appDatabase.ExchangeDAO().insertAll(ticket.exchanges)
                            }
                        },
                        { throwable -> errorStatus.value = throwable.message.toString() }
                )
        )
    }

    private fun loadResponseFromDataBase(jobsResponse: LiveData<List<ExchangesItem>>) {
        doAsync {
            jobsResponse.observeForever {
                if (it?.isNotEmpty() == true) {
                    responseFromDataBase.value = it.subList(0, if (it.size > 30) 30 else it.lastIndex).sortedByDescending { it.name }
                }
            }
        }
    }
}