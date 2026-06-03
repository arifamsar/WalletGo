plugins {
    alias(libs.plugins.template.feature.convention)
}

kotlin {
    androidLibrary {
        namespace = "com.moneylite.features.profile"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
    }
}
