# Kotlin Library Template
Kotlin Multiplatform library template for quick-starts without dealing with boilerplate code

## Introductions
It covers the following boilerplate

1. Publication to local and remote maven repository
2. Multiplatform module conventions
3. Gradle files and build scripts
4. Version catalog
5. Popular gitignore settings
6. GitHub release actions script
7. Sample shared module

## Usage
To use this template all you have to do is to follow these steps:

1. Download a zip file or clone this repository (Don't forget to change git remote settings)
2. Replace `com.attafitamim.kalib` with your package everywhere in the project
3. Replace `file-picker` with your library name everywhere in the project
4. Adjust targets in `MultiplatformConventions.kt`
5. Adjust publishing information in `PublishConventions.kt`
6. Update `README.md` file

That's it! Now you can add your modules, dependencies, adjust targets and start coding!

## Maven central
To publish your library to maven central you need to follow these steps:

1. Crate an account on [central portal](https://central.sonatype.com/)
2. Create a namespace with your package as shown in these [docs](https://central.sonatype.org/register/namespace/)
3. Generate a token as shown [here](https://central.sonatype.org/publish/generate-portal-token/)
4. Generate gpg key using your sonatype email, upload it, then confirm your email using the received link in your inbox
5. Add secrets for publishing as shown [here](https://vanniktech.github.io/gradle-maven-publish-plugin/central/#secrets)
6. Don't forget to add secrets as repository secrets for GitHub actions as shown [here](https://docs.github.com/en/actions/security-guides/using-secrets-in-github-actions)

To publish your artifacts to central portal you can either run `./gradlew publishAllPublicationsToMavenCentralRepository` locally or create a GitHub release

> [!IMPORTANT]  
> Don't forget to change publishing version in `PublishConventions.kt`

## Future plans
1. Upgrade to Kotlin 2.0.0
2. Add testing examples and GitHub actions for the matter
3. Add lint example and GitHub actions for the matter
