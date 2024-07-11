plugins {
    id(libs.plugins.convention.multiplatform.get().pluginId)
    id(libs.plugins.convention.publish.get().pluginId)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Coroutines
            implementation(libs.kotlin.coroutines.core)

            // Date
            implementation(libs.kotlin.datetime)
        }
    }
}