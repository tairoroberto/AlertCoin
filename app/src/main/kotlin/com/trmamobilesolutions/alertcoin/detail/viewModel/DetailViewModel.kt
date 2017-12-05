package com.trmamobilesolutions.alertcoin.detail.viewModel

import android.arch.lifecycle.ViewModel
import com.trmamobilesolutions.alertcoin.home.model.domain.Ticket
import timber.log.Timber

/**
 * Created by tairo on 12/12/17.
 */
class DetailViewModel : ViewModel() {

    fun manipulateResponse(ticket: Ticket?) {
        Timber.i("job ${ticket?.total?.trades}")

    }

    fun showError(str: String) {

    }
}