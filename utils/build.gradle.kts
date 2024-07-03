plugins {
    id(libs.plugins.convention.multiplatform.get().pluginId)
    id(libs.plugins.convention.publish.get().pluginId)
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                // Coroutines
                api(libs.kotlin.coroutines.core)

                // Date
                api(libs.kotlin.datetime)
            }
        }
    }
}