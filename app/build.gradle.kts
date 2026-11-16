import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val localProperties = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use(::load)
}

fun localValue(name: String): String = localProperties.getProperty(name).orEmpty().trim()

fun String.asBuildConfigString(): String =
    "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""

val firebaseConfigured = file("google-services.json").exists()
val appsflyerKey = localValue("APPSFLYER_DEV_KEY")
val qmSubscription = localValue("QM_SUBSCRIPTION")
val qmUid = localValue("QM_UID")
val qmMavenConfigured =
    localValue("QM_MAVEN_USERNAME").isNotEmpty() && localValue("QM_MAVEN_PASSWORD").isNotEmpty()
val qmConfigured = qmMavenConfigured && qmSubscription.isNotEmpty() && qmUid.isNotEmpty()

if (firebaseConfigured) {
    apply(plugin = "com.google.gms.google-services")
}

android {
    namespace = "com.marcormz.jellyapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.marcormz.jellyapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        buildConfigField("boolean", "FIREBASE_CONFIGURED", firebaseConfigured.toString())
        buildConfigField("String", "APPSFLYER_DEV_KEY", appsflyerKey.asBuildConfigString())
        buildConfigField("boolean", "QM_CONFIGURED", qmConfigured.toString())
        buildConfigField("String", "QM_SUBSCRIPTION", qmSubscription.asBuildConfigString())
        buildConfigField("String", "QM_UID", qmUid.asBuildConfigString())
        buildConfigField(
            "boolean",
            "QM_TEST_MODE",
            localValue("QM_TEST_MODE").equals("true", ignoreCase = true).toString(),
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
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

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }

    testOptions {
        animationsDisabled = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.appsflyer)
    implementation(libs.okhttp)

    if (qmMavenConfigured) {
        implementation(libs.quantummetric)
    }

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
