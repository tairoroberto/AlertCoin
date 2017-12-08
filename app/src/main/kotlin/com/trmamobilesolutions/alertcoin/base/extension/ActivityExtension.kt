package com.trmamobilesolutions.alertcoin.base.extension

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.ContentProviderOperation
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.provider.ContactsContract
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import com.trmamobilesolutions.alertcoin.R
import com.trmamobilesolutions.alertcoin.base.extension.tooltip.Tooltip
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by tairo on 11/12/17.
 */

val REQUEST_IMAGE_CAPTURE = 1
val REQUEST_TAKE_PHOTO = 2
val REQUEST_CAMERA_PERMISSION = 3

fun Activity.showProgress(form: View?, progressBar: ProgressBar?, show: Boolean) {
    val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

    form?.visibility = if (show) View.GONE else View.VISIBLE
    form?.animate()
            ?.setDuration(shortAnimTime)
            ?.alpha((if (show) 0 else 1).toFloat())
            ?.setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    form.visibility = if (show) View.GONE else View.VISIBLE
                }
            })

    progressBar?.visibility = if (show) View.VISIBLE else View.GONE
    progressBar?.animate()
            ?.setDuration(shortAnimTime)
            ?.alpha((if (show) 1 else 0).toFloat())
            ?.setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    progressBar.visibility = if (show) View.VISIBLE else View.GONE
                }
            })
}

fun Activity.showSnackBarError(view: View?, msg: String) {
    val snackbar: Snackbar = Snackbar.make(view as View, msg, Snackbar.LENGTH_LONG)
            .setAction("OK", null)
    snackbar.view.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light))
    snackbar.show()
}

fun Activity.isConected(): Boolean {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val netInfo = cm.activeNetworkInfo
    return netInfo != null && netInfo.isConnectedOrConnecting
}

fun Activity.hideSoftKeyboard() {
    val view = currentFocus
    if (view != null) {
        (getSystemService(Context.INPUT_METHOD_SERVICE)
                as? InputMethodManager)?.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

fun Activity.showSoftKeyboard() {
    val view = currentFocus
    if (view != null) {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.showSoftInput(view, 0)
    }
}


fun Activity.requestCameraPermission(view: View) {

    val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    )

    if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
            Manifest.permission.CAMERA)) {
        ActivityCompat.requestPermissions(this, permissions, REQUEST_CAMERA_PERMISSION)
        return
    }

    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this, permissions, REQUEST_CAMERA_PERMISSION)
        return
    }

    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this, permissions, REQUEST_CAMERA_PERMISSION)
        return
    }

    val thisActivity = this

    val listener = View.OnClickListener {
        ActivityCompat.requestPermissions(thisActivity, permissions,
                REQUEST_CAMERA_PERMISSION)
    }

    Snackbar.make(view, R.string.permission_rationale_camera,
            Snackbar.LENGTH_INDEFINITE)
            .setAction("Ok", listener)
            .show()
}

fun Activity.showTooltip(message: String, view: View, alignAnchorToLeft: Boolean) {
    Tooltip.make(
            view.context,
            Tooltip.Builder(131)
                    .anchor(view, Tooltip.Gravity.TOP)
                    .closePolicy(Tooltip.ClosePolicy.TOUCH_ANYWHERE_CONSUME, 4000)
                    .text(message)
                    .marginRightAndLeft(40)
                    .alignAnchorToLeft(alignAnchorToLeft)
                    .build()
    ).show()
}

fun Activity.showTooltip(message: String, view: View, alignAnchorToLeft: Boolean, color: Int) {
    Tooltip.make(
            view.context,
            Tooltip.Builder(131)
                    .anchor(view, Tooltip.Gravity.TOP)
                    .closePolicy(Tooltip.ClosePolicy.TOUCH_ANYWHERE_CONSUME, 4000)
                    .text(message)
                    .color(color)
                    .textColor(ContextCompat.getColor(view.context, R.color.white))
                    .marginRightAndLeft(40)
                    .alignAnchorToLeft(alignAnchorToLeft)
                    .build()
    ).show()
}

fun Activity.addContact(displayName: String?, number: String?, email: String?) {
    val operationList: ArrayList<ContentProviderOperation> = ArrayList()
    operationList.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
            .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
            .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
            .build())

    // first and last names
    operationList.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName)
            .build())

    operationList.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_HOME)
            .build())

    operationList.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)

            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.Email.DATA, email)
            .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
            .build())

    try {
        contentResolver.applyBatch(ContactsContract.AUTHORITY, operationList)
    } catch (e: Exception) {
        e.printStackTrace()
        Log.i("LOG", "Exception add contact: ${e.message}")
    }
}

fun Context.dpToPx(dp: Int): Int {
    val metrics = resources.displayMetrics
    return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT).toInt()
}

fun Context.pxToDp(px: Int): Int {
    val metrics = resources.displayMetrics
    return px / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT).toInt()
}

fun Activity.mayRequestContacts(view: View): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        return true
    }

    if (checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
        return true
    }

    if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS) || shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CONTACTS)) {
        Snackbar.make(view, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                .setAction(android.R.string.ok,
                        { requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS), 0) })
    } else {
        requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS), 0)
    }
    return false
}

fun Context.formataData(data: String?): Date? {
    if (data == null || data == "")
        return null

    var date: Date? = null
    try {
        val formatter = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.GERMANY)
        date = formatter.parse(data)
    } catch (e: ParseException) {
        e.printStackTrace()
    }

    return date
}

fun Context.formataDataToString(date: Date?): String? {
    if (date == null || date.equals(""))
        return null

    val formatador = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.GERMANY)//new SimpleDateFormat("dd/MM/yyyy");
    return formatador.format(date)
}

fun Context.getDataHoje(): Date {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.time
}

fun Context.getDateToStringShort(date: Date?): String? {
    if (date == null)
        return null

    val formatador = SimpleDateFormat("dd/MM/yyyy", Locale.GERMANY)
    return formatador.format(date)
}

