package br.com.tairoroberto.cindy.extension

import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import java.util.regex.Pattern

/**
 * Created by tairo on 9/27/17.
 */
fun Context.unmask(s: String): String {
    return if (!TextUtils.isEmpty(s)) {
        s.replace("R$", "").replace(",", "")
                .replace(".", "").trim { it <= ' ' }
    } else {
        ""
    }
}

fun Context.currencyToDouble(str: String): Double {
    val hasMask = (str.contains("R$") ||
            str.contains("$")) ||
            str.contains("R") ||
            (str.contains(".") || str.contains(",") || str.contains("€"))

    var strAux = str

    if (hasMask) {
        strAux = removeCaracters(str)
    }
    return if (TextUtils.isEmpty(strAux)) {
        0.0
    } else {
        String.format(Locale.US, "%.2f", strAux.toDouble() / 100).toDouble()
    }
}

fun Context.removeCaracters(str: String): String {
    return str.replace("R$", "")
            .replace(",", "")
            .replace(".", "")
            .replace("R", "")
            .replace("$", "")
            .replace("€", "")
            .replace(" ", "")
            .trim()
}

fun Context.insert(mask: String, ediTxt: EditText): TextWatcher {
    return object : TextWatcher {
        internal var isUpdating: Boolean = false
        internal var old = ""

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            val str = ediTxt.context.unmask(s.toString())
            val mascara = StringBuilder()
            if (isUpdating) {
                old = str
                isUpdating = false
                return
            }
            var i = 0
            for (m in mask.toCharArray()) {
                if (m != '#' && str.length > old.length) {
                    mascara.append(m)
                    continue
                }
                try {
                    mascara.append(str[i])
                } catch (e: Exception) {
                    break
                }

                i++
            }
            isUpdating = true
            ediTxt.setText(mascara.toString())
            ediTxt.setSelection(mascara.toString().length)
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { /*unused*/
        }

        override fun afterTextChanged(s: Editable) { /*unused*/
        }
    }
}

fun Context.isValidEmail(target: CharSequence?): Boolean {
    return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches()
}

fun Context.formatCurrency(value: Double?): String {
    val ptBr = Locale("pt", "BR")
    val nf = NumberFormat.getNumberInstance(ptBr) as DecimalFormat
    nf.isGroupingUsed = true
    nf.positivePrefix = "R$ "
    nf.negativePrefix = "R$ -"
    nf.minimumFractionDigits = 2
    nf.maximumFractionDigits = 2
    return nf.format(value)
}

private fun padNumber(number: String, maxLength: Int): String {
    val padded = StringBuilder(number)
    for (i in 0 until maxLength - number.length) {
        padded.append(" ")
    }
    return padded.toString()
}

fun Context.formatPhone(current: String): String {
    var number = current.replace("[^0-9]*".toRegex(), "")
    if (number.length > 11) {
        number = number.substring(0, 11)
    }
    val length = number.length
    Log.i("TAG", "formatPhone: " + length)

    val paddedNumber = padNumber(number, 11)

    val ddd = paddedNumber.substring(0, 2)
    val part1 = paddedNumber.substring(2, 6)
    val part2 = paddedNumber.substring(6, 11)

    return "($ddd) $part1-$part2"
}

fun Context.checkIfHaveSpecialCharacter(s: String): Boolean {
    val special = "!@#$%^&*,.()_-+'`[]~|;\\/=?\"{}<>:"
    val pattern = ".*[" + Pattern.quote(special) + "].*"
    return s.matches(pattern.toRegex())
}
