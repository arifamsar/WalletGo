plugins {
    alias(libs.plugins.template.feature.convention)
}

kotlin {
    androidLibrary {
        namespace = "com.example.template.features.schedule"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
    }
}
