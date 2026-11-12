import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

/** Compiles with the JDK 25 toolchain while emitting bytecode for [target]. */
internal fun Project.configureKotlin(target: JvmTarget) {
    extensions.configure<KotlinBaseExtension> { jvmToolchain(TOOLCHAIN_JDK) }
    tasks.withType<KotlinJvmCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(target)
            allWarningsAsErrors.set(true)
        }
    }
}

internal fun Project.configureTestDependencies() {
    dependencies.apply {
        add("testImplementation", lib("junit"))
        add("testImplementation", lib("mockk"))
        add("testImplementation", lib("turbine"))
        add("testImplementation", lib("kotlinx-coroutines-test"))
    }
}

/** Robolectric reaches into JDK internals, which JDK 17+ hides unless exported. */
internal fun Project.configureRobolectricJvmArgs() {
    tasks.withType<org.gradle.api.tasks.testing.Test>().configureEach {
        jvmArgs(
            "--add-exports=java.base/jdk.internal.access=ALL-UNNAMED",
            "--add-opens=java.base/java.lang=ALL-UNNAMED",
            "--add-opens=java.base/java.io=ALL-UNNAMED",
            "--add-opens=java.base/java.util=ALL-UNNAMED",
        )
    }
}

/** Robolectric does not support SDK 37 yet; one shared robolectric.properties pins the SDK it runs. */
internal fun Project.configureRobolectricSdk() {
    extensions.configure<com.android.build.api.dsl.CommonExtension> {
        sourceSets.getByName("test").resources.srcDir(rootProject.file("config/robolectric"))
    }
}
