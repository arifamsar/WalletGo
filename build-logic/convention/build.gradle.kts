import org.gradle.initialization.DependenciesAccessors
import org.gradle.kotlin.dsl.support.serviceOf

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

group = "com.example.template.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
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
            implementationClass = "com.example.template.convention.KotlinMultiplatformConventionPlugin"
        }
        register("compose-multiplatform") {
            id = "template.compose.multiplatform"
            implementationClass = "com.example.template.convention.ComposeMultiplatformConventionPlugin"
        }
        register("room") {
            id = "template.room"
            implementationClass = "com.example.template.convention.RoomConventionPlugin"
        }
        register("navigation") {
            id = "template.navigation.convention"
            implementationClass = "com.example.template.convention.NavigationConventionPlugin"
        }
        register("feature") {
            id = "template.feature.convention"
            implementationClass = "com.example.template.convention.FeatureConventionPlugin"
        }
    }
}
