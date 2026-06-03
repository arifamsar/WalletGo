package com.moneylite.core.data.service

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesOf
import com.moneylite.core.ui.theme.ThemeTemplate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class UserPreferencesThemeTemplateTest {

    @Test
    fun themeTemplateFlow_defaultsToDefaultWhenPreferenceIsMissing() = runTest {
        val userPreferences = UserPreferences(emptyDataStore())

        assertEquals(
            ThemeTemplate.Default,
            userPreferences.themeTemplateFlow().first()
        )
    }

    @Test
    fun setThemeTemplate_persistsSelectedTemplateId() = runTest {
        val userPreferences = UserPreferences(emptyDataStore())

        userPreferences.setThemeTemplate(ThemeTemplate.Strawberry)

        assertEquals(
            ThemeTemplate.Strawberry,
            userPreferences.themeTemplateFlow().first()
        )
    }

    @Test
    fun themeTemplateFlow_defaultsToDefaultWhenStoredIdIsUnknown() = runTest {
        val dataStore = dataStoreWithStoredThemeId("not_a_template")
        val userPreferences = UserPreferences(dataStore)

        assertEquals(
            ThemeTemplate.Default,
            userPreferences.themeTemplateFlow().first()
        )
    }

    private fun emptyDataStore(): DataStore<Preferences> =
        dataStoreWithStoredThemeId(null)

    private fun dataStoreWithStoredThemeId(id: String?): DataStore<Preferences> =
        InMemoryPreferencesDataStore(
            initialPreferences = if (id == null) {
                preferencesOf()
            } else {
                preferencesOf(UserPreferences.theme_template to id)
            }
        )
}

private class InMemoryPreferencesDataStore(
    initialPreferences: Preferences
) : DataStore<Preferences> {
    private val preferences = MutableStateFlow(initialPreferences)

    override val data: Flow<Preferences> = preferences

    override suspend fun updateData(
        transform: suspend (t: Preferences) -> Preferences
    ): Preferences {
        val updated = transform(preferences.value)
        preferences.value = updated
        return updated
    }
}
