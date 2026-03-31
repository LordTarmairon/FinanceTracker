package com.gorthaur.financetracker.core.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.gorthaur.financetracker.core.model.AppLanguage

object AppLanguageManager {
    fun applyLanguage(language: AppLanguage) {
        val locales = when (language) {
            AppLanguage.SYSTEM -> LocaleListCompat.getEmptyLocaleList()
            AppLanguage.ENGLISH -> LocaleListCompat.forLanguageTags("en")
            AppLanguage.SPANISH -> LocaleListCompat.forLanguageTags("es")
            AppLanguage.KOREAN -> LocaleListCompat.forLanguageTags("kr")
        }
        AppCompatDelegate.setApplicationLocales(locales)
    }
}