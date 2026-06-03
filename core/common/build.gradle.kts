plugins {
    alias(libs.plugins.template.kotlin.multiplatform)
}

kotlin {
    androidLibrary {
        namespace = "com.moneylite.core.common"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.datetime)
            api(libs.kotlinx.coroutines.core)
            api(libs.kermit)
            implementation(libs.koin.core)
        }
    }
}
