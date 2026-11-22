package net.igng.bbs.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import net.igng.bbs.core.model.AppPreferences
import net.igng.bbs.core.model.LocalReadingHistoryEntry
import net.igng.bbs.core.model.MotionIntensity
import net.igng.bbs.core.model.ReaderTypographyPrefs
import net.igng.bbs.core.model.ThemeMode
import net.igng.bbs.core.network.IgngJson
import java.io.File

private val Context.bbsDataStore: DataStore<Preferences> by preferencesDataStore(name = "igngbbs_v2_prefs")

@Serializable
data class LocalEditorDraft(
    val postId: String? = null,
    val title: String = "",
    val content: String = "",
    val summary: String = "",
    val tags: List<String> = emptyList(),
    val categoryId: String? = null,
    val columnId: String? = null,
    val visibility: String = "public",
    val format: String = "markdown",
    val updatedAtMs: Long = System.currentTimeMillis(),
)

class SettingsRepository(context: Context) {
    private val appContext = context.applicationContext
    private val cacheDir = File(appContext.cacheDir, "igngbbs_swr_cache").apply { mkdirs() }
    private val json = IgngJson.instance

    val preferences: Flow<AppPreferences> = appContext.bbsDataStore.data.map { prefs ->
        prefs[KEY_PREFS]?.let { raw ->
            runCatching { json.decodeFromString(AppPreferences.serializer(), raw) }.getOrNull()
        } ?: AppPreferences()
    }

    val readingHistory: Flow<List<LocalReadingHistoryEntry>> = appContext.bbsDataStore.data.map { prefs ->
        prefs[KEY_READING_HISTORY]?.let { raw ->
            runCatching {
                json.decodeFromString(ListSerializer(LocalReadingHistoryEntry.serializer()), raw)
            }.getOrNull()
        }.orEmpty()
    }

    suspend fun updatePreferences(transform: (AppPreferences) -> AppPreferences) {
        appContext.bbsDataStore.edit { prefs ->
            val current = prefs[KEY_PREFS]?.let {
                runCatching { json.decodeFromString(AppPreferences.serializer(), it) }.getOrNull()
            } ?: AppPreferences()
            val updated = transform(current)
            prefs[KEY_PREFS] = json.encodeToString(AppPreferences.serializer(), updated)
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) = updatePreferences { it.copy(themeMode = mode) }
    suspend fun setMotionIntensity(intensity: MotionIntensity) = updatePreferences { it.copy(motionIntensity = intensity) }
    suspend fun setHapticsEnabled(enabled: Boolean) = updatePreferences { it.copy(hapticsEnabled = enabled) }
    suspend fun setCollapseSeriesInFeed(enabled: Boolean) = updatePreferences { it.copy(collapseSeriesInFeed = enabled) }
    suspend fun setBackgroundNotificationsEnabled(enabled: Boolean) =
        updatePreferences { it.copy(backgroundNotificationsEnabled = enabled) }
    suspend fun updateReaderPrefs(transform: (ReaderTypographyPrefs) -> ReaderTypographyPrefs) =
        updatePreferences { it.copy(reader = transform(it.reader)) }

    suspend fun recordReadingProgress(entry: LocalReadingHistoryEntry) {
        appContext.bbsDataStore.edit { prefs ->
            val current = prefs[KEY_READING_HISTORY]?.let {
                runCatching {
                    json.decodeFromString(ListSerializer(LocalReadingHistoryEntry.serializer()), it)
                }.getOrNull()
            }.orEmpty()
            val updated = (listOf(entry.copy(visitedAtEpochMs = System.currentTimeMillis())) +
                current.filterNot { it.postId == entry.postId }).take(60)
            prefs[KEY_READING_HISTORY] = json.encodeToString(
                ListSerializer(LocalReadingHistoryEntry.serializer()),
                updated,
            )
        }
    }

    suspend fun clearReadingHistory() {
        appContext.bbsDataStore.edit { it.remove(KEY_READING_HISTORY) }
    }

    suspend fun saveEditorDraft(key: String, draft: LocalEditorDraft) {
        appContext.bbsDataStore.edit { prefs ->
            val map = decodeDraftMap(prefs[KEY_EDITOR_DRAFTS]).toMutableMap()
            map[key] = draft.copy(updatedAtMs = System.currentTimeMillis())
            prefs[KEY_EDITOR_DRAFTS] = json.encodeToString(
                MapSerializer(String.serializer(), LocalEditorDraft.serializer()),
                map,
            )
        }
    }

    suspend fun loadEditorDraft(key: String): LocalEditorDraft? {
        val raw = appContext.bbsDataStore.data.first()[KEY_EDITOR_DRAFTS]
        return decodeDraftMap(raw)[key]
    }

    suspend fun clearEditorDraft(key: String) {
        appContext.bbsDataStore.edit { prefs ->
            val map = decodeDraftMap(prefs[KEY_EDITOR_DRAFTS]).toMutableMap()
            map.remove(key)
            prefs[KEY_EDITOR_DRAFTS] = json.encodeToString(
                MapSerializer(String.serializer(), LocalEditorDraft.serializer()),
                map,
            )
        }
    }

    suspend fun getLastNotifiedInboxId(userId: Long): String? {
        val raw = appContext.bbsDataStore.data.first()[KEY_INBOX_CURSORS]
        return decodeStringMap(raw)[userId.toString()]
    }

    suspend fun setLastNotifiedInboxId(userId: Long, itemId: String) {
        appContext.bbsDataStore.edit { prefs ->
            val map = decodeStringMap(prefs[KEY_INBOX_CURSORS]).toMutableMap()
            map[userId.toString()] = itemId
            prefs[KEY_INBOX_CURSORS] = json.encodeToString(
                MapSerializer(String.serializer(), String.serializer()),
                map,
            )
        }
    }

    suspend fun getLastDailyLoginPeriod(userId: Long): String? {
        val raw = appContext.bbsDataStore.data.first()[KEY_DAILY_LOGIN_PERIODS]
        return decodeStringMap(raw)[userId.toString()]
    }

    suspend fun markDailyLoginPeriod(userId: Long, period: String) {
        appContext.bbsDataStore.edit { prefs ->
            val map = decodeStringMap(prefs[KEY_DAILY_LOGIN_PERIODS]).toMutableMap()
            map[userId.toString()] = period
            prefs[KEY_DAILY_LOGIN_PERIODS] = json.encodeToString(
                MapSerializer(String.serializer(), String.serializer()),
                map,
            )
        }
    }

    suspend fun writeCache(key: String, payloadJson: String) = withContext(Dispatchers.IO) {
        runCatching {
            File(cacheDir, "${sanitizeKey(key)}.json").writeText(payloadJson)
        }
    }

    suspend fun readCache(key: String): String? = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(cacheDir, "${sanitizeKey(key)}.json")
            if (file.exists()) file.readText() else null
        }.getOrNull()
    }

    suspend fun clearLocalCache() = withContext(Dispatchers.IO) {
        runCatching {
            cacheDir.listFiles()?.forEach { it.delete() }
        }
    }

    private fun decodeDraftMap(raw: String?): Map<String, LocalEditorDraft> = raw?.let {
        runCatching {
            json.decodeFromString(MapSerializer(String.serializer(), LocalEditorDraft.serializer()), it)
        }.getOrNull()
    }.orEmpty()

    private fun decodeStringMap(raw: String?): Map<String, String> = raw?.let {
        runCatching {
            json.decodeFromString(MapSerializer(String.serializer(), String.serializer()), it)
        }.getOrNull()
    }.orEmpty()

    private fun sanitizeKey(key: String): String = key.replace(Regex("[^a-zA-Z0-9_-]"), "_")

    companion object {
        private val KEY_PREFS = stringPreferencesKey("app_preferences_v2")
        private val KEY_READING_HISTORY = stringPreferencesKey("reading_history_v2")
        private val KEY_EDITOR_DRAFTS = stringPreferencesKey("editor_drafts_v2")
        private val KEY_INBOX_CURSORS = stringPreferencesKey("inbox_cursors_v2")
        private val KEY_DAILY_LOGIN_PERIODS = stringPreferencesKey("daily_login_periods_v2")
    }
}
