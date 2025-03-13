package com.example.myapp.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.myapp.utils.Constants
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Singleton
class PreferenceManager @Inject constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        Constants.Prefs.PREF_NAME,
        Context.MODE_PRIVATE
    )
    private val gson = Gson()

    // App theme
    var isDarkMode by booleanPreference(defaultValue = false)

    // App settings
    var isNotificationsEnabled by booleanPreference(defaultValue = true)
    var isSoundEnabled by booleanPreference(defaultValue = true)
    var isVibrationEnabled by booleanPreference(defaultValue = true)

    // App state
    var isFirstLaunch by booleanPreference(defaultValue = true)
    var lastSyncTime by longPreference(defaultValue = 0L)
    var appLanguage by stringPreference(defaultValue = "en")

    // Cache control
    var cacheExpiryTime by longPreference(defaultValue = 0L)
    
    fun clearPreferences() {
        prefs.edit().clear().apply()
    }

    // Preference Delegates
    private fun booleanPreference(
        defaultValue: Boolean = false,
        key: String? = null
    ) = PreferenceDelegate(
        preferences = prefs,
        defaultValue = defaultValue,
        key = key,
        getter = SharedPreferences::getBoolean,
        setter = SharedPreferences.Editor::putBoolean
    )

    private fun stringPreference(
        defaultValue: String = "",
        key: String? = null
    ) = PreferenceDelegate(
        preferences = prefs,
        defaultValue = defaultValue,
        key = key,
        getter = SharedPreferences::getString,
        setter = SharedPreferences.Editor::putString
    )

    private fun intPreference(
        defaultValue: Int = 0,
        key: String? = null
    ) = PreferenceDelegate(
        preferences = prefs,
        defaultValue = defaultValue,
        key = key,
        getter = SharedPreferences::getInt,
        setter = SharedPreferences.Editor::putInt
    )

    private fun longPreference(
        defaultValue: Long = 0L,
        key: String? = null
    ) = PreferenceDelegate(
        preferences = prefs,
        defaultValue = defaultValue,
        key = key,
        getter = SharedPreferences::getLong,
        setter = SharedPreferences.Editor::putLong
    )

    private fun floatPreference(
        defaultValue: Float = 0f,
        key: String? = null
    ) = PreferenceDelegate(
        preferences = prefs,
        defaultValue = defaultValue,
        key = key,
        getter = SharedPreferences::getFloat,
        setter = SharedPreferences.Editor::putFloat
    )

    private class PreferenceDelegate<T>(
        private val preferences: SharedPreferences,
        private val defaultValue: T,
        private val key: String?,
        private val getter: SharedPreferences.(String, T) -> T,
        private val setter: SharedPreferences.Editor.(String, T) -> SharedPreferences.Editor
    ) : ReadWriteProperty<Any, T> {

        override fun getValue(thisRef: Any, property: KProperty<*>): T {
            return preferences.getter(key ?: property.name, defaultValue)
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
            preferences.edit().setter(key ?: property.name, value).apply()
        }
    }

    companion object {
        @Volatile
        private var instance: PreferenceManager? = null

        fun getInstance(context: Context): PreferenceManager {
            return instance ?: synchronized(this) {
                instance ?: PreferenceManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
