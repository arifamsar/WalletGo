import com.moneylite.convention.utils.sharedUiDependencies

plugins {
    alias(libs.plugins.template.kotlin.multiplatform)
    alias(libs.plugins.template.compose.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    androidLibrary {
        namespace = "com.moneylite.core.ui"
        compileSdk = libs.versions.android.compileSdk.get().toInt()

        androidResources.enable = true
    }

    sourceSets {
        commonMain.dependencies {
            sharedUiDependencies(project)
            implementation(project(":core:common"))
            implementation(project(":core:domain"))
            implementation(libs.composeIcons.tablerIcons)
            implementation(libs.composeIcons.octicons)
            implementation(libs.vico.compose)
            implementation(libs.vico.compose.m3)

        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        androidMain.dependencies {
            implementation(libs.compose.ui.tooling)
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.moneylite.core.ui.generated.resources"
    generateResClass = always
}
