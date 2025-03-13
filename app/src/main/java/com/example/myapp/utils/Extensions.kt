package com.example.myapp.utils

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleCoroutineScope
import com.example.myapp.data.network.NetworkResult
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Context Extensions
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

// Fragment Extensions
fun Fragment.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    context?.showToast(message, duration)
}

// View Extensions
fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.showSnackbar(
    message: String,
    duration: Int = Snackbar.LENGTH_SHORT,
    action: String? = null,
    actionListener: (() -> Unit)? = null
) {
    val snackbar = Snackbar.make(this, message, duration)
    if (action != null && actionListener != null) {
        snackbar.setAction(action) { actionListener.invoke() }
    }
    snackbar.show()
}

// Flow Extensions
fun <T> Flow<NetworkResult<T>>.onStartLoading(): Flow<NetworkResult<T>> =
    onStart { emit(NetworkResult.loading()) }

fun <T> Flow<NetworkResult<T>>.handleErrors(): Flow<NetworkResult<T>> =
    catch { e -> emit(NetworkResult.error(e.message ?: "Unknown error occurred")) }

// Date Extensions
fun Date.format(pattern: String = Constants.DateFormat.DISPLAY_DATE_FORMAT): String {
    return SimpleDateFormat(pattern, Locale.getDefault()).format(this)
}

fun String.toDate(pattern: String = Constants.DateFormat.API_DATE_FORMAT): Date? {
    return try {
        SimpleDateFormat(pattern, Locale.getDefault()).parse(this)
    } catch (e: Exception) {
        null
    }
}

// Network Extensions
fun <T> Response<T>.isSuccessful(successCodes: Array<Int> = arrayOf(200, 201)): Boolean {
    return successCodes.contains(code())
}

// Validation Extensions
fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.isValidPassword(): Boolean {
    return length in Constants.Validation.MIN_PASSWORD_LENGTH..Constants.Validation.MAX_PASSWORD_LENGTH
}

// Coroutine Extensions
fun <T> Flow<T>.launchWhenStarted(lifecycleScope: LifecycleCoroutineScope) {
    lifecycleScope.launchWhenStarted {
        this@launchWhenStarted.collect {}
    }
}

// String Extensions
fun String?.orEmpty(defaultValue: String = ""): String {
    return this ?: defaultValue
}

fun String.capitalizeFirst(): String {
    return if (isNotEmpty()) {
        this[0].uppercase() + substring(1).lowercase()
    } else {
        this
    }
}

// Number Extensions
fun Int?.orZero(): Int = this ?: 0
fun Long?.orZero(): Long = this ?: 0L
fun Double?.orZero(): Double = this ?: 0.0
fun Float?.orZero(): Float = this ?: 0f

// Collection Extensions
fun <T> List<T>?.orEmpty(): List<T> = this ?: emptyList()
fun <K, V> Map<K, V>?.orEmpty(): Map<K, V> = this ?: emptyMap()

// Boolean Extensions
fun Boolean?.orFalse(): Boolean = this ?: false
fun Boolean?.orTrue(): Boolean = this ?: true
