import java.util.Properties
import java.io.FileInputStream
import java.io.ByteArrayOutputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
}

fun getVersionCode(): Int {
    val stdout = ByteArrayOutputStream()
    exec {
        this.commandLine = listOf("git", "tag", "--list")
        this.standardOutput = stdout
    }
    return stdout.toString().split("\n").count { it.isNotBlank() } + 1800
}

fun getVersionName(): String {
    val stdout = ByteArrayOutputStream()
    exec {
        this.commandLine = listOf("git", "describe", "--tags", "--dirty", "--always")
        this.standardOutput = stdout
    }
    return stdout.toString().trim()
}

@Suppress("UnstableApiUsage")
android {
    namespace = "igrek.todotree"
    compileSdk = 34
    defaultConfig {
        applicationId = "igrek.todotree"
        minSdk = 25
        targetSdk = 34 // Android 14
        versionCode = getVersionCode()
        versionName = getVersionName()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
        vectorDrawables.useSupportLibrary = true
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
        getByName("debug") {
            isDebuggable = true
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            buildConfigField("java.util.Date", "BUILD_DATE", "new java.util.Date(" + System.currentTimeMillis() + "L)")
        }
        register("prerelease") {
            isDebuggable = true
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf(
            "-Xallow-result-return-type",
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=kotlinx.coroutines.DelicateCoroutinesApi",
        )
    }
    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4" // based on https://developer.android.com/jetpack/androidx/releases/compose-kotlin
    }
    configurations.all {
        resolutionStrategy {
            force("com.google.code.findbugs:jsr305:1.3.9")
        }
    }
    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/license.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
            excludes += "META-INF/notice.txt"
            excludes += "META-INF/ASL2.0"
        }
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
    // Android
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.annotation:annotation:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.preference:preference-ktx:1.2.1") // Settings layouts
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("androidx.activity:activity-ktx:1.8.1")
    implementation("androidx.work:work-runtime:2.8.1")
    // Kotlin
    val kotlinVersion = rootProject.extra.get("kotlin_version") as String
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
    // Material Design Components
    implementation("com.google.android.material:material:1.10.0")
    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.material3:material3:1.1.2") // Material Design 3
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.activity:activity-compose:1.8.1") // Integration with activities
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.ui:ui-tooling-preview")
    // Guava
    implementation("com.google.guava:guava:32.1.3-android")
    // YAML
    implementation("com.charleskorn.kaml:kaml:0.56.0")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.16.0")
    // RX
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation("io.reactivex.rxjava2:rxkotlin:2.4.0")
    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    // Joda Time
    implementation("joda-time:joda-time:2.12.5")
    implementation("org.joda:joda-convert:2.2.3")
    implementation(files("libs/explosionfield-1.0.1.aar"))
    // Unit tests
    testImplementation("androidx.appcompat:appcompat:1.6.1")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}
