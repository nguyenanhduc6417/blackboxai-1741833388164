package com.example.myapp.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.myapp.data.network.api.User
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    var token: String?
        get() = prefs.getString(KEY_TOKEN, null)
        set(value) {
            prefs.edit().putString(KEY_TOKEN, value).apply()
        }

    var user: User?
        get() {
            val userJson = prefs.getString(KEY_USER, null)
            return if (userJson != null) {
                gson.fromJson(userJson, User::class.java)
            } else null
        }
        set(value) {
            val userJson = if (value != null) gson.toJson(value) else null
            prefs.edit().putString(KEY_USER, userJson).apply()
        }

    val isLoggedIn: Boolean
        get() = token != null

    fun saveSession(token: String, user: User) {
        this.token = token
        this.user = user
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREF_NAME = "MyAppPrefs"
        private const val KEY_TOKEN = "token"
        private const val KEY_USER = "user"

        @Volatile
        private var instance: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return instance ?: synchronized(this) {
                instance ?: SessionManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
