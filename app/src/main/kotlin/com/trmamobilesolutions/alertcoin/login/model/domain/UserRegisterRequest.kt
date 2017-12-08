package com.trmamobilesolutions.alertcoin.login.model.domain

import com.google.gson.annotations.SerializedName

/**
 * Created by tairo on 9/25/17.
 */
class UserRegisterRequest(@SerializedName("name") var nome: String = "",
                          @SerializedName("birth_date") var dataNascimento: String = "",
                          @SerializedName("phone_number") var telefone: String = "",
                          @SerializedName("email") var email: String = "",
                          @SerializedName("password") var senha: String = "")