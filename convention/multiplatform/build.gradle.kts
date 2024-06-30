plugins {
    alias(libs.plugins.kotlin.jvm)
    id(libs.plugins.java.gradle.plugin.get().pluginId)
}

kotlin {
    jvmToolchain(17)
}

gradlePlugin {
    plugins.create("multiplatform") {
        id = "com.attafitamim.file.picker.multiplatform"
        implementationClass = "com.attafitamim.file.picker.multiplatform.MultiplatformConventions"
    }
}

dependencies {
    compileOnly(libs.kotlin.plugin)
    compileOnly(libs.android.build.tools)
}
