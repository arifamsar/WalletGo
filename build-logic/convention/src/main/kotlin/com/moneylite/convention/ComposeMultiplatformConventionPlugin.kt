package com.moneylite.convention

import com.moneylite.convention.utils.alias
import com.moneylite.convention.utils.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

class ComposeMultiplatformConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                alias(libs.plugins.compose.multiplatform)
                alias(libs.plugins.compose.compiler)
            }
            extensions.configure(ComposeCompilerGradlePluginExtension::class.java) {
                stabilityConfigurationFiles.add(
                    rootProject.layout.projectDirectory.file("compose_stability.conf")
                )
            }
        }
    }
}
