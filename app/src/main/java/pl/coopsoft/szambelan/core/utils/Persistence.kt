package pl.coopsoft.szambelan.core.utils

import android.content.Context
import javax.inject.Inject

class Persistence @Inject constructor(
    private val formattingUtils: FormattingUtils
) {

    companion object {
        private const val PREFERENCES_NAME = "preferences"

        const val PREF_CURRENT_GARDEN = "current_garden"
        const val PREF_CURRENT_MAIN = "current_main"
        const val PREF_EMPTY_ACTIONS = "empty_actions"
        const val PREF_OLD_GARDEN = "old_garden"
        const val PREF_OLD_MAIN = "old_main"
        const val PREF_THEME_MODE = "theme_mode"
        const val PREF_USER_EMAIL = "user_email"
    }

    private fun getPrefs(context: Context) =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun getString(context: Context, key: String, defValue: String = formattingUtils.toString(0.0)) =
        getPrefs(context).getString(key, defValue) ?: defValue

    fun putString(context: Context, key: String, value: String) =
        getPrefs(context).edit().putString(key, value).apply()

    fun getDouble(context: Context, key: String, defValue: Double = 0.0) =
        try {
            getPrefs(context).getFloat(key, defValue.toFloat()).toDouble()
        } catch (e: ClassCastException) {
            formattingUtils.toDouble(getString(context, key, defValue.toString()))
        }

    fun putDouble(context: Context, key: String, value: Double) =
        getPrefs(context).edit().remove(key).putFloat(key, value.toFloat()).apply()

    fun getInt(context: Context, key: String, defValue: Int) =
        getPrefs(context).getInt(key, defValue)

    fun putInt(context: Context, key: String, value: Int) =
        getPrefs(context).edit().putInt(key, value).apply()
}