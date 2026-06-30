import java.util.Properties
import java.io.File

plugins {
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.moneylite"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.moneylite"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    signingConfigs {
        create("release") {
            val localProperties = Properties().apply {
                val file = rootProject.file("local.properties")
                if (file.exists()) {
                    file.inputStream().use { load(it) }
                }
            }
            val storeFileVal = (localProperties.getProperty("signing.storeFilePath") ?: System.getenv("SIGNING_STORE_FILE"))?.trim()?.let { path ->
                val f = file(path)
                if (f.exists()) f else rootProject.file(path)
            }
            val storePasswordVal = (localProperties.getProperty("signing.storePassword") ?: System.getenv("SIGNING_STORE_PASSWORD"))?.trim()
            val keyAliasVal = (localProperties.getProperty("signing.keyAlias") ?: System.getenv("SIGNING_KEY_ALIAS"))?.trim()
            val keyPasswordVal = (localProperties.getProperty("signing.keyPassword") ?: System.getenv("SIGNING_KEY_PASSWORD"))?.trim()

            if (storeFileVal != null && storeFileVal.exists() && storePasswordVal != null && keyAliasVal != null && keyPasswordVal != null) {
                storeFile = storeFileVal
                storePassword = storePasswordVal
                keyAlias = keyAliasVal
                keyPassword = keyPasswordVal
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            val releaseConfig = signingConfigs.getByName("release")
            if (releaseConfig.storeFile != null) {
                signingConfig = releaseConfig
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}


dependencies {
    implementation(project(":sharedUI"))
    implementation(libs.androidx.activityCompose)
}
