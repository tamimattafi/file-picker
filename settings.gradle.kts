enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenLocal()
        mavenCentral()
        google()
    }

    includeBuild("convention")
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
    }
}

rootProject.name = "file-picker"
include(":core")
include(":core:data")
include(":core:domain")
include(":core:utils")
include(":layout")

include(":sample:shared")
