package com.attafitamim.file.picker.multiplatform

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension

class MultiplatformConventions : Plugin<Project> {
  override fun apply(project: Project) {
    project.plugins.apply {
      apply("org.jetbrains.kotlin.multiplatform")
      apply("com.android.library")
    }

    val extension = project.kotlinExtension as KotlinMultiplatformExtension
    extension.apply {
      applyDefaultHierarchyTemplate()
      jvmToolchain(17)
      
      androidTarget {
        compilations.all {
          it.kotlinOptions {
            jvmTarget = "17"
          }
        }
      }
      
      iosX64()
      iosArm64()
      iosSimulatorArm64()

      // TODO: add support for these targets in the future, keep them here for now
      jvm()
      js {
        browser()
      }
    }

    val androidExtension = project.extensions.getByName("android") as LibraryExtension
    androidExtension.apply {
      namespace = "com.attafitamim.file.picker.${project.name}"
      compileSdk = 34

      defaultConfig {
        minSdk = 24
      }

      compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
      }
    }
  }
}
