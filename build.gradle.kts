val kotlinVersion = "1.9.20"
extra["kotlin_version"] = kotlinVersion

plugins {
    id("com.android.application") version "8.1.4" apply false
    id("com.android.library") version "8.1.4" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.20" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

tasks.create<Delete>("clean") {
    delete(rootProject.buildDir)
}
