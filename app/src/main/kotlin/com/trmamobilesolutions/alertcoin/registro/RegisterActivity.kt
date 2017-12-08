package com.trmamobilesolutions.alertcoin.registro

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.trmamobilesolutions.alertcoin.R
import com.trmamobilesolutions.alertcoin.base.extension.showSnackBarError
import com.trmamobilesolutions.alertcoin.base.extension.showTooltip
import com.trmamobilesolutions.alertcoin.home.view.MainActivity
import com.trmamobilesolutions.alertcoin.login.model.domain.UserRegisterRequest
import kotlinx.android.synthetic.main.activity_register.*
import org.jetbrains.anko.startActivity
import java.util.*

class RegisterActivity : AppCompatActivity(), FacebookCallback<LoginResult> {

    private var callbackManager: CallbackManager? = null

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.activity_register)

        FacebookSdk.sdkInitialize(applicationContext)
        callbackManager = CallbackManager.Factory.create()

        LoginManager.getInstance().registerCallback(callbackManager, this)

        btnLoginFacebook.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"))
        }

        btnRegistrar.setOnClickListener {
            registrarUsuario()
        }
    }

    private fun registrarUsuario() {
        if (editNome.text.isEmpty()) {
            showTooltip("Preencha o nome!", editNome, true)
            return
        }

        if (editDataNascimento.text.isEmpty()) {
            showTooltip("Preencha data de nascimento!", editDataNascimento, true)
            return
        }

        if (editTelefoneCliente.text.isEmpty()) {
            showTooltip("Preencha o telefone!", editTelefoneCliente, true)
            return
        }

        if (editEmail.text.isEmpty()) {
            showTooltip("Preencha o email!", editEmail, true)
            return
        }

        if (editSenha.text.isEmpty()) {
            showTooltip("Preencha a senha!", editSenha, true)
            return
        }

        val userRegister = UserRegisterRequest()
        userRegister.nome = editNome.text.toString()
        userRegister.dataNascimento = editDataNascimento.text.toString()
        userRegister.telefone = editTelefoneCliente.text.toString()
        userRegister.email = editEmail.text.toString()
        userRegister.senha = editSenha.text.toString()

        /*ApiUtils.getApiService()?.registerUser(userRegister)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({
                    Log.i("LOG", "$it")
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }, { error ->
                    Log.i("ERROR", "${error.message}")
                    showSnackBarError(editNome, "Falha ao comunicar com serivdor :(")
                })*/
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager?.onActivityResult(requestCode, resultCode, data)
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
        showSnackBarError(btnLoginFacebook, "Falha ao fazeer login :(")
    }
}
