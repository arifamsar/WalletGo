plugins {
    alias(libs.plugins.template.feature.convention)
}

kotlin {
    androidLibrary {
        namespace = "com.moneylite.features.transactions"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
    }

    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
