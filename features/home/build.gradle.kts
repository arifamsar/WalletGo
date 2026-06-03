plugins {
    alias(libs.plugins.template.feature.convention)
}

kotlin {
    androidLibrary {
        namespace = "com.moneylite.features.home"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
    }
}
