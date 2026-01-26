plugins {
    alias(libs.plugins.template.feature.convention)
}

kotlin {
    androidLibrary {
        namespace = "com.example.template.features.transactions"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
    }
}
