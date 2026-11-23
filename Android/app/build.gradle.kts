import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

val releaseSigningPropertiesFile = rootProject.file("release-signing.properties")
val releaseSigningProperties = Properties().apply {
    if (releaseSigningPropertiesFile.exists()) {
        releaseSigningPropertiesFile.inputStream().use { load(it) }
    }
}
val releaseStoreFile = releaseSigningProperties.getProperty("storeFile")
    ?: providers.environmentVariable("ANDROID_SIGNING_STORE_FILE").orNull
val releaseStorePassword = releaseSigningProperties.getProperty("storePassword")
    ?: providers.environmentVariable("ANDROID_SIGNING_STORE_PASSWORD").orNull
val releaseKeyAlias = releaseSigningProperties.getProperty("keyAlias")
    ?: providers.environmentVariable("ANDROID_SIGNING_KEY_ALIAS").orNull
val releaseKeyPassword = releaseSigningProperties.getProperty("keyPassword")
    ?: providers.environmentVariable("ANDROID_SIGNING_KEY_PASSWORD").orNull
val hasReleaseSigning = listOf(releaseStoreFile, releaseStorePassword, releaseKeyAlias, releaseKeyPassword)
    .all { !it.isNullOrBlank() }
val requireReleaseSigning = providers.environmentVariable("IGNG_REQUIRE_RELEASE_SIGNING")
    .orNull
    ?.equals("true", ignoreCase = true) == true
if (requireReleaseSigning && !hasReleaseSigning) {
    throw GradleException("Release signing is required (IGNG_REQUIRE_RELEASE_SIGNING=true) but signing credentials are missing.")
}

val apiBaseUrl = providers.gradleProperty("IGNG_API_BASE_URL").orElse("https://www.igng.net").get()
val bbsBaseUrl = providers.gradleProperty("IGNG_BBS_BASE_URL").orElse("https://www.igngbbs.net").get()
val ssoBaseUrl = providers.gradleProperty("IGNG_SSO_BASE_URL").orElse("https://sso.igng.net").get()

android {
    namespace = "net.igng.bbs"
    compileSdk = 36

    defaultConfig {
        applicationId = "net.igng.bbs"
        minSdk = 26
        targetSdk = 36
        versionCode = 20001
        versionName = "2.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "IGNG_API_BASE_URL", "\"$apiBaseUrl\"")
        buildConfigField("String", "IGNG_BBS_BASE_URL", "\"$bbsBaseUrl\"")
        buildConfigField("String", "IGNG_SSO_BASE_URL", "\"$ssoBaseUrl\"")
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = rootProject.file(releaseStoreFile!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (hasReleaseSigning) signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:network"))
    implementation(project(":core:auth"))
    implementation(project(":core:markdown"))
    implementation(project(":core:data"))
    implementation(project(":feature:feed"))
    implementation(project(":feature:column"))
    implementation(project(":feature:post"))
    implementation(project(":feature:inbox"))
    implementation(project(":feature:creator"))
    implementation(project(":feature:profile"))
    implementation(project(":feature:admin"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.kotlinx.coroutines.android)

    debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(libs.junit)
}
