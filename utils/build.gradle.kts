plugins {
    id(libs.plugins.convention.multiplatform.get().pluginId)
    id(libs.plugins.convention.publish.get().pluginId)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.core)
                implementation(projects.core.utils)

                // Compose
                implementation(compose.ui)
                implementation(compose.runtime)
                implementation(compose.foundation)
            }
        }
    }
}