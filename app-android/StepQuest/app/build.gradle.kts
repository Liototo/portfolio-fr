import java.util.Properties

plugins {
    id ("com.android.application")
    id ("org.jetbrains.kotlin.android")
    id("com.ncorti.ktfmt.gradle") version "0.16.0"
    id("com.google.gms.google-services")
    id("jacoco")
    id("org.sonarqube") version "4.4.1.3373"
}

sonar {
    properties {
        property("sonar.projectKey", "cs311-EPFL-StepQuest_ApplicationCode")
        property("sonar.organization", "cs311-epfl-stepquest")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.junit.reportPaths", "${project.layout.buildDirectory.get()}/test-results/testDebugunitTest/")
        property("sonar.androidLint.reportPaths", "${project.layout.buildDirectory.get()}/reports/lint-results-debug.xml")
        property("sonar.coverage.jacoco.xmlReportPaths", "${project.layout.buildDirectory.get()}/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
    }
}

android {
    namespace = "com.github.se.stepquest"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.github.se.stepquest"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        //load the values from .properties file
        val mapsKeyFile = project.rootProject.file("local.properties")
        val properties = Properties()
        properties.load(mapsKeyFile.inputStream())

        //fetch the map key
        val apiKey = properties.getProperty("MAPS_API_KEY") ?: ""
        var apiKey2=apiKey
        if (apiKey[0] != '\"'){
            apiKey2="\"" + apiKey + "\""
        }

        buildConfigField("String", "MAPS_API_KEY", apiKey2)

        //inject the key dynamically into the manifest
        manifestPlaceholders["GOOGLE_MAP_KEY"] = apiKey
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    packaging {
        resources {
            merges += "META-INF/LICENSE.md"
            merges += "META-INF/LICENSE-notice.md"
        }
    }
    testOptions {
        packagingOptions {
            jniLibs {
                useLegacyPackaging = true
            }
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.ui:ui:1.4.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.4.0")
    implementation("androidx.compose.material:material:1.1.1")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.navigation:navigation-compose:2.6.0-rc01")

    implementation("com.google.maps.android:maps-compose:4.3.0")
    implementation("com.google.maps.android:maps-compose-utils:4.3.0")

    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.5")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui-graphics")
    implementation("com.google.android.gms:play-services-auth:20.6.0")
    implementation("com.google.firebase:firebase-auth-ktx:22.3.0")

    implementation(libs.androidx.junit.ktx)
//    implementation("com.google.firebase:firebase-database-ktx:20.3.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.3.0")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.5")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.4.0")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    debugImplementation("androidx.compose.ui:ui-tooling:1.4.0")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.4.0")

    androidTestImplementation("com.kaspersky.android-components:kaspresso:1.4.3")
    // Allure support
    androidTestImplementation("com.kaspersky.android-components:kaspresso-allure-support:1.4.3")
    // Jetpack Compose support
    androidTestImplementation("com.kaspersky.android-components:kaspresso-compose-support:1.4.1")

    implementation("androidx.fragment:fragment:1.5.5")

    implementation("com.squareup.okhttp3:okhttp:3.10.0")

//    testImplementation("org.mockito:mockito-core:3.11.2")
    testImplementation("org.mockito:mockito-inline:2.13.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    implementation("com.google.firebase:firebase-database-ktx:20.3.0")
    androidTestImplementation("com.google.firebase:firebase-database-ktx:20.3.0")
    implementation("com.google.firebase:firebase-firestore:24.10.0")
    androidTestImplementation("com.google.firebase:firebase-firestore:24.10.0")

    implementation("com.firebaseui:firebase-ui-auth:7.2.0")
    testImplementation("com.google.firebase:firebase-auth:7.2.0")
    implementation("com.google.android.play:core-ktx:1.7.0")

    implementation("com.google.android.gms:play-services-fitness:20.0.0")

    implementation("io.coil-kt:coil-compose:2.6.0")


    implementation("com.google.android.gms:play-services-location:21.1.0")
    implementation("com.google.accompanist:accompanist-permissions:0.35.0-alpha")

    implementation ("androidx.compose.runtime:runtime-livedata:1.1.1")
    testImplementation("org.robolectric:robolectric:4.11.1")

    // Dependencies for using MockK in instrumented tests
    androidTestImplementation("io.mockk:mockk:1.13.7")
    androidTestImplementation("io.mockk:mockk-android:1.13.7")
    androidTestImplementation("io.mockk:mockk-agent:1.13.7")
    androidTestImplementation("org.mockito:mockito-core:3.12.4")
    androidTestImplementation("junit:junit: 4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.4.0")

    testImplementation("io.mockk:mockk:1.13.7")
    testImplementation("io.mockk:mockk-android:1.13.7")
    testImplementation("io.mockk:mockk-agent:1.13.7")
    testImplementation("org.mockito.kotlin:mockito-kotlin:3.2.0")
    androidTestImplementation("org.mockito.kotlin:mockito-kotlin:3.2.0")
    androidTestImplementation("org.hamcrest:hamcrest:2.2")
    androidTestImplementation("org.mockito:mockito-android:3.12.4")
    androidTestImplementation("org.robolectric:shadows-framework:4.11.1")

    val nav_version = "2.7.7"
    androidTestImplementation("androidx.navigation:navigation-testing:$nav_version")
    implementation("androidx.compose.material:material-icons-extended:1.5.1")

    val cameraxVersion = "1.3.0-rc01"

    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-video:$cameraxVersion")

    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("androidx.camera:camera-extensions:$cameraxVersion")

    androidTestImplementation("androidx.arch.core:core-testing:2.1.0")
    implementation("com.google.android.libraries.places:places:2.6.0")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))
    implementation("com.google.firebase:firebase-storage")

}

tasks.register("jacocoTestReport", JacocoReport::class) {
    mustRunAfter("testDebugUnitTest", "connectedCheck")

    reports {
        xml.required = true
        html.required = true
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
    )
    val debugTree = fileTree("${project.buildDir}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }
    val mainSrc = "${project.projectDir}/src/main/java"

    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(fileTree(project.buildDir) {
        include("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
        include("outputs/code_coverage/debugAndroidTest/connected/*/coverage.ec")
    })
}