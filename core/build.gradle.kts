plugins {
    id(libs.plugins.convention.multiplatform.get().pluginId)
    id(libs.plugins.convention.publish.get().pluginId)
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                // Add your dependencies here
            }
        }
    }
}