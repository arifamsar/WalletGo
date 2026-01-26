import com.example.template.convention.utils.sharedUiDependencies

plugins {
    alias(libs.plugins.template.kotlin.multiplatform)
    alias(libs.plugins.template.compose.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    androidLibrary {
        namespace = "com.example.template.core.ui"
        compileSdk = libs.versions.android.compileSdk.get().toInt()

        androidResources.enable = true
    }

    sourceSets {
        commonMain.dependencies {
            sharedUiDependencies(project)
            implementation(project(":core:common"))
        }

        androidMain.dependencies {
            implementation(libs.compose.ui.tooling)
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.example.template.core.ui.generated.resources"
    generateResClass = always
}
