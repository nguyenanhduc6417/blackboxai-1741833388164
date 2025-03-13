package com.example.myapp.utils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResourceProvider @Inject constructor(private val context: Context) {

    fun getString(@StringRes resId: Int): String {
        return context.getString(resId)
    }

    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String {
        return context.getString(resId, *formatArgs)
    }

    fun getQuantityString(@PluralsRes resId: Int, quantity: Int, vararg formatArgs: Any): String {
        return context.resources.getQuantityString(resId, quantity, *formatArgs)
    }

    fun getColor(@ColorRes resId: Int): Int {
        return ContextCompat.getColor(context, resId)
    }

    fun getColorStateList(@ColorRes resId: Int): ColorStateList? {
        return ContextCompat.getColorStateList(context, resId)
    }

    fun getDimension(@DimenRes resId: Int): Float {
        return context.resources.getDimension(resId)
    }

    fun getDimensionPixelSize(@DimenRes resId: Int): Int {
        return context.resources.getDimensionPixelSize(resId)
    }

    fun getDrawable(@DrawableRes resId: Int): Drawable? {
        return ContextCompat.getDrawable(context, resId)
    }

    companion object {
        @Volatile
        private var instance: ResourceProvider? = null

        fun getInstance(context: Context): ResourceProvider {
            return instance ?: synchronized(this) {
                instance ?: ResourceProvider(context.applicationContext).also { instance = it }
            }
        }
    }
}

// Extension functions for easy access
fun Context.resourceProvider(): ResourceProvider = ResourceProvider.getInstance(this)
