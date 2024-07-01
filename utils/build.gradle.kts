plugins {
    id(libs.plugins.convention.multiplatform.get().pluginId)
    id(libs.plugins.convention.publish.get().pluginId)
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.core.data)
                api(projects.core.domain)

                // Coroutines
                api(libs.kotlin.coroutines.core)
            }
        }
    }
}