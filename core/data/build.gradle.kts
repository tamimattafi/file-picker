plugins {
    id(libs.plugins.convention.multiplatform.get().pluginId)
    id(libs.plugins.convention.publish.get().pluginId)
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.core.domain)
                implementation(projects.core.utils)

                // Coroutines
                implementation(libs.kotlin.coroutines.core)
            }
        }

        val androidMain by getting {
            dependencies {
                // Android
                implementation(libs.androidx.core.ktx)
            }
        }
    }
}