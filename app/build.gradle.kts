plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.google.services)
    alias(libs.plugins.hiltAndroid)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinKsp)
}

android {
    namespace = "pl.coopsoft.szambelan"

    defaultConfig {
        applicationId = "pl.coopsoft.szambelan"
        minSdk = 24
        compileSdk = 35
        targetSdk = 35
        versionCode = 1019
        versionName = "1.19"
        buildConfigField("String", "BASE_URL", "\"" + System.getenv("SZAMBELAN_BASE_URL") + "\"")
    }

    buildTypes {
        release {
            isDebuggable = false
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    @Suppress("UnstableApiUsage")
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                it.jvmArgs("-Xmx2g")
            }
        }
    }
}

dependencies {
    implementation(libs.activity.ktx)

    // Jetpack Compose
    implementation(libs.material)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.ui)
    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    // Google Play services
    implementation(libs.play.services.auth)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.dynamic.links.ktx)

    // Hilt
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // UNIT TESTS
    debugImplementation(libs.compose.ui.test.manifest)
    testImplementation(libs.compose.ui.test.junit4)
    testImplementation(libs.core.ktx)
    testImplementation(libs.espresso.contrib)
    testImplementation(libs.espresso.core)
    testImplementation(libs.espresso.intents)
    testImplementation(libs.junit.ktx)
    testImplementation(libs.junit)
    testImplementation(libs.hilt.testing)
    testImplementation(libs.truth)
    testImplementation(libs.mockwebserver)
    testImplementation(libs.mockk)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.robolectric)
    kspTest(libs.hilt.compiler)
}
