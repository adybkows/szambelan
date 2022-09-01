package pl.coopsoft.szambelan

import android.content.Context
import pl.coopsoft.szambelan.utils.FormattingUtils

object Persistence {
    private const val PREFERENCES_NAME = "preferences"

    const val PREF_USER_EMAIL = "user_email"
    const val PREF_EMPTY_ACTIONS = "empty_actions"
    const val PREF_OLD_MAIN = "old_main"
    const val PREF_OLD_GARDEN = "old_garden"
    const val PREF_CURRENT_MAIN = "current_main"
    const val PREF_CURRENT_GARDEN = "current_garden"

    private fun getPrefs(context: Context) =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun getString(context: Context, key: String, defValue: String = FormattingUtils.toString(0.0)) =
        getPrefs(context).getString(key, defValue) ?: defValue

    fun putString(context: Context, key: String, value: String) =
        getPrefs(context).edit().putString(key, value).apply()

    fun getDouble(context: Context, key: String, defValue: Double = 0.0) =
        try {
            getPrefs(context).getFloat(key, defValue.toFloat()).toDouble()
        } catch (e: ClassCastException) {
            FormattingUtils.toDouble(getString(context, key, defValue.toString()))
        }

    fun putDouble(context: Context, key: String, value: Double) =
        getPrefs(context).edit().remove(key).putFloat(key, value.toFloat()).apply()
}