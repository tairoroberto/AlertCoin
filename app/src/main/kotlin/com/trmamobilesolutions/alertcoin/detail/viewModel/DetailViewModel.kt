package com.trmamobilesolutions.alertcoin.detail.viewModel

import android.arch.lifecycle.ViewModel
import com.trmamobilesolutions.alertcoin.home.model.domain.Job
import timber.log.Timber

/**
 * Created by tairo on 12/12/17.
 */
class DetailViewModel : ViewModel() {

    fun manipulateResponse(job: Job?) {
        Timber.i("job ${job?.position}")

    }

    fun showError(str: String) {

    }
}