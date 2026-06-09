import org.gradle.initialization.DependenciesAccessors
import org.gradle.kotlin.dsl.support.serviceOf

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

group = "com.moneylite.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.compose.compiler.gradlePlugin)
    compileOnly(libs.room.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    gradle.serviceOf<DependenciesAccessors>().classes.asFiles.forEach {
        compileOnly(files(it.absolutePath))
    }
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        register("kotlin-multiplatform") {
            id = "template.kotlin.multiplatform"
            implementationClass = "com.moneylite.convention.KotlinMultiplatformConventionPlugin"
        }
        register("compose-multiplatform") {
            id = "template.compose.multiplatform"
            implementationClass = "com.moneylite.convention.ComposeMultiplatformConventionPlugin"
        }
        register("room") {
            id = "template.room"
            implementationClass = "com.moneylite.convention.RoomConventionPlugin"
        }
        register("navigation") {
            id = "template.navigation.convention"
            implementationClass = "com.moneylite.convention.NavigationConventionPlugin"
        }
        register("feature") {
            id = "template.feature.convention"
            implementationClass = "com.moneylite.convention.FeatureConventionPlugin"
        }
    }
}
