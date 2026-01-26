package com.example.template.convention

import com.example.template.convention.utils.alias
import com.example.template.convention.utils.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class FeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                alias(libs.plugins.template.kotlin.multiplatform)
                alias(libs.plugins.template.compose.multiplatform)
                alias(libs.plugins.template.navigation.convention)
            }

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.commonMain.dependencies {
                    implementation(project(":core:ui"))
                    implementation(project(":core:common"))
                    implementation(project(":core:domain"))
                    implementation(project(":core:data"))
                }
            }
        }
    }
}
