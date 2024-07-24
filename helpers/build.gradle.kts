plugins {
    id(libs.plugins.convention.multiplatform.get().pluginId)
    id(libs.plugins.convention.publish.get().pluginId)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.utils)

            // Compose
            implementation(compose.ui)
            implementation(compose.runtime)
            implementation(compose.foundation)

            // Coil
            implementation(libs.coil)
        }

        androidMain.dependencies {
            // Coil
            implementation(libs.coil.video)
        }
    }
}