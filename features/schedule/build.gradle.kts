plugins {
    alias(libs.plugins.template.feature.convention)
}

kotlin {
    androidLibrary {
        namespace = "com.moneylite.features.schedule"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
    }
}
