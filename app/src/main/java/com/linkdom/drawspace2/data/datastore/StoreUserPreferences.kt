package com.linkdom.drawspace2.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

class StoreUserPreferences(
    private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "UserSettings"
        private val Context.dataStore by preferencesDataStore(PREFS_NAME)
        val PREFS_KEY_IS_SHOW_STATUS = booleanPreferencesKey("IsShowStatus_KEY")
        val PREFS_KEY_IS_SHOW_BOTTOM_BUTTON = booleanPreferencesKey("IsShowBottomButton_KEY")
        val PREFS_KEY_IS_SHOW_INFO =  booleanPreferencesKey("IsShowInfo_KEY")
    }

    val getUserPreferences = context.dataStore.data
        .map { preferences ->
            val isShowStatus = preferences[PREFS_KEY_IS_SHOW_STATUS] ?: false
            val isShowBottomButton = preferences[PREFS_KEY_IS_SHOW_BOTTOM_BUTTON] ?: false
            val isShowInfo = preferences[PREFS_KEY_IS_SHOW_INFO] ?: false
            UserPreferences(isShowStatus, isShowBottomButton, isShowInfo)
        }

    suspend fun saveUserPreferences(userPreferences: UserPreferences) {
        context.dataStore.edit { preferences ->
            preferences[PREFS_KEY_IS_SHOW_STATUS] = userPreferences.isShowStatus
            preferences[PREFS_KEY_IS_SHOW_BOTTOM_BUTTON] = userPreferences.isShowBottomButton
            preferences[PREFS_KEY_IS_SHOW_INFO] = userPreferences.isShowInfo
        }
    }
}

