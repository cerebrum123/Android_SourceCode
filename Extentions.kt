package com.battery.cygni.presentation.common

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.battery.cygni.R
import com.battery.cygni.utils.event.helper.Resource
import com.battery.cygni.utils.toast.MyToast
import com.battery.cygni.utils.toast.SnackbarUtils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar

/** Network Extensions */
fun Context.isNetworkAvailable(): Boolean {
    val connectivityManager =
        this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = connectivityManager.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnected
}

fun SharedPreferences.saveValue(key: String, value: Any?) {
    when (value) {
        is String? -> editNdCommit { it.putString(key, value) }
        is Int -> editNdCommit { it.putInt(key, value) }
        is Boolean -> editNdCommit { it.putBoolean(key, value) }
        is Float -> editNdCommit { it.putFloat(key, value) }
        is Long -> editNdCommit { it.putLong(key, value) }
        else -> throw UnsupportedOperationException("Not yet implemented")
    }
}

fun <T> SharedPreferences.getValue(key: String, defaultValue: Any? = null): T? {
    return when (defaultValue) {
        is String? -> {
            getString(key, defaultValue as? String) as? T
        }
        is Int -> {
            getInt(key, defaultValue as? Int ?: -1) as? T
        }
        is Boolean -> getBoolean(key, defaultValue as? Boolean ?: false) as? T
        is Float -> getFloat(key, defaultValue as? Float ?: -1f) as? T
        is Long -> getLong(key, defaultValue as? Long ?: -1) as? T
        else -> throw UnsupportedOperationException("Not yet implemented")
    }
}

inline fun SharedPreferences.editNdCommit(operation: (SharedPreferences.Editor) -> Unit) {
    val editor = this.edit()
    operation(editor)
    editor.apply()
}


fun Activity.hideKeyboard() {
    val manager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    manager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
}

fun Activity.showKeyboard() {
    val manager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    manager.toggleSoftInputFromWindow(
        this.currentFocus?.applicationWindowToken,
        InputMethodManager.SHOW_FORCED,
        0
    );
}

fun Activity.showSuccessSnack(message: String, top: Boolean = false) {
    val view: View = findViewById(android.R.id.content)
    SnackbarUtils.with(view).setMessage(message).showSuccess(top)
}

fun Activity.showWarningSnack(message: String, top: Boolean = false) {
    val view: View = findViewById(android.R.id.content)
    SnackbarUtils.with(view).setMessage(message).showWarning(top)
}

fun Activity.showErrorSnack(message: String, top: Boolean = false) {
    val view: View = findViewById(android.R.id.content)
    SnackbarUtils.with(view).setMessage(message).showError(top)
}

fun Activity.showDefaultSnack(message: String, top: Boolean = false) {
    val view: View = findViewById(android.R.id.content)
    SnackbarUtils.with(view).setMessage(message).show(top)
}


fun Activity.showErrorToast(message: String) {
    if (message.isEmpty())
        return
    MyToast.error(this, message, Toast.LENGTH_SHORT, true).show()
}

fun Activity.showInfoToast(message: String) {
    MyToast.info(this, message, Toast.LENGTH_SHORT, true).show()
}

fun Fragment.successToast(message: String) {
    if (message.isNotEmpty())
        MyToast.success(this.requireContext(), message, Toast.LENGTH_SHORT, true).show()
}

fun Activity.successToast(message: String) {
    if (message.isNotEmpty())
        MyToast.success(this, message, Toast.LENGTH_SHORT, true).show()
}

fun View.showSnackBar(message: String) {
    Snackbar.make(this, message, Snackbar.LENGTH_LONG).also {
        it.view.setBackgroundColor(ContextCompat.getColor(this.context, R.color.black))
        it.show()
    }
}

fun <T> Resource<T>.log() {
    Log.i("Resource", this.toString())
}


fun RecyclerView.setLinearLayoutManger() {
    this.layoutManager = LinearLayoutManager(this.context)
}


fun Fragment.showSheet(sheet: BottomSheetDialogFragment) {
    sheet.show(this.childFragmentManager, sheet.tag)
}

fun FragmentActivity.showSheet(sheet: BottomSheetDialogFragment) {
    sheet.show(this.supportFragmentManager, sheet.tag)
}

fun ContentResolver.getFileName(fileUri: Uri): String {

    var name = ""
    val returnCursor = this.query(fileUri, null, null, null, null)
    if (returnCursor != null) {
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        name = returnCursor.getString(nameIndex)
        returnCursor.close()
    }

    return name
}

fun Resources.dptoPx(dp: Int): Float {
    return dp * this.displayMetrics.density
}

fun <T> Activity.startNewActivity(s: Class<T>, killCurrent: Boolean = false) {
    val intent = Intent(this, s)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
    startActivity(intent)
    if (killCurrent)
        finish()
}

fun <T> Activity.getNewIntent(s: Class<T>): Intent {
    val intent = Intent(this, s)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
    return intent
}


fun String.toGenderId(): String {
    return when (this) {
        "Male" -> "1"
        "Female" -> "2"
        "Unspecified" -> "3"
        "Undisclosed" -> "4"
        else -> "0"
    }
}


fun String.toGenderName(): String {
    return when (this) {
        "1" -> "Male"
        "2" -> "Female"
        "3" -> "Unspecified"
        "4" -> "Undisclosed"
        else -> ""
    }
}

fun View.rotateInfinite() {
    val rotate = RotateAnimation(
        0f, 360f,
        Animation.RELATIVE_TO_SELF, 0.5f,
        Animation.RELATIVE_TO_SELF, 0.5f
    )
    rotate.duration = 5500
    rotate.interpolator = LinearInterpolator()
    rotate.repeatCount = Animation.INFINITE
    this.startAnimation(rotate)
}

fun String.decodeHex(): ByteArray {
    check(length % 2 == 0) { "Must have an even length" }

    return chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
}

fun ByteArray.toHexString(): String {
    return String(this, Charsets.UTF_16)
}

fun String.hexToString(): String {
    val output = StringBuilder()
    var i = 0
    while (i < this.length) {
        val str: String = this.substring(i, i + 2)
        output.append(str.toInt(16).toChar())
        i += 2
    }
    return output.toString()
}

fun String.hexToInt(): Int {
    return this.toInt(16)
}

fun String.convertToBinary(): String {
    var binaryNum = ""
    if (checkHexaDecimalNumber(this)) {
        var i = 0
        while (i < this.length) {
            when (this[i]) {
                '0' -> binaryNum += "0000"
                '1' -> binaryNum += "0001"
                '2' -> binaryNum += "0010"
                '3' -> binaryNum += "0011"
                '4' -> binaryNum += "0100"
                '5' -> binaryNum += "0101"
                '6' -> binaryNum += "0110"
                '7' -> binaryNum += "0111"
                '8' -> binaryNum += "1000"
                '9' -> binaryNum += "1001"
                'A', 'a' -> binaryNum += "1010"
                'B', 'b' -> binaryNum += "1011"
                'C', 'c' -> binaryNum += "1100"
                'D', 'd' -> binaryNum += "1101"
                'E', 'e' -> binaryNum += "1110"
                'F', 'f' -> binaryNum += "1111"
            }
            i++
        }

    }
    return binaryNum
}


private fun checkHexaDecimalNumber(hexaDecimalNum: String): Boolean {
    var isHexaDecimalNum = true
    for (charAtPos in hexaDecimalNum) {
        if (!(((charAtPos >= '0') && (charAtPos <= '9'))
                    || ((charAtPos >= 'A') && (charAtPos <= 'F'))
                    || ((charAtPos >= 'a') && (charAtPos <= 'f'))
                    )
        ) {
            isHexaDecimalNum = false
            break
        }
    }
    return isHexaDecimalNum
}

fun List<String>.toReadableData(): String {
    val b = StringBuilder()
    this.forEach {
        b.append(it)
        b.append(" ")
    }
    return b.toString()
}


fun List<String>.toReadableDataWithIndex(): String {
    var index = 0
    val b = StringBuilder()
    this.forEach {
        b.append(index)
        b.append("=>")
        b.append(it)
        b.append("\n")
        index++
    }
    return b.toString()
}
