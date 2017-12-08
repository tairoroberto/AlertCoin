package com.trmamobilesolutions.alertcoin.login

import android.app.LoaderManager.LoaderCallbacks
import android.content.Context
import android.content.CursorLoader
import android.content.Intent
import android.content.Loader
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import com.facebook.*
import com.trmamobilesolutions.alertcoin.registro.RegisterActivity
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.trmamobilesolutions.alertcoin.R
import com.trmamobilesolutions.alertcoin.base.extension.*
import com.trmamobilesolutions.alertcoin.home.view.MainActivity
import com.trmamobilesolutions.alertcoin.login.model.domain.UserLogin
import io.fabric.sdk.android.services.common.CommonUtils.hideKeyboard
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.startActivity
import java.util.*
import kotlin.collections.ArrayList


/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : AppCompatActivity(), LoaderCallbacks<Cursor>, FacebookCallback<LoginResult> {

    private var callbackManager: CallbackManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)



        email.setText(getPreferences(Context.MODE_PRIVATE).getString("email", ""))
        password.setText(getPreferences(Context.MODE_PRIVATE).getString("senha", ""))

        populateAutoComplete()
        password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })

        email_sign_in_button.setOnClickListener { attemptLogin() }

        FacebookSdk.sdkInitialize(applicationContext)
        callbackManager = CallbackManager.Factory.create()

        LoginManager.getInstance().registerCallback(callbackManager, this)

        login_button.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"))
        }


        textViewRegister.setOnClickListener({
            startActivity<RegisterActivity>()
        })
        textViewRegister2.setOnClickListener({
            startActivity<RegisterActivity>()
        })

        this.hideSoftKeyboard()
    }

    private fun populateAutoComplete() {
        if (!mayRequestContacts(email)) {
            return
        }

        loaderManager.initLoader(0, null, this)
    }


    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete()
            }
        }
    }

    private fun attemptLogin() {

        val emailStr = email.text.toString()
        val passwordStr = password.text.toString()

        if (TextUtils.isEmpty(emailStr)) {
            showTooltip(getString(R.string.error_field_required), textInputLayoutEmail, true)
            email.requestFocus()
            return
        }

        if (TextUtils.isEmpty(passwordStr)) {
            showTooltip(getString(R.string.error_field_required), textInputLayoutSenha, true)
            password.requestFocus()
            return
        }

        if (checkBox.isChecked) {
            val sharedPref = getPreferences(Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putString("email", emailStr)
            editor.putString("senha", passwordStr)
            editor.apply()
        }

        showProgress(textViewRegister2, login_progress, true)

        val userLogin = UserLogin()
        userLogin.email = emailStr
        userLogin.senha = passwordStr

        /*ApiUtils.getApiService()?.login(userLogin)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({
                    Log.i("LOG", "$it")
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }, { error ->
                    Log.i("ERROR", "${error.message}")
                    showProgress(textViewRegister2, login_progress, false)
                    showSnackBarError(email, "Falha ao comunicar com serivdor :(")
                })*/
        startActivity(Intent(this, MainActivity::class.java))
    }

    override fun onSuccess(result: LoginResult?) {
        val parameters = Bundle()
        parameters.putString("fields", "name, last_name, email, picture")
        val request = GraphRequest.newMeRequest(result?.accessToken) { userJson, _ ->
            if (userJson != null) {
                startActivity<MainActivity>()
                finish()
            }
        }
        request.parameters = parameters
        request.executeAsync()
    }

    override fun onCancel() {
        Toast.makeText(this, "Cancelado", Toast.LENGTH_SHORT).show()
    }

    override fun onError(error: FacebookException?) {
        showSnackBarError(login_button, "Falha ao fazer login :(")
        Log.i("", "Error: ${error?.localizedMessage} \n ${error?.message}")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateLoader(i: Int, bundle: Bundle?): Loader<Cursor> {
        return CursorLoader(this,
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI, ContactsContract.Contacts.Data.CONTENT_DIRECTORY),
                ProfileQuery.PROJECTION, ContactsContract.Contacts.Data.MIMETYPE + " = ?",
                arrayOf(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE),
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC")
    }

    override fun onLoadFinished(cursorLoader: Loader<Cursor>, cursor: Cursor) {
        val emails = ArrayList<String>()
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS))
            cursor.moveToNext()
        }

        addEmailsToAutoComplete(emails)
    }

    override fun onLoaderReset(cursorLoader: Loader<Cursor>) {

    }

    private fun addEmailsToAutoComplete(emailAddressCollection: List<String>) {
        val adapter = ArrayAdapter(this@LoginActivity,
                android.R.layout.simple_dropdown_item_1line, emailAddressCollection)

        email.setAdapter(adapter)
    }

    object ProfileQuery {
        val PROJECTION = arrayOf(
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY)
        val ADDRESS = 0
        val IS_PRIMARY = 1
    }

    companion object {

        /**
         * Id to identity READ_CONTACTS permission request.
         */
        private val REQUEST_READ_CONTACTS = 0
    }
}
