plugins {
    id(libs.plugins.convention.multiplatform.get().pluginId)
    id(libs.plugins.convention.publish.get().pluginId)
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.core.domain)
                api(projects.core.data)
                implementation(projects.core.utils)
            }
        }
    }
}