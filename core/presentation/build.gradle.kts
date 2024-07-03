plugins {
    id(libs.plugins.convention.multiplatform.get().pluginId)
    id(libs.plugins.convention.publish.get().pluginId)
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.core.domain)
                api(projects.utils)

                // Coroutines
                implementation(libs.kotlin.coroutines.core)

                // VM
                implementation(libs.androidx.lifecycle.view.model)
                implementation(libs.orbit.core)
                implementation(libs.orbit.view.model)
            }
        }
    }
}
