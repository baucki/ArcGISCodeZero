plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.learning.arcgiscodezero"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.learning.arcgiscodezero"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
        kotlinCompilerExtensionVersion = "1.5.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/DEPENDENCIES"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.fragment:fragment-ktx:1.5.5")
    // Jetpack Compose Bill of Materials
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    // Jetpack Compose dependencies
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("com.google.android.material:material:1.5.0")
    implementation(libs.androidx.recyclerview)

    // ArcGIS Map Kotlin SDK dependencies
    implementation("com.esri:arcgis-maps-kotlin:200.4.0")
    implementation("com.esri:arcgis-maps-kotlin-toolkit-authentication")
//    implementation("com.esri.arcgisruntime:arcgis-android:100.15.0")

    // Toolkit dependencies
    implementation(platform("com.esri:arcgis-maps-kotlin-toolkit-bom:200.4.0"))
    implementation("com.esri:arcgis-maps-kotlin-toolkit-geoview-compose")
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}