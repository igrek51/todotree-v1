import java.util.Properties
import java.io.FileInputStream
import java.io.ByteArrayOutputStream

plugins {
//    kotlin("jvm")
    id("com.android.application")
    kotlin("android")
//    kotlin("kapt") version "1.9.20"
    kotlin("plugin.serialization") //version "1.9.0"
}

fun getVersionCode(): Int {
    val stdout = ByteArrayOutputStream()
    exec {
        this.commandLine = listOf("git", "tag", "--list")
        this.standardOutput = stdout
    }
    return stdout.toString().split("\n").size + 1800
}

fun getVersionName(): String {
    val stdout = ByteArrayOutputStream()
    exec {
        this.commandLine = listOf("git", "describe", "--tags", "--dirty", "--always")
        this.standardOutput = stdout
    }
    return stdout.toString().trim()
}

android {
    namespace = "igrek.todotree"
    compileSdkVersion(33)
    defaultConfig {
        applicationId = "igrek.todotree"
        minSdk = 25
        targetSdk = 33
        versionCode = getVersionCode()
        versionName = getVersionName()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
    }
    signingConfigs {
        create("release") {
            val propsFile = rootProject.file(".keystore.properties")
            if (propsFile.exists()) {
                val props = Properties()
                props.load(FileInputStream(propsFile))
                storeFile = file(props["storeFile"] as String)
                storePassword = props["storePassword"] as String
                keyAlias = props["keyAlias"] as String
                keyPassword = props["keyPassword"] as String
            }
        }
    }
    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            buildConfigField("java.util.Date", "BUILD_DATE", "new java.util.Date(" + System.currentTimeMillis() + "L)")
        }
        register("prerelease") {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            buildConfigField("java.util.Date", "BUILD_DATE", "new java.util.Date(" + System.currentTimeMillis() + "L)")
            signingConfig = signingConfigs.getByName("debug")
        }
        getByName("release") {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            buildConfigField("java.util.Date", "BUILD_DATE", "new java.util.Date(" + System.currentTimeMillis() + "L)")
            signingConfig = signingConfigs.getByName("release")
        }
    }
    packagingOptions {
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/LICENSE")
        exclude("META-INF/LICENSE.txt")
        exclude("META-INF/license.txt")
        exclude("META-INF/NOTICE")
        exclude("META-INF/NOTICE.txt")
        exclude("META-INF/notice.txt")
        exclude("META-INF/ASL2.0")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = listOf(
            "-Xallow-result-return-type",
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=kotlinx.coroutines.DelicateCoroutinesApi",
        )
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.8" // based on https://developer.android.com/jetpack/androidx/releases/compose-kotlin
    }
    configurations.all {
        resolutionStrategy {
            force("com.google.code.findbugs:jsr305:1.3.9")
        }
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
    // Android
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.annotation:annotation:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.legacy:legacy-preference-v14:1.0.0")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("androidx.activity:activity-ktx:1.7.2")
    implementation("androidx.work:work-runtime:2.8.1")
    // Kotlin
    val kotlinVersion = rootProject.extra.get("kotlin_version") as String
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:1.0-M1-1.4.0-rc")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
    // Material Design Components
    implementation("com.google.android.material:material:1.9.0")
    // Jetpack Compose
    val composeBom = platform("androidx.compose:compose-bom:2023.06.01")
    implementation(composeBom)
    implementation("androidx.compose.material3:material3:1.1.2") // Material Design 3
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.activity:activity-compose:1.7.2") // Integration with activities
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.ui:ui-tooling-preview")
    // Guava
    implementation("com.google.guava:guava:31.1-android")
    // GSON
    implementation("com.google.code.gson:gson:2.9.0")
    // RX
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation("io.reactivex.rxjava2:rxkotlin:2.4.0")
    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:3.12.12") // last version supporting API 1
    implementation("com.squareup.okhttp3:logging-interceptor:3.12.12")
    // Joda Time
    implementation("joda-time:joda-time:2.12.5")
    implementation("org.joda:joda-convert:2.2.3")
    implementation(files("libs/explosionfield-1.0.1.aar"))
    // Unit tests
    testImplementation("androidx.appcompat:appcompat:1.6.1")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:2.28.2")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
}
