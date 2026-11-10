import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")
            extensions.configure<ApplicationExtension> {
                compileSdk = COMPILE_SDK
                defaultConfig.minSdk = MIN_SDK
                defaultConfig.targetSdk = TARGET_SDK
                compileOptions.sourceCompatibility = JavaVersion.VERSION_17
                compileOptions.targetCompatibility = JavaVersion.VERSION_17
                testOptions.unitTests.isIncludeAndroidResources = true
            }
            configureKotlin(JvmTarget.JVM_17)
            configureTestDependencies()
            configureRobolectricJvmArgs()
            configureRobolectricSdk()
            dependencies.add("testImplementation", lib("robolectric"))
            dependencies.add("testImplementation", lib("androidx-test-core"))
        }
    }
}
