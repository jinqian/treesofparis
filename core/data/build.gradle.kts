plugins {
    id("trees.android.library")
    id("trees.android.hilt")
    alias(libs.plugins.room)
}

/** Version of the pre-packaged tree data. Bumped by data/scripts/update_trees.py --bump-version. */
val treesDbVersion =
    providers.fileContents(layout.projectDirectory.file("trees-db-version.txt")).asText.map {
        it.trim()
    }

/** Which extraction the asset was built from; written by data/scripts/update_trees.py. */
@Suppress("UNCHECKED_CAST")
val treesDbMeta =
    providers.fileContents(layout.projectDirectory.file("trees-db-meta.json")).asText.map {
        groovy.json.JsonSlurper().parseText(it) as Map<String, Any?>
    }

android {
    namespace = "fr.arbresdeparis.core.data"
    buildFeatures.buildConfig = true
    defaultConfig {
        buildConfigField("int", "TREES_DB_VERSION", treesDbVersion.get())
        buildConfigField("String", "TREES_EXTRACTED_AT", "\"${treesDbMeta.get()["extractedAt"] ?: ""}\"")
        buildConfigField("int", "TREES_RECORDS_KEPT", "${treesDbMeta.get()["recordsKept"] ?: 0}")
    }
    // The exported schemas, for the user database's migration tests.
    sourceSets["test"].assets.srcDir("$projectDir/schemas")
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(projects.core.domain)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    testImplementation(libs.room.testing)
}
