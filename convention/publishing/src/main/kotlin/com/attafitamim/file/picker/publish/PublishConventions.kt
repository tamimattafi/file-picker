package com.attafitamim.file.picker.publish

import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPomDeveloper
import org.gradle.api.publish.maven.MavenPomLicense
import org.gradle.api.publish.maven.MavenPomScm

class PublishConventions : Plugin<Project> {

    private val version = "0.1.0-alpha13"
    private val group = "com.attafitamim.file.picker"

    override fun apply(project: Project) {
        project.plugins.apply("com.vanniktech.maven.publish")

        val mavenPublishing = project.extensions.getByName("mavenPublishing")
                as MavenPublishBaseExtension

        val artifact = project.fullName
        mavenPublishing.apply {
            coordinates(group, artifact, version)
            pom(MavenPom::configure)
            publishToMavenCentral(
                com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL,
                automaticRelease = true
            )
            signAllPublications()
        }
    }
}

private val Project.fullName: String
    get() {
        val names = ArrayList<String>()
        names.add(name)

        var currentProject = this
        while (currentProject.parent != null && currentProject.parent != rootProject) {
            val parentProject = currentProject.parent!!
            names.add(parentProject.name)
            currentProject = parentProject
        }

        return names.reversed().joinToString("-")
    }

private fun MavenPom.configure() {
    name.set("file-picker")
    description.set("file-picker for Kotlin Multiplatform")
    url.set("https://github.com/tamimattafi/file-picker")

    licenses { licenseSpec ->
        licenseSpec.license(MavenPomLicense::configure)
    }

    developers { developerSpec ->
        developerSpec.developer(MavenPomDeveloper::configure)
    }

    scm(MavenPomScm::configure)
}

private fun MavenPomLicense.configure() {
    name.set("Apache License 2.0")
    url.set("https://github.com/tamimattafi/file-picker/blob/main/LICENSE")
}

private fun MavenPomDeveloper.configure() {
    id.set("attafitamim")
    name.set("Tamim Attafi")
    email.set("attafitamim@gmail.com")
}

private fun MavenPomScm.configure() {
    connection.set("scm:git:github.com/tamimattafi/file-picker.git")
    developerConnection.set("scm:git:ssh://github.com/tamimattafi/file-picker.git")
    url.set("https://github.com/tamimattafi/file-picker/tree/main")
}
