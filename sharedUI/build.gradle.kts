import com.example.template.convention.utils.sharedUiDependencies

plugins {
    alias(libs.plugins.template.kotlin.multiplatform)
    alias(libs.plugins.template.compose.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {

    androidLibrary {
        namespace = "com.example.template.sharedui"
        compileSdk = libs.versions.android.compileSdk.get().toInt()

        androidResources.enable = true
    }

    sourceSets {
        commonMain.dependencies {
            sharedUiDependencies(project)
            api(projects.core.common)
            api(projects.core.domain)
            api(projects.core.data)
            api(projects.core.ui)
            api(projects.features.home)
            api(projects.features.profile)
            api(projects.features.schedule)
            api(projects.features.transactions)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.compose.ui.test)
            implementation(libs.kotlinx.coroutines.test)
        }

        androidMain.dependencies {
            implementation(libs.compose.ui.tooling)
        }
    }
}
