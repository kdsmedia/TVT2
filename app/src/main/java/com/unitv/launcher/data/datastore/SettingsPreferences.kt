package com.unitv.launcher.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsPreferences(private val context: Context) {

    companion object {
        val SHORTCUT_SLOT_1 = stringPreferencesKey("shortcut_slot_1")
        val SHORTCUT_SLOT_2 = stringPreferencesKey("shortcut_slot_2")
        val SHORTCUT_SLOT_3 = stringPreferencesKey("shortcut_slot_3")
        val WALLPAPER_URI = stringPreferencesKey("wallpaper_uri")
        val BANNER_URIS = stringPreferencesKey("banner_uris") // URIs separadas por vírgula para o banner
    }

    suspend fun saveShortcut(slot: Int, packageName: String) {
        context.dataStore.edit { preferences ->
            when (slot) {
                1 -> preferences[SHORTCUT_SLOT_1] = packageName
                2 -> preferences[SHORTCUT_SLOT_2] = packageName
                3 -> preferences[SHORTCUT_SLOT_3] = packageName
            }
        }
    }

    suspend fun saveWallpaper(uriString: String) {
        context.dataStore.edit { it[WALLPAPER_URI] = uriString }
    }

    suspend fun saveBannerUris(urisString: String) {
        context.dataStore.edit { it[BANNER_URIS] = urisString }
    }

    val shortcut1Flow: Flow<String?> = context.dataStore.data.map { it[SHORTCUT_SLOT_1] }
    val shortcut2Flow: Flow<String?> = context.dataStore.data.map { it[SHORTCUT_SLOT_2] }
    val shortcut3Flow: Flow<String?> = context.dataStore.data.map { it[SHORTCUT_SLOT_3] }
    val wallpaperUriFlow: Flow<String?> = context.dataStore.data.map { it[WALLPAPER_URI] }
    val bannerUrisFlow: Flow<String?> = context.dataStore.data.map { it[BANNER_URIS] }
}