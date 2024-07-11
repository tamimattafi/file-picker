plugins {
    id(libs.plugins.convention.multiplatform.get().pluginId)
    id(libs.plugins.convention.publish.get().pluginId)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.utils)
            implementation(projects.helpers)

            // Compose
            implementation(compose.ui)
            implementation(compose.runtime)
            implementation(compose.foundation)

            // Coil
            implementation(libs.coil)
        }

        androidMain.dependencies {
            // Camera
            implementation(libs.androidx.camera.core)
            implementation(libs.androidx.camera.video)
            implementation(libs.androidx.camera.lifecycle)
            implementation(libs.androidx.camera.view)
            implementation(libs.androidx.camera.extensions)
        }
    }
}