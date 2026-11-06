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

android {
    namespace = "com.treesofparis.core.data"
    buildFeatures.buildConfig = true
    defaultConfig {
        buildConfigField("int", "TREES_DB_VERSION", treesDbVersion.get())
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(projects.core.domain)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
}
