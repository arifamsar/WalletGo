package com.example.template.convention

import com.example.template.convention.utils.alias
import com.example.template.convention.utils.libs
import org.gradle.api.Plugin
import org.gradle.api.Project

class ComposeMultiplatformConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                alias(libs.plugins.compose.multiplatform)
                alias(libs.plugins.compose.compiler)
            }
        }
    }
}
